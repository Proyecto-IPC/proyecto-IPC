/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/javafx/FXMLController.java to edit this template
 */
package mapademo;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.Label; // Asegúrate de importar Label
import javafx.scene.text.Font;
import upv.ipc.sportlib.SportActivityApp;
import upv.ipc.sportlib.MapRegion;

public class GestionarMapasViewController implements Initializable {

    @FXML private ListView<MapRegion> listaMapas;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        configurarLista();
        cargarMapas();
    }

    private void configurarLista() {
        // 1. Crear el mensaje que se mostrará cuando la lista esté vacía
        Label mensajeVacio = new Label("Ahora mismo no tienes mapas añadidos");
        mensajeVacio.setFont(new Font("System", 14)); // Tamaño de letra legible
        mensajeVacio.setStyle("-fx-text-fill: #6c757d;"); // Color gris sutil
        
        // 2. Asignar el mensaje como "Placeholder" de la lista
        listaMapas.setPlaceholder(mensajeVacio);

        // 3. Configuración normal de las celdas (para cuando sí haya mapas)
        listaMapas.setCellFactory(lv -> new ListCell<MapRegion>() {
            @Override
            protected void updateItem(MapRegion item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.getName());
                }
            }
        });
    }

    private void cargarMapas() {
        ObservableList<MapRegion> mapas = FXCollections.observableArrayList(
            SportActivityApp.getInstance().getMapRegions()
        );
        listaMapas.setItems(mapas);
    }

    @FXML
    private void handleIrAAñadir(ActionEvent event) {
        MainViewController.getInstancia().mostrarPantallaAñadirMapa();
    }

    @FXML
    private void handleVolver(ActionEvent event) {
        MainViewController.getInstancia().mostrarPantallaPrincipal();
    }
    
    @FXML
    private void handleEliminarMapa(ActionEvent event) {
        // 1. Obtener el mapa que el usuario ha marcado con el ratón
        MapRegion mapaSeleccionado = listaMapas.getSelectionModel().getSelectedItem();
        
        // 2. Si no ha seleccionado ninguno, mostramos un aviso en pantalla
        if (mapaSeleccionado == null) {
            mostrarAlerta("Ningún mapa seleccionado", "Por favor, selecciona un mapa de la lista para poder borrarlo.");
            return;
        }
        
        // 3. Eliminarlo de la base de datos de la SportLib
        // NOTA: Asegúrate de comprobar el nombre exacto de este método en tu librería de la UPV.
        // Habitualmente se llama removeMapRegion o removeMap.
        SportActivityApp.getInstance().getMapRegions().remove(mapaSeleccionado);
        
        // 4. Refrescar la pantalla para que el mapa desaparezca al instante (o salte el aviso de lista vacía)
        cargarMapas();
    }

    private void mostrarAlerta(String titulo, String mensaje) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
}
