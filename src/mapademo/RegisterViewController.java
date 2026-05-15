package mapademo;

import java.io.File;
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
import javafx.scene.control.TextFormatter;
import javafx.scene.control.Tooltip;
import javafx.scene.input.DragEvent;
import javafx.scene.input.TransferMode;
import javafx.stage.FileChooser;
import javafx.util.Duration;
import upv.ipc.sportlib.SportActivityApp;
import upv.ipc.sportlib.User;

public class RegisterViewController implements Initializable {

    @FXML private TextField txtUsuario;
    @FXML private TextField txtEmail;
    @FXML private PasswordField txtPassword;
    @FXML private PasswordField txtConfirm;
    @FXML private TextField txtBirthDay;
    @FXML private TextField txtBirthMonth;
    @FXML private TextField txtBirthYear;
    @FXML private DatePicker dpBirthDate;
    @FXML private Button btnSeleccionarAvatar;
    @FXML private Button btnRegistrar;
    @FXML private Button btnIrLogin;
    @FXML private Label lblError;
    @FXML private Label lblInfoPassword;

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("d/M/uuuu")
            .withResolverStyle(ResolverStyle.STRICT);
    private boolean updatingDateFields;
    private String avatarPath = "";

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        lblError.setText("");
        installDateControls();
        btnSeleccionarAvatar.setTooltip(new Tooltip("Puedes arrastrar una imagen o seleccionarla desde el explorador."));
        AnimationBehavior.installHover(btnRegistrar);
        AnimationBehavior.installHover(btnIrLogin);
        AnimationBehavior.installHover(btnSeleccionarAvatar);

        Tooltip passwordTooltip = new Tooltip("La contraseña debe incluir:\n• Entre 8 y 20 caracteres\n• Una letra mayúscula\n• Una letra minúscula\n• Un número\n• Un símbolo");
        passwordTooltip.setShowDelay(javafx.util.Duration.millis(200));
        passwordTooltip.setShowDuration(Duration.INDEFINITE);
        lblInfoPassword.setTooltip(passwordTooltip);
    }

    @FXML
    private void handleRegistro() {
        String usuario = txtUsuario.getText().trim();
        String email = txtEmail.getText().trim();
        String password = txtPassword.getText().trim();
        String confirm = txtConfirm.getText().trim();
        LocalDate birthDate = parseBirthDate();

        if (hasBirthDateInput() && birthDate == null) {
            lblError.setText("Introduce una fecha de nacimiento válida.");
            return;
        }

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
            if (password.length() < 8 || password.length() > 20) {
                lblError.setText("La contraseña debe tener entre 8 y 20 caracteres.");
            } else {
                lblError.setText("La contraseña debe incluir mayúscula, minúscula, número y símbolo.");
            }
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

    @FXML
    private void handleSeleccionarAvatar() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Seleccionar avatar");
        chooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Imágenes", "*.png", "*.jpg", "*.jpeg", "*.gif"));
        File selected = chooser.showOpenDialog(btnSeleccionarAvatar.getScene().getWindow());
        if (selected != null) {
            setAvatarFile(selected);
        }
    }

    @FXML
    private void handleAvatarDragOver(DragEvent event) {
        if (event.getDragboard().hasFiles()
                && event.getDragboard().getFiles().stream().anyMatch(this::isImageFile)) {
            event.acceptTransferModes(TransferMode.COPY);
        }
        event.consume();
    }

    @FXML
    private void handleAvatarDragDropped(DragEvent event) {
        boolean completed = false;
        if (event.getDragboard().hasFiles()) {
            File file = event.getDragboard().getFiles().stream()
                    .filter(this::isImageFile)
                    .findFirst()
                    .orElse(null);
            if (file != null) {
                setAvatarFile(file);
                completed = true;
            }
        }
        event.setDropCompleted(completed);
        event.consume();
    }

    private void installDateControls() {
        txtBirthDay.setTextFormatter(digitsFormatter(2));
        txtBirthMonth.setTextFormatter(digitsFormatter(2));
        txtBirthYear.setTextFormatter(digitsFormatter(4));

        dpBirthDate.valueProperty().addListener((obs, oldDate, newDate) -> {
            if (updatingDateFields || newDate == null) {
                return;
            }
            updatingDateFields = true;
            txtBirthDay.setText(String.valueOf(newDate.getDayOfMonth()));
            txtBirthMonth.setText(String.valueOf(newDate.getMonthValue()));
            txtBirthYear.setText(String.valueOf(newDate.getYear()));
            updatingDateFields = false;
        });

        txtBirthDay.focusedProperty().addListener((obs, wasFocused, isFocused) -> updateDatePickerOnBlur(isFocused));
        txtBirthMonth.focusedProperty().addListener((obs, wasFocused, isFocused) -> updateDatePickerOnBlur(isFocused));
        txtBirthYear.focusedProperty().addListener((obs, wasFocused, isFocused) -> updateDatePickerOnBlur(isFocused));
    }

    private TextFormatter<String> digitsFormatter(int maxLength) {
        return new TextFormatter<>(change -> {
            String text = change.getControlNewText();
            return text.matches("\\d{0," + maxLength + "}") ? change : null;
        });
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

    private boolean hasBirthDateInput() {
        return !txtBirthDay.getText().trim().isEmpty()
                || !txtBirthMonth.getText().trim().isEmpty()
                || !txtBirthYear.getText().trim().isEmpty();
    }

    private void setAvatarFile(File file) {
        avatarPath = file.getAbsolutePath();
        btnSeleccionarAvatar.setText(file.getName());
        btnSeleccionarAvatar.setTooltip(new Tooltip(avatarPath));
    }

    private boolean isImageFile(File file) {
        String name = file.getName().toLowerCase();
        return file.isFile()
                && (name.endsWith(".png") || name.endsWith(".jpg")
                || name.endsWith(".jpeg") || name.endsWith(".gif"));
    }
}
