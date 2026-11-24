import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class App extends Application {
    private static Stage stage;

    @Override
    public void start(Stage primary) throws IOException {
        stage = primary;
        stage.setTitle("Spotify Analyzer");
        show("Login");
        stage.show();
    }

    /** Swap the window's contents to another FXML screen: "Login", "Register" or "Main". */
    public static void show(String name) throws IOException {
        Parent root = FXMLLoader.load(App.class.getResource("/" + name + ".fxml"));
        stage.setScene(new Scene(root));
    }

    public static void main(String[] args) {
        launch(args);
    }
}
