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
import javafx.stage.Stage;

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
        // 1. Validar que los campos de nombre, imagen y latitud no estén vacíos
        if (txtNombreMapa.getText().isEmpty() || txtRutaImagen.getText().isEmpty() ||
            txtLatNorte.getText().isEmpty() || txtLatSur.getText().isEmpty()) {
            
            mostrarAlerta("Error", "Los campos Nombre, Imagen, Norte y Sur son obligatorios.");
            return;
        }

        // 2. Convertir texto a números
        double latN = Double.parseDouble(txtLatNorte.getText());
        double latS = Double.parseDouble(txtLatSur.getText());

        // 3. Validación lógica: El Norte siempre debe estar por encima (ser mayor) que el Sur
        if (latS >= latN) {
            mostrarAlerta("Error de Coordenadas", "La Latitud Norte debe ser mayor que la Latitud Sur.");
            return;
        }

        // 4. Lógica de guardado
        System.out.println("Registrando mapa con latitudes: N:" + latN + " S:" + latS);
        
        mostrarAlerta("Éxito", "El mapa ha sido registrado correctamente.");
        cerrarVentana();

    } catch (NumberFormatException e) {
        // Si el usuario escribe letras o usa comas en lugar de puntos
        mostrarAlerta("Error de Formato", "Las latitudes deben ser números decimales (ejemplo: 40.41).");
    }

    }

    @FXML
    private void handleBotonCancelar(ActionEvent event) {
        Stage stage = (Stage) btnCancelar.getScene().getWindow();
    
    
    stage.close();
}

    private void mostrarAlerta(String titulo, String mensaje) {
    Alert alert = new Alert(Alert.AlertType.INFORMATION);
    alert.setTitle(titulo);
    alert.setHeaderText(null);
    alert.setContentText(mensaje);
    alert.showAndWait();
    } 
    
    private void cerrarVentana() {
    Stage stage = (Stage) btnAñadir.getScene().getWindow();
    stage.close();
}
}
    
    

