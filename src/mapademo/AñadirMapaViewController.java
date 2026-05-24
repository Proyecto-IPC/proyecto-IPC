package mapademo;

import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import upv.ipc.sportlib.MapRegion;
import upv.ipc.sportlib.SportActivityApp;

public class AñadirMapaViewController implements Initializable {

    @FXML private TextField txtNombreMapa;
    @FXML private TextField txtRutaImagen;
    @FXML private Button btnExaminar;
    @FXML private TextField txtLatNorte;
    @FXML private TextField txtLatSur;
    @FXML private TextField txtLonEste;
    @FXML private TextField txtLonOeste;
    @FXML private Button btnCancelar;
    @FXML private Button btnAñadir;
    @FXML private ImageView mapPreview;
    @FXML private Label lblPreviewPlaceholder;
    @FXML private Label lblFeedback;

    private File selectedImageFile;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        AnimationBehavior.installHover(btnExaminar);
        AnimationBehavior.installHover(btnAñadir);
        AnimationBehavior.installHover(btnCancelar);
    }

    @FXML
    private void handleSeleccionarImagen(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Seleccionar imagen del mapa");
        fileChooser.getExtensionFilters().addAll(
            new FileChooser.ExtensionFilter("Imágenes", "*.jpg", "*.jpeg", "*.png"),
            new FileChooser.ExtensionFilter("Imágenes JPG", "*.jpg", "*.jpeg"),
            new FileChooser.ExtensionFilter("Imágenes PNG", "*.png")
        );
        File file = fileChooser.showOpenDialog(btnExaminar.getScene().getWindow());
        if (file != null) {
            selectedImageFile = file;
            txtNombreMapa.setText(nombreSinExtension(file));
            txtRutaImagen.setText(file.getAbsolutePath());
            mapPreview.setImage(new Image(file.toURI().toString(), 310, 170, true, true));
            mapPreview.setVisible(true);
            lblPreviewPlaceholder.setVisible(false);
        }
    }

    @FXML
    private void handleBotonAñadir(ActionEvent event) {
        try {
            if (!validarCamposObligatorios()) {
                setFeedback("Todos los campos son obligatorios.", false);
                return;
            }

            double latN = Double.parseDouble(txtLatNorte.getText().trim());
            double latS = Double.parseDouble(txtLatSur.getText().trim());
            double lonE = Double.parseDouble(txtLonEste.getText().trim());
            double lonO = Double.parseDouble(txtLonOeste.getText().trim());

            if (latS >= latN) {
                setFeedback("La latitud máxima debe ser mayor que la mínima.", false);
                return;
            }
            if (lonO >= lonE) {
                setFeedback("La longitud máxima debe ser mayor que la mínima.", false);
                return;
            }
            if (!selectedImageFile.exists()) {
                setFeedback("El archivo de imagen no existe.", false);
                return;
            }

            MapRegion mapa = SportActivityApp.getInstance().addMapRegion(
                nombreSinExtension(selectedImageFile),
                selectedImageFile,
                latN, latS, lonO, lonE
            );

            if (mapa == null) {
                setFeedback("No se ha podido añadir el mapa.", false);
                return;
            }

            MainViewController.getInstancia().cargarVista("GestionMapasView.fxml");
        } catch (NumberFormatException e) {
            setFeedback("Las coordenadas deben ser números decimales.", false);
        }
    }

    private boolean validarCamposObligatorios() {
        return selectedImageFile != null
                && !txtLatNorte.getText().trim().isEmpty()
                && !txtLatSur.getText().trim().isEmpty()
                && !txtLonEste.getText().trim().isEmpty()
                && !txtLonOeste.getText().trim().isEmpty();
    }

    private String nombreSinExtension(File file) {
        String name = file.getName();
        int dotIndex = name.lastIndexOf('.');
        return dotIndex > 0 ? name.substring(0, dotIndex) : name;
    }

    @FXML
    private void handleBotonCancelar(ActionEvent event) {
        MainViewController.getInstancia().cargarVista("GestionMapasView.fxml");
    }

    private void setFeedback(String mensaje, boolean success) {
        lblFeedback.setText(mensaje);
        lblFeedback.getStyleClass().setAll(success ? "success" : "error");
    }
}
