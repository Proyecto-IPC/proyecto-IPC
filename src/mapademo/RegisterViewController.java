package mapademo;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

public class RegisterViewController implements Initializable {

    @FXML private TextField txtUsuario;
    @FXML private PasswordField txtPassword;
    @FXML private PasswordField txtConfirm;
    @FXML private Button btnRegistrar;
    @FXML private Button btnIrLogin;
    @FXML private Label lblError;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        lblError.setText("");
    }

    @FXML
    private void handleRegistro() {
        String usuario = txtUsuario.getText().trim();
        String password = txtPassword.getText().trim();
        String confirm = txtConfirm.getText().trim();

        if (usuario.isEmpty() || password.isEmpty() || confirm.isEmpty()) {
            lblError.setText("Rellena todos los campos.");
            return;
        }

        if (!password.equals(confirm)) {
            lblError.setText("Las contraseñas no coinciden.");
            return;
        }

        // Aquí conectaremos con IPC2026.jar para registrar el usuario
        lblError.setText("Registro pendiente de conectar con la BD.");
    }

    @FXML
    private void handleIrLogin() {
        // Aquí navegaremos al Login cuando conectemos el MainShell
    }
}