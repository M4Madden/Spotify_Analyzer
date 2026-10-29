/*
    Matthew Madden

    documentation: https://reccobeats.com/docs/documentation/introduction

    Makes api calls to reccobeats to pull song data
    
    Available:
    - getAudioFeatures:
        - "acousticness"
        - "danceability"
        - "energy"
        - "instrumentalness"
        - "liveness"
        - "loudness"
        - "speechiness"
        - "tempo"
        - "valence"

*/

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class recco_api {
    private static final String BASE_URL = "https://api.reccobeats.com/v1";
    private static final HttpClient client = HttpClient.newHttpClient();

    

    /**
     * Converts spotify ids to recco ids
     * 
     * @param List<String>
     * 
     * @return map
     */
    //Ideally we will store this in a database for cacheing eventually
    public static Map<String, String> spotifyToRecco(List<String> spotifyIds)throws IOException, InterruptedException {
        Map<String, String> map = new HashMap<>();

        HttpRequest req = HttpRequest.newBuilder() // make a request
            .uri(URI.create(BASE_URL + "/track?ids=" + String.join(",", spotifyIds))) // build a link for recco call
            .build();
        HttpResponse<String> res = client.send(req, HttpResponse.BodyHandlers.ofString()); // pull a response

        if (res.statusCode() != 200) { // if server doesnt accept the request
            throw new RuntimeException("ReccoBeats error " + res.statusCode());
        }

        var tracks = JsonParser.parseString(res.body()) 
            .getAsJsonObject().getAsJsonArray("content");

        for (JsonElement el : tracks) {  // step through all tracks in a List
            JsonObject t = el.getAsJsonObject();
            String href = t.get("href").getAsString();
            String spotifyId = href.substring(href.lastIndexOf('/') + 1);
            map.put(spotifyId, t.get("id").getAsString());
        }
        return map;
    }

    /**
     * Pulls audio features from reccobeats
     * 
     * 
     * @param String trackId
     * @return json string containing values 0 to 1
     *      
     **/
    public static String getAudioFeatures(String trackId) throws IOException, InterruptedException {
        HttpRequest req = HttpRequest.newBuilder()
            .uri(URI.create(BASE_URL + "/track/" + trackId + "/audio-features"))
            .build();

        HttpResponse<String> res = client.send(req, HttpResponse.BodyHandlers.ofString());

        if (res.statusCode() == 200) { // if req accepted
            return res.body();// JSON string
        } else if (res.statusCode() == 404) { 
            return null;// track not in their database
        } else if (res.statusCode() == 429) { 
            throw new RuntimeException("Rate limited, retry after: "
                + res.headers().firstValue("Retry-After").orElse("unknown"));  // tell the user how long the server asked to wait
        } else {
            throw new RuntimeException("ReccoBeats error " + res.statusCode()); 
        }
    }

    //output data
    public static void main(String[] args) throws Exception {
        String spotifyId = "7KXjTSCq5nL1LoYtL7XAwS";

        Map<String, String> ids = spotifyToRecco(List.of(spotifyId));
        String reccoId = ids.get(spotifyId);

        if (reccoId == null) {
            System.out.println("Track not found in ReccoBeats");
            return;
        }

        System.out.println("ReccoBeats ID: " + reccoId);
        System.out.println(getAudioFeatures(reccoId));
    }
}