import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import se.michaelthelin.spotify.SpotifyApi;

import java.util.List;

public class PrototypeApp extends Application {

    private Label statusLabel;
    private Button loginButton;
    private ListView<String> playlistListView;

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Music Discovery - Prototype 1");

        Label header = new Label("Spotify Discovery Prototype");
        header.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        statusLabel = new Label("Status: Not connected");
        statusLabel.setStyle("-fx-text-fill: gray;");

        loginButton = new Button("Log in with Spotify");
        loginButton.setStyle("-fx-background-color: #1DB954; -fx-text-fill: white; -fx-font-weight: bold;");

        playlistListView = new ListView<>();
        playlistListView.setPlaceholder(new Label("No playlists loaded. Click 'Log in with Spotify' above."));

        loginButton.setOnAction(e -> handleLogin());

        VBox layout = new VBox(12, header, loginButton, statusLabel, playlistListView);
        layout.setPadding(new Insets(20));
        layout.setAlignment(Pos.TOP_CENTER);

        Scene scene = new Scene(layout, 400, 500);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void handleLogin() {
        loginButton.setDisable(true);
        statusLabel.setText("Status: Opening browser for authorization...");

        new Thread(() -> {
            try {
                SpotifyApi api = spotify_auth.link();
                SpotifyDataService dataService = new SpotifyDataService(api);

                String userName = dataService.getUserDisplayName();
                List<String> playlists = dataService.getUserPlaylists();

                Platform.runLater(() -> {
                    statusLabel.setText("Logged in as: " + userName);
                    statusLabel.setStyle("-fx-text-fill: green;");
                    playlistListView.getItems().setAll(playlists);
                    loginButton.setText("Connected");
                });
            } catch (Exception ex) {
                Platform.runLater(() -> {
                    statusLabel.setText("Error: " + ex.getMessage());
                    statusLabel.setStyle("-fx-text-fill: red;");
                    loginButton.setDisable(false);
                });
            }
        }).start();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
