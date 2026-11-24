import se.michaelthelin.spotify.SpotifyApi;
import se.michaelthelin.spotify.model_objects.credentials.AuthorizationCodeCredentials;


public class Spotify_connection {

    public static SpotifyApi link (int userID) throws Exception {
        SpotifyApi api = spotify_auth.link();

        User_connection.saveTokens(userID, api.getAccessToken(), api.getRefreshToken(), 3600);
        return api;
    }

    public static SpotifyApi getApi(int userID) throws Exception {
        User_connection.Tokens t = User_connection.getTokens(userID);
        if (t == null) return null;

        SpotifyApi api = spotify_auth.newApi();
        api.setAccessToken(t.access());
        api.setRefreshToken(t.refresh());

        long now = System.currentTimeMillis() / 1000;
        if (t.expiresAtEpoch() - 60 <= now) {
            AuthorizationCodeCredentials c =
            api.authorizationCodePKCERefresh().build().execute();
            api.setAccessToken(c.getAccessToken());
            if (c.getRefreshToken() != null)
            api.setRefreshToken(c.getRefreshToken());
            User_connection.updateAccessToken(userID, c.getAccessToken(), c.getRefreshToken(), c.getExpiresIn());

        }
        return api;
    }
}
