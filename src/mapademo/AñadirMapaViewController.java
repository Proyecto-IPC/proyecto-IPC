package mapademo;

import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;
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

    @Override
    public void initialize(URL url, ResourceBundle rb) { }

    @FXML
    private void handleSeleccionarImagen(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Seleccionar Imagen del Mapa");
        fileChooser.getExtensionFilters().addAll(
            new FileChooser.ExtensionFilter("Imágenes JPG", "*.jpg", "*.jpeg"),
            new FileChooser.ExtensionFilter("Imágenes PNG", "*.png")
        );
        File selectedFile = fileChooser.showOpenDialog(btnExaminar.getScene().getWindow());
        if (selectedFile != null) {
            txtRutaImagen.setText(selectedFile.getAbsolutePath());
        }
    }

    @FXML
    private void handleBotonAñadir(ActionEvent event) {
        try {
            if (txtNombreMapa.getText().isEmpty() || txtRutaImagen.getText().isEmpty() ||
                txtLatNorte.getText().isEmpty() || txtLatSur.getText().isEmpty() ||
                txtLonEste.getText().isEmpty() || txtLonOeste.getText().isEmpty()) {
                mostrarAlerta("Error", "Todos los campos son obligatorios.");
                return;
            }

            double latN = Double.parseDouble(txtLatNorte.getText());
            double latS = Double.parseDouble(txtLatSur.getText());
            double lonE = Double.parseDouble(txtLonEste.getText());
            double lonO = Double.parseDouble(txtLonOeste.getText());

            if (latS >= latN) {
                mostrarAlerta("Error de Coordenadas", "La Latitud Norte debe ser mayor que la Latitud Sur.");
                return;
            }
            if (lonO >= lonE) {
                mostrarAlerta("Error de Coordenadas", "La Longitud Este debe ser mayor que la Longitud Oeste.");
                return;
            }

            File imageFile = new File(txtRutaImagen.getText());
            if (!imageFile.exists()) {
                mostrarAlerta("Error", "El archivo de imagen no existe.");
                return;
            }

            SportActivityApp.getInstance().addMapRegion(
                txtNombreMapa.getText().trim(),
                imageFile,
                latN, latS, lonO, lonE
            );

            mostrarAlerta("Éxito", "El mapa ha sido registrado correctamente.");
            MainViewController.getInstancia().cargarVista("GestionarMapasView.fxml");

        } catch (NumberFormatException e) {
            mostrarAlerta("Error de Formato", "Las coordenadas deben ser números decimales (ejemplo: 40.41).");
        }
    }

    @FXML
    private void handleBotonCancelar(ActionEvent event) {
        MainViewController.getInstancia().mostrarPantallaPrincipal();
    }

    private void mostrarAlerta(String titulo, String mensaje) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
}
    
    

