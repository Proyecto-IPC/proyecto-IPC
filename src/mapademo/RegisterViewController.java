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

        if (!upv.ipc.sportlib.User.validateNickName(usuario)) {
            lblError.setText("Nombre de usuario no válido.");
            return;
        }

        if (password.length() < 8) {
            lblError.setText("Contraseña no válida: mínimo 8 caracteres.");
            return;
        }

        if (!upv.ipc.sportlib.User.validatePassword(password)) {
            lblError.setText("Contraseña no válida: debe tener mayúscula, minúscula, número y símbolo.");
            return;
        }

        upv.ipc.sportlib.SportActivityApp app = upv.ipc.sportlib.SportActivityApp.getInstance();

        boolean exito = app.registerUser(usuario, usuario + "@app.com", password, java.time.LocalDate.of(2000, 1, 1), "");

        if (exito) {
            lblError.setText("");
            MainViewController.getInstancia().cargarVista("LoginView.fxml");
        } else {
            lblError.setText("El usuario ya existe o hubo un error.");
        }
    }

    @FXML
    private void handleIrLogin() {
        MainViewController.getInstancia().cargarVista("LoginView.fxml");
    }
}