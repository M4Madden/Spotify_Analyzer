import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

import java.io.IOException;

public class RegisterController {
    @FXML private TextField username;
    @FXML private PasswordField password;
    @FXML private PasswordField repeat_password;
    @FXML private Button register;
    @FXML private Button back_to_login;
    @FXML private Label passwd_reqs;

    @FXML
    private void onRegister() throws IOException {
        if (username.getText().isBlank()) {
            passwd_reqs.setText("Choose a username");
            return;
        }
        if (password.getText().length() < 8) {
            passwd_reqs.setText("Password must be at least 8 characters");
            return;
        }
        if (!password.getText().equals(repeat_password.getText())) {
            passwd_reqs.setText("Passwords don't match");
            return;
        }
        // TODO: save the new account
        App.show("Login");
    }

    @FXML
    private void onBack() throws IOException {
        App.show("Login");
    }
}
