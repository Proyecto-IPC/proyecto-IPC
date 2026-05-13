package mapademo;

import java.util.Optional;
import java.util.stream.Collectors;
import javafx.scene.control.ChoiceDialog;
import javafx.scene.control.TextInputDialog;
import upv.ipc.sportlib.Activity;
import upv.ipc.sportlib.Annotation;
import upv.ipc.sportlib.AnnotationType;
import upv.ipc.sportlib.GeoPoint;
import upv.ipc.sportlib.SportActivityApp;
import java.sql.SQLException;

public class AnotacionesManager {

    private Activity activity;
    private MapViewController mapController;

    public void setActivity(Activity activity) {
        this.activity = activity;
    }

    public void setMapController(MapViewController mvc) {
        this.mapController = mvc;
        mvc.setOnMapSecondaryClick(geoPoint -> mostrarMenu(geoPoint.getLatitude(), geoPoint.getLongitude()));
    }

    public void mostrarMenu(double lat, double lon) {
        AnnotationType[] tipos = {AnnotationType.POINT, AnnotationType.LINE, AnnotationType.TEXT, AnnotationType.CIRCLE};
        ChoiceDialog<AnnotationType> tipoDialog = new ChoiceDialog<>(AnnotationType.POINT, tipos);
        tipoDialog.setTitle("Nueva Anotación");
        tipoDialog.setHeaderText("Selecciona el tipo de anotación");
        tipoDialog.setContentText("Tipo:");

        Optional<AnnotationType> tipo = tipoDialog.showAndWait();
        if (tipo.isEmpty()) return;

        TextInputDialog textDialog = new TextInputDialog();
        textDialog.setTitle("Nueva Anotación");
        textDialog.setHeaderText("Tipo: " + tipo.get() + " en " + lat + ", " + lon);
        textDialog.setContentText("Texto:");

        Optional<String> texto = textDialog.showAndWait();
        if (texto.isEmpty() || texto.get().trim().isEmpty()) return;

        String color = colorPorTipo(tipo.get());
        GeoPoint punto = new GeoPoint(lat, lon);
        Annotation annotation = new Annotation(tipo.get(), texto.get().trim(), color, 2.0, java.util.List.of(punto));

        if (activity != null) {
            SportActivityApp.getInstance().addAnnotation(activity, annotation);
            try {
                Activity refreshed = SportActivityApp.getInstance().getActivityById(activity.getId());
                if (refreshed != null) {
                    this.activity = refreshed;
                    if (mapController != null) {
                        mapController.refreshAnnotations(refreshed.getAnnotations());
                    }
                }
            } catch (SQLException e) {
                System.err.println("No se pudo recargar la actividad: " + e.getMessage());
            }
            System.out.println("Anotación guardada: " + annotation.getType() + " - " + annotation.getText());
        } else {
            System.out.println("Sin actividad activa, anotación no persistida.");
        }
    }

    public java.util.List<Annotation> getAnotaciones() {
        if (activity == null) return java.util.Collections.emptyList();
        return activity.getAnnotations();
    }

    private String colorPorTipo(AnnotationType tipo) {
        return switch (tipo) {
            case POINT -> "#f59e0b";
            case LINE -> "#3b82f6";
            case TEXT -> "#10b981";
            case CIRCLE -> "#8b5cf6";
        };
    }
}