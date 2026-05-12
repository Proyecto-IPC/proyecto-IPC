package mapademo;

import java.net.URL;
import java.time.LocalDate;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import upv.ipc.sportlib.SportActivityApp;
import upv.ipc.sportlib.User;

public class RegisterViewController implements Initializable {

    @FXML private TextField txtUsuario;
    @FXML private TextField txtEmail;
    @FXML private PasswordField txtPassword;
    @FXML private PasswordField txtConfirm;
    @FXML private DatePicker dpBirthDate;
    @FXML private TextField txtAvatarPath;
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
        String email = txtEmail.getText().trim();
        String password = txtPassword.getText().trim();
        String confirm = txtConfirm.getText().trim();
        LocalDate birthDate = dpBirthDate.getValue();
        String avatarPath = txtAvatarPath.getText().trim();

        if (usuario.isEmpty() || email.isEmpty() || password.isEmpty() || confirm.isEmpty() || birthDate == null) {
            lblError.setText("Completa usuario, correo, contraseña, confirmación y fecha de nacimiento.");
            return;
        }

        if (!User.checkNickName(usuario)) {
            lblError.setText("El usuario no es válido. Revisa el formato del nombre.");
            return;
        }

        if (!User.checkEmail(email)) {
            lblError.setText("El correo electrónico no es válido.");
            return;
        }

        if (!password.equals(confirm)) {
            lblError.setText("Las contraseñas no coinciden. Escríbelas de nuevo.");
            return;
        }

        if (!User.checkPassword(password)) {
            lblError.setText("Usa una contraseña válida: mayúscula, minúscula, número y símbolo.");
            return;
        }

        if (!User.isOlderThan(birthDate, 12)) {
            lblError.setText("Debes tener al menos 12 años para crear una cuenta.");
            return;
        }

        boolean exito = SportActivityApp.getInstance()
                .registerUser(usuario, email, password, birthDate, avatarPath);

        if (exito) {
            lblError.setText("");
            MainViewController.getInstancia().cargarVista("LoginView.fxml");
        } else {
            lblError.setText("No se ha podido crear la cuenta. Prueba con otro usuario o correo.");
        }
    }

    @FXML
    private void handleIrLogin() {
        MainViewController.getInstancia().cargarVista("LoginView.fxml");
    }
}
