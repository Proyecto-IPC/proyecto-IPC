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
    upv.ipc.sportlib.User user = upv.ipc.sportlib.SportActivityApp.getInstance().getCurrentUser();
    if (user != null) {
        lblUsuario.setText("Usuario: " + user.getNickName());
        lblEmail.setText("Email: " + user.getEmail());
    }
}

    @FXML
    private void handleLogout() {
    upv.ipc.sportlib.SportActivityApp.getInstance().logout();
    MainViewController.getInstancia().cargarVista("LoginView.fxml");
    }
}