import se.michaelthelin.spotify.SpotifyApi;
import se.michaelthelin.spotify.model_objects.specification.Paging;
import se.michaelthelin.spotify.model_objects.specification.PlaylistSimplified;
import se.michaelthelin.spotify.model_objects.specification.User;

import java.util.ArrayList;
import java.util.List;

public class SpotifyDataService {

    private final SpotifyApi spotifyApi;

    public SpotifyDataService(SpotifyApi spotifyApi) {
        this.spotifyApi = spotifyApi;
    }

    public String getUserDisplayName() {
        try {
            User user = spotifyApi.getCurrentUsersProfile().build().execute();
            return user.getDisplayName();
        } catch (Exception e) {
            System.err.println("Failed to fetch user profile: " + e.getMessage());
            return "Unknown User";
        }
    }

    public List<String> getUserPlaylists() {
        List<String> playlistNames = new ArrayList<>();
        try {
            Paging<PlaylistSimplified> playlistPaging = spotifyApi
                    .getListOfCurrentUsersPlaylists()
                    .limit(20)
                    .build()
                    .execute();

            for (PlaylistSimplified playlist : playlistPaging.getItems()) {
                playlistNames.add(playlist.getName());
            }
        } catch (Exception e) {
            System.err.println("Failed to fetch playlists: " + e.getMessage());
        }
        return playlistNames;
    }
}
