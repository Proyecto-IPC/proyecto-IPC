/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/javafx/FXMLController.java to edit this template
 */
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

/**
 * FXML Controller class
 *
 * @author amira
 */
public class AñadirMapaViewController implements Initializable {

    @FXML
    private TextField txtNombreMapa;
    @FXML
    private TextField txtRutaImagen;
    @FXML
    private Button btnExaminar;
    @FXML
    private TextField txtLatNorte;
    @FXML
    private TextField txtLatSur;
    @FXML
    private TextField txtLonEste;
    @FXML
    private TextField txtLonOeste;
    @FXML
    private Button btnCancelar;
    @FXML
    private Button btnAñadir;

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // TODO
    }    

    @FXML
    private void handleSeleccionarImagen(ActionEvent event) {
        // 1. Crear el selector de archivos
    FileChooser fileChooser = new FileChooser();
    fileChooser.setTitle("Seleccionar Imagen del Mapa");

    // 2. Filtrar para que solo se vean archivos JPG/PNG
    fileChooser.getExtensionFilters().addAll(
        new FileChooser.ExtensionFilter("Imágenes JPG", "*.jpg", "*.jpeg"),
        new FileChooser.ExtensionFilter("Imágenes PNG", "*.png")
    );

    // 3. Abrir la ventana y obtener el archivo (el escenario se obtiene del propio botón)
    File selectedFile = fileChooser.showOpenDialog(btnExaminar.getScene().getWindow());

    if (selectedFile != null) {
 
        // Ponemos la ruta absoluta del archivo en el TextField para que el usuario la vea
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

        System.out.println("Registrando mapa: " + txtNombreMapa.getText() + " | lat: " + latN + "-" + latS + " | lon: " + lonO + "-" + lonE);
        mostrarAlerta("Éxito", "El mapa ha sido registrado correctamente.");
        MainViewController.getInstancia().mostrarPantallaPrincipal();

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
    
    

