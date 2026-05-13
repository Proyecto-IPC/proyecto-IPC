/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package mapademo;

/**
 *
 * @author amira
 */


import java.util.ArrayList;
import java.util.List;
import javafx.scene.control.TextInputDialog;
import java.util.Optional;

public class AnotacionesManager {

    // Lista para persistir las notas durante la ejecución
    private List<Anotacion> listaAnotaciones = new ArrayList<>();

    /**
     * Este método será llamado por el hook setOnMapSecondaryClick 
     * en MapViewController.
     */
    public void mostrarMenu(double lat, double lon) {
        // 1. Crear un diálogo de entrada de texto sencillo
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Nueva Anotación");
        dialog.setHeaderText("Añadir comentario en: " + lat + ", " + lon);
        dialog.setContentText("Escribe tu nota aquí:");

        // 2. Esperar la respuesta del usuario
        Optional<String> result = dialog.showAndWait();

        // 3. Si el usuario escribió algo, crear el objeto Anotacion
        result.ifPresent(texto -> {
            if (!texto.trim().isEmpty()) {
                Anotacion nuevaNota = new Anotacion(lat, lon, texto);
                listaAnotaciones.add(nuevaNota);
                System.out.println("Anotación guardada con éxito.");
            }
        });
    }

    /**
     * Método opcional para obtener todas las notas y pintarlas en el mapa
     */
    public List<Anotacion> getAnotaciones() {
        return listaAnotaciones;
    }
}
