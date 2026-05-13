package mapademo;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import javafx.scene.control.ChoiceDialog;
import javafx.scene.control.TextInputDialog;
import upv.ipc.sportlib.GeoPoint;

public class AnotacionesManager {

    private List<Anotacion> listaAnotaciones = new ArrayList<>();
    private MapViewController mapController;

    public void setMapController(MapViewController mvc) {
        this.mapController = mvc;
        mvc.setOnMapSecondaryClick(geoPoint -> mostrarMenu(geoPoint.getLatitude(), geoPoint.getLongitude()));
    }

    public void mostrarMenu(double lat, double lon) {
        ChoiceDialog<Anotacion.Tipo> tipoDialog = new ChoiceDialog<>(Anotacion.Tipo.NOTA, Anotacion.Tipo.values());
        tipoDialog.setTitle("Nueva Anotación");
        tipoDialog.setHeaderText("Selecciona el tipo de anotación");
        tipoDialog.setContentText("Tipo:");

        Optional<Anotacion.Tipo> tipo = tipoDialog.showAndWait();
        if (tipo.isEmpty()) return;

        TextInputDialog textDialog = new TextInputDialog();
        textDialog.setTitle("Nueva Anotación");
        textDialog.setHeaderText("Tipo: " + tipo.get() + " en " + lat + ", " + lon);
        textDialog.setContentText("Nota:");

        Optional<String> texto = textDialog.showAndWait();
        if (texto.isEmpty() || texto.get().trim().isEmpty()) return;

        Anotacion anotacion = new Anotacion(lat, lon, texto.get().trim(), tipo.get());
        listaAnotaciones.add(anotacion);
        System.out.println("Anotación guardada: " + anotacion.getTipo() + " - " + anotacion.getTexto());
    }

    public List<Anotacion> getAnotaciones() {
        return listaAnotaciones;
    }
}
