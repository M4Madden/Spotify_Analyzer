/* 
    Authenticate users spotify account
    Matthew Madden

    documentation: https://github.com/spotify-web-api-java/spotify-web-api-java

    Auth requests:
    - user-read-private,
    - user-top-read,
    - user-read-recently-played,
    - user-library-read,
    - user-follow-read,
    - playlist-read-private,
    - playlist-read-collaborative,
    - user-read-playback-state,
    - user-read-currently-playing

    Requires a file named config.properties with 
    spotify.client.id="CLIENT_ID GOES HERE"
*/



import java.awt.Desktop;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Properties;
import java.util.concurrent.CompletableFuture;

import com.sun.net.httpserver.HttpServer;

import se.michaelthelin.spotify.SpotifyApi;
import se.michaelthelin.spotify.SpotifyHttpManager;
import se.michaelthelin.spotify.model_objects.credentials.AuthorizationCodeCredentials;


public class spotify_auth {
    private static final String CLIENT_ID = loadClientId();
    private static final URI REDIRECT = SpotifyHttpManager.makeUri("http://127.0.0.1:8888/callback");

    private static String loadClientId() {
        Properties props = new Properties();
        try (FileInputStream in = new FileInputStream("config.properties")) {
            props.load(in);
        } catch (IOException e) {
            throw new RuntimeException("Could not read config.properties", e);
        }
        return props.getProperty("spotify.client.id");
    }

    /**
    * Links the users spotify account 
    * 
    * @return SpotifyApi
    * 
    * contains access token and refresh token
    */
    public static SpotifyApi link() throws Exception {
        // PKCE verifier + challenge
        byte[] rnd = new byte[64];
        new SecureRandom().nextBytes(rnd);
        String verifier = Base64.getUrlEncoder().withoutPadding().encodeToString(rnd);
        String challenge = Base64.getUrlEncoder().withoutPadding().encodeToString(
            MessageDigest.getInstance("SHA-256").digest(verifier.getBytes(StandardCharsets.US_ASCII)));

        SpotifyApi api = new SpotifyApi.Builder()
            .setClientId(CLIENT_ID)
            .setRedirectUri(REDIRECT)
            .build();

        // Start local server to catch the redirect
        CompletableFuture<String> codeFuture = new CompletableFuture<>();
        HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 8888), 0);
        server.createContext("/callback", ex -> {
            String query = ex.getRequestURI().getQuery(); // code=XXX or error=XXX
            String body = "<h3>Spotify linked. You can close this tab.</h3>";
            ex.sendResponseHeaders(200, body.length());
            ex.getResponseBody().write(body.getBytes());
            ex.close();
            if (query != null && query.startsWith("code=")) {
                codeFuture.complete(query.substring(5).split("&")[0]);
            } else {
                codeFuture.completeExceptionally(new RuntimeException("Auth failed: " + query));
            }
        });
        server.start();

        //Open the login page in the user's browser
        URI loginUri = api.authorizationCodePKCEUri(challenge)
            // Each of these is a usable call from spotify
            .scope("user-read-private,user-top-read,user-read-recently-played,user-library-read,user-follow-read,playlist-read-private,playlist-read-collaborative,user-read-playback-state,user-read-currently-playing")
            .build().execute();
        Desktop.getDesktop().browse(loginUri);

        String code;
        try {
            code = codeFuture.get();
        } finally {
            server.stop(0);
        }

        AuthorizationCodeCredentials creds = api.authorizationCodePKCE(code, verifier).build().execute();
        api.setAccessToken(creds.getAccessToken());
        api.setRefreshToken(creds.getRefreshToken());
        return api;
    }

    public static void main(String[] args) throws Exception {
        SpotifyApi api = link();
        System.out.println("Linked as: " + api.getCurrentUsersProfile().build().execute().getDisplayName());
    }
}