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

    // Validamos formato de nickname y contraseña con los métodos de la librería
    if (!upv.ipc.sportlib.User.validateNickName(usuario)) {
        lblError.setText("Nombre de usuario no válido.");
        return;
    }

    if (!upv.ipc.sportlib.User.validatePassword(password)) {
        lblError.setText("Contraseña no válida (mín. 8 caracteres).");
        return;
    }

    upv.ipc.sportlib.SportActivityApp app = upv.ipc.sportlib.SportActivityApp.getInstance();

    // Registramos el usuario — email y fecha de nacimiento los pediremos luego en la vista completa
    boolean exito = app.registerUser(usuario, password, usuario + "@app.com", java.time.LocalDate.now(), "");

    if (exito) {
        lblError.setText("");
        System.out.println("Registro correcto: " + usuario);
        // Aquí navegaremos al Login después del registro
    } else {
        lblError.setText("El usuario ya existe o hubo un error.");
    }
}
    @FXML
    private void handleIrLogin() {
        // Aquí navegaremos al Login cuando conectemos el MainShell
    }
}