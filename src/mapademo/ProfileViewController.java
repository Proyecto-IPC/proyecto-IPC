package mapademo;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;

public class ProfileViewController implements Initializable {

    @FXML private Label lblUsuario;
    @FXML private Label lblEmail;
    @FXML private Button btnLogout;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Aquí cargaremos los datos del usuario desde IPC2026.jar
        lblUsuario.setText("Usuario: -");
        lblEmail.setText("Email: -");
    }

    @FXML
    private void handleLogout() {
        // Aquí cerraremos la sesión y volveremos al Login
    }
}