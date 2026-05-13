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
        // AQUÍ ESTÁ LA LÍNEA CLAVE: 
        // Ponemos la ruta absoluta del archivo en el TextField para que el usuario la vea
        txtRutaImagen.setText(selectedFile.getAbsolutePath());
    }
}
    }
    

