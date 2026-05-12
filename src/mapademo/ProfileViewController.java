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

public class ProfileViewController implements Initializable {

    @FXML private Label lblUsuario;
    @FXML private TextField txtEmail;
    @FXML private PasswordField txtPassword;
    @FXML private DatePicker dpBirthDate;
    @FXML private TextField txtAvatarPath;
    @FXML private Label lblFeedback;
    @FXML private Button btnGuardar;

    private User currentUser;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        currentUser = SportActivityApp.getInstance().getCurrentUser();
        lblFeedback.setText("");

        if (currentUser == null) {
            lblUsuario.setText("No hay sesión activa");
            btnGuardar.setDisable(true);
            return;
        }

        lblUsuario.setText(currentUser.getNickName());
        txtEmail.setText(currentUser.getEmail());
        dpBirthDate.setValue(currentUser.getBirthDate());
        txtAvatarPath.setText(currentUser.getAvatarPath() == null ? "" : currentUser.getAvatarPath());
    }

    @FXML
    private void handleGuardar() {
        if (currentUser == null) {
            lblFeedback.getStyleClass().setAll("error");
            lblFeedback.setText("Inicia sesión para modificar tu perfil.");
            return;
        }

        String email = txtEmail.getText().trim();
        String newPassword = txtPassword.getText().trim();
        LocalDate birthDate = dpBirthDate.getValue();
        String avatarPath = txtAvatarPath.getText().trim();

        if (email.isEmpty() || birthDate == null) {
            lblFeedback.getStyleClass().setAll("error");
            lblFeedback.setText("Completa correo electrónico y fecha de nacimiento.");
            return;
        }

        if (!User.checkEmail(email)) {
            lblFeedback.getStyleClass().setAll("error");
            lblFeedback.setText("El correo electrónico no es válido.");
            return;
        }

        if (!newPassword.isEmpty() && !User.checkPassword(newPassword)) {
            lblFeedback.getStyleClass().setAll("error");
            lblFeedback.setText("La nueva contraseña debe incluir mayúscula, minúscula, número y símbolo.");
            return;
        }

        if (!User.isOlderThan(birthDate, 12)) {
            lblFeedback.getStyleClass().setAll("error");
            lblFeedback.setText("La fecha indicada debe corresponder a una persona de al menos 12 años.");
            return;
        }

        String passwordToSave = newPassword.isEmpty() ? currentUser.getPassword() : newPassword;
        String avatarToSave = avatarPath.isEmpty() ? currentUser.getAvatarPath() : avatarPath;

        boolean exito = SportActivityApp.getInstance()
                .updateCurrentUser(email, passwordToSave, birthDate, avatarToSave);

        if (exito) {
            currentUser = SportActivityApp.getInstance().getCurrentUser();
            txtPassword.clear();
            lblFeedback.getStyleClass().setAll("success");
            lblFeedback.setText("Perfil actualizado.");
        } else {
            lblFeedback.getStyleClass().setAll("error");
            lblFeedback.setText("No se ha podido actualizar el perfil.");
        }
    }

}
