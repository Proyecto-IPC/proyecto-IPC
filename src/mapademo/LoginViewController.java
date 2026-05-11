package mapademo;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

public class LoginViewController implements Initializable {

    @FXML private TextField txtUsuario;
    @FXML private PasswordField txtPassword;
    @FXML private Button btnLogin;
    @FXML private Label lblError;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        lblError.setText("");
    }

    @FXML
    private void handleLogin() {
        String usuario = txtUsuario.getText().trim();
        String password = txtPassword.getText().trim();

        if (usuario.isEmpty() || password.isEmpty()) {
            lblError.setText("Introduce usuario y contraseña para continuar.");
            return;
        }

        upv.ipc.sportlib.SportActivityApp app = upv.ipc.sportlib.SportActivityApp.getInstance();
        boolean exito = app.login(usuario, password);

        if (exito) {
            lblError.setText("");
            MainViewController.getInstancia().cargarVista("ProfileView.fxml");
        } else {
            lblError.setText("Usuario o contraseña incorrectos. Revisa los datos e inténtalo de nuevo.");
        }
    }

    @FXML
    private void handleIrRegistro() {
        MainViewController.getInstancia().cargarVista("RegisterView.fxml");
    }
}
