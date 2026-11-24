import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuItem;
import java.util.List;
import se.michaelthelin.spotify.SpotifyApi;

public class MainController {
    @FXML private Label acc_name;
    @FXML private Label playlist_title;
    @FXML private Label song_title;
    @FXML private ListView<PlaylistItem> playlist_list;
    @FXML private ListView<TrackItem> song_list;
    @FXML private ListView<String> data_list;
    @FXML private MenuItem linkSpotify;

    private SpotifyDataService dataService;

    @FXML
    private void initialize() {
        playlist_list.getSelectionModel().selectedItemProperty().addListener((obs, old, sel) -> {
            if (sel == null || dataService == null) return;

            new Thread(() -> {
            }).start();
        });
    }
    public record TrackItem(String id, String name, String artists, String album,
                        int durationMs, String releaseDate) {
    @Override
    public String toString() {
        return name;   // what the ListView shows
    }
}
/*
    @FXML
    private void onLinkSpotify() {
        linkSpotify.setDisable(true);
        acc_name.setText("Opening browser...");

        new Thread(() -> {
            try {
                SpotifyApi api = spotify_auth.link();
                dataService = new SpotifyDataService(api);

                String userName = dataService.getUserDisplayName();
                //List<PlaylistItem> playlists = dataService.getUserPlaylists();

                Platform.runLater(() -> {
                    acc_name.setText(userName);
                    playlist_list.getItems().setAll(playlists);
                });
            } catch (Exception ex) {
                Platform.runLater(() -> {
                    acc_name.setText("Error: " + ex.getMessage());
                    linkSpotify.setDisable(false);
                });
            }
        }).start();
    }
*/
    @FXML
    private void onClose() {
        Platform.exit();
    }
}