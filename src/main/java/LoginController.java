import java.io.IOException;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

public class LoginController {
    @FXML private TextField username;
    @FXML private PasswordField password;
    @FXML private Button login;
    @FXML private Button register;
    @FXML private Label message;

    @FXML
    private void onLogin() throws IOException {
        if (username.getText().isBlank() || password.getText().isEmpty()) {
            message.setText("Enter a username and password");
            return;
        }
        // TODO: check credentials against database
        App.show("Main");
    }

    @FXML
    private void onRegister() throws IOException {
        App.show("Register");
    }
}
