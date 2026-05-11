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
        lblError.setText("");  // Ocultamos el label de error al inicio
    }

    @FXML
private void handleLogin() {
    String usuario = txtUsuario.getText().trim();
    String password = txtPassword.getText().trim();

    if (usuario.isEmpty() || password.isEmpty()) {
        lblError.setText("Rellena todos los campos.");
        return;
    }

    // Obtenemos la instancia única de la aplicación
    upv.ipc.sportlib.SportActivityApp app = upv.ipc.sportlib.SportActivityApp.getInstance();

    // Intentamos hacer login con las credenciales introducidas
    boolean exito = app.login(usuario, password);

    if (exito) {
        lblError.setText("");
        // Aquí navegaremos al Dashboard (lo conectamos con el MainShell después)
        System.out.println("Login correcto: " + app.getCurrentUser().getNickName());
    } else {
        lblError.setText("Usuario o contraseña incorrectos.");
    }
}
}