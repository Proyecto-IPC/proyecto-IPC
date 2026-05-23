package mapademo;

import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.shape.Circle;
import javafx.scene.shape.SVGPath;
import javafx.util.Duration;
import upv.ipc.sportlib.SportActivityApp;
import upv.ipc.sportlib.User;

public class ProfileViewController implements Initializable {

    @FXML private Label lblUsuario;
    @FXML private TextField txtEmail;
    @FXML private PasswordField txtPasswordCurrent;
    @FXML private PasswordField txtPassword;
    @FXML private PasswordField txtPasswordConfirm;
    @FXML private TextField txtBirthDay;
    @FXML private TextField txtBirthMonth;
    @FXML private TextField txtBirthYear;
    @FXML private DatePicker dpBirthDate;
    @FXML private TextField txtAvatarPath;
    @FXML private Label lblFeedback;
    @FXML private Button btnGuardar;
    @FXML private Button btnCancelar;
    @FXML private Button btnExaminar;
    @FXML private Label lblInfoPassword;
    @FXML private Label lblInfoCurrentPassword;
    
    @FXML private ImageView avatarPreview;
    @FXML private SVGPath defaultAvatarPreview;

    private User currentUser;
    private String initialEmail;
    private LocalDate initialBirthDate;
    private String initialAvatarPath;
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("d/M/uuuu")
            .withResolverStyle(ResolverStyle.STRICT);
    private boolean updatingDateFields;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        currentUser = SportActivityApp.getInstance().getCurrentUser();
        lblFeedback.setText("");
        installDateControls();

        Circle clip = new Circle(32, 32, 32);
        avatarPreview.setClip(clip);

        txtAvatarPath.textProperty().addListener((obs, oldVal, newVal) -> {
            actualizarPreviewAvatar(newVal);
        });

        if (currentUser == null) {
            lblUsuario.setText("No hay sesión activa");
            btnGuardar.setDisable(true);
            return;
        }

        lblUsuario.setText(currentUser.getNickName());
        initialEmail = currentUser.getEmail();
        initialBirthDate = currentUser.getBirthDate();
        initialAvatarPath = currentUser.getAvatarPath() == null ? "" : currentUser.getAvatarPath();

        txtEmail.setText(initialEmail);
        dpBirthDate.setValue(initialBirthDate);
        setBirthDateFields(initialBirthDate);
        txtAvatarPath.setText(initialAvatarPath);
        
        AnimationBehavior.installHover(btnGuardar);
        AnimationBehavior.installHover(btnCancelar);
        AnimationBehavior.installHover(btnExaminar);

        Tooltip tooltip = new Tooltip("La contraseña debe incluir:\n• Entre 8 y 20 caracteres\n• Una letra mayúscula\n• Una letra minúscula\n• Un número\n• Un símbolo");
        tooltip.setShowDelay(javafx.util.Duration.millis(200));
        tooltip.setShowDuration(Duration.INDEFINITE);
        lblInfoPassword.setTooltip(tooltip);

        Tooltip currentPasswordTooltip = new Tooltip("Solo requerida si vas a cambiar la contraseña");
        currentPasswordTooltip.setShowDelay(javafx.util.Duration.millis(200));
        currentPasswordTooltip.setShowDuration(Duration.INDEFINITE);
        lblInfoCurrentPassword.setTooltip(currentPasswordTooltip);
    }

    private void actualizarPreviewAvatar(String path) {
        if (path == null || path.trim().isEmpty()) {
            avatarPreview.setVisible(false);
            defaultAvatarPreview.setVisible(true);
            return;
        }
        
        try {
            String uri = path.startsWith("http") || path.startsWith("file:")
                ? path
                : new java.io.File(path).toURI().toString();
                
            Image img = new Image(uri, 64, 64, false, true);
            
            if (!img.isError()) {
                avatarPreview.setImage(img);
                avatarPreview.setVisible(true);
                defaultAvatarPreview.setVisible(false);
            } else {
                avatarPreview.setVisible(false);
                defaultAvatarPreview.setVisible(true);
            }
        } catch (Exception e) {
            avatarPreview.setVisible(false);
            defaultAvatarPreview.setVisible(true);
        }
    }

    @FXML
    private void handleGuardar() {
        if (currentUser == null) return;

        String email = txtEmail.getText().trim();
        String currentPassword = txtPasswordCurrent.getText().trim();
        String newPassword = txtPassword.getText().trim();
        String confirmPassword = txtPasswordConfirm.getText().trim();
        LocalDate birthDate = parseBirthDate();
        String avatarPath = txtAvatarPath.getText().trim();

        if (email.isEmpty() || birthDate == null) {
            mostrarError("Completa el correo electrónico y la fecha de nacimiento.");
            return;
        }

        if (!User.checkEmail(email)) {
            mostrarError("El correo electrónico no es válido.");
            return;
        }

        if (!User.isOlderThan(birthDate, 12)) {
            mostrarError("La fecha indicada debe corresponder a una persona de al menos 12 años.");
            return;
        }

        String passwordToSave = currentUser.getPassword();
        boolean isTryingToChangePassword = !currentPassword.isEmpty() || !newPassword.isEmpty() || !confirmPassword.isEmpty();

        if (isTryingToChangePassword) {
            if (currentPassword.isEmpty()) {
                mostrarError("Debes introducir tu contraseña actual para poder cambiarla.");
                return;
            }
            if (!currentPassword.equals(currentUser.getPassword())) {
                mostrarError("La contraseña actual es incorrecta.");
                return;
            }
            if (newPassword.isEmpty()) {
                mostrarError("Has introducido tu clave actual, pero la nueva contraseña está vacía.");
                return;
            }
            if (!newPassword.equals(confirmPassword)) {
                mostrarError("Las contraseñas nuevas no coinciden.");
                return;
            }
            if (!User.checkPassword(newPassword)) {
                mostrarError("La nueva contraseña debe tener mínimo 8 caracteres, incluir mayúscula, minúscula y un número.");
                return;
            }
            passwordToSave = newPassword;
        }

        String avatarToSave = avatarPath.isEmpty() ? currentUser.getAvatarPath() : avatarPath;
        boolean exito = SportActivityApp.getInstance()
                .updateCurrentUser(email, passwordToSave, birthDate, avatarToSave);

        if (exito) {
            currentUser = SportActivityApp.getInstance().getCurrentUser();
            limpiarCamposPassword();
            
            lblFeedback.getStyleClass().setAll("success");
            lblFeedback.setText("Perfil actualizado con éxito.");

            initialEmail = currentUser.getEmail();
            initialBirthDate = currentUser.getBirthDate();
            initialAvatarPath = currentUser.getAvatarPath() == null ? "" : currentUser.getAvatarPath();
            
            MainViewController.getInstancia().actualizarPerfilUsuario();
        } else {
            mostrarError("Error del servidor: No se ha podido actualizar el perfil.");
        }
    }

    @FXML
    private void handleCancelar() {
        txtEmail.setText(initialEmail);
        dpBirthDate.setValue(initialBirthDate);
        setBirthDateFields(initialBirthDate);
        txtAvatarPath.setText(initialAvatarPath);
        limpiarCamposPassword();
        lblFeedback.setText("");
    }

    private void limpiarCamposPassword() {
        txtPasswordCurrent.clear();
        txtPassword.clear();
        txtPasswordConfirm.clear();
    }

    private void mostrarError(String mensaje) {
        lblFeedback.getStyleClass().setAll("error");
        lblFeedback.setText(mensaje);
    }

    private void installDateControls() {
        txtBirthDay.setTextFormatter(digitsFormatter(2));
        txtBirthMonth.setTextFormatter(digitsFormatter(2));
        txtBirthYear.setTextFormatter(digitsFormatter(4));

        dpBirthDate.valueProperty().addListener((obs, oldDate, newDate) -> {
            if (updatingDateFields || newDate == null) return;
            setBirthDateFields(newDate);
        });

        txtBirthDay.focusedProperty().addListener((obs, wasFocused, isFocused) -> updateDatePickerOnBlur(isFocused));
        txtBirthMonth.focusedProperty().addListener((obs, wasFocused, isFocused) -> updateDatePickerOnBlur(isFocused));
        txtBirthYear.focusedProperty().addListener((obs, wasFocused, isFocused) -> updateDatePickerOnBlur(isFocused));
    }

    private javafx.scene.control.TextFormatter<String> digitsFormatter(int maxLength) {
        return new javafx.scene.control.TextFormatter<>(change -> {
            String text = change.getControlNewText();
            return text.matches("\\d{0," + maxLength + "}") ? change : null;
        });
    }

    private void setBirthDateFields(LocalDate date) {
        if (date == null) return;
        updatingDateFields = true;
        txtBirthDay.setText(String.valueOf(date.getDayOfMonth()));
        txtBirthMonth.setText(String.valueOf(date.getMonthValue()));
        txtBirthYear.setText(String.valueOf(date.getYear()));
        updatingDateFields = false;
    }

    private void updateDatePickerOnBlur(boolean focused) {
        if (!focused) {
            LocalDate parsed = parseBirthDate();
            if (parsed != null) {
                dpBirthDate.setValue(parsed);
            }
        }
    }

    private LocalDate parseBirthDate() {
        String day = txtBirthDay.getText().trim();
        String month = txtBirthMonth.getText().trim();
        String year = txtBirthYear.getText().trim();
        if (day.isEmpty() && month.isEmpty() && year.isEmpty()) {
            return dpBirthDate.getValue();
        }
        if (day.isEmpty() || month.isEmpty() || year.isEmpty()) {
            return null;
        }
        try {
            return LocalDate.parse(day + "/" + month + "/" + year, DATE_FORMAT);
        } catch (DateTimeParseException ex) {
            return null;
        }
    }

    @FXML
    private void handleSeleccionarImagen() {
        javafx.stage.FileChooser fileChooser = new javafx.stage.FileChooser();
        fileChooser.setTitle("Seleccionar Imagen de Perfil");
        fileChooser.getExtensionFilters().addAll(
            new javafx.stage.FileChooser.ExtensionFilter("Imágenes", "*.jpg", "*.jpeg", "*.png")
        );
        java.io.File selectedFile = fileChooser.showOpenDialog(btnExaminar.getScene().getWindow());
        if (selectedFile != null) {
            txtAvatarPath.setText(selectedFile.getAbsolutePath());
        }
    }
}
