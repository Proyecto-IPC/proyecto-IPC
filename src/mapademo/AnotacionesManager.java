package mapademo;

import java.util.List;
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
        mvc.setOnMapSecondaryClick((geoPoint, hitAnnotation) -> {
            mvc.showAnnotationPanel(geoPoint.getLatitude(), geoPoint.getLongitude(), hitAnnotation);
        });
    }

    public void procesarSegundoPunto(GeoPoint primerPunto, GeoPoint segundoPunto, AnnotationType tipo, String texto, String colorHex, double strokeWidth) {
        if (primerPunto != null && segundoPunto != null) {
            Annotation annotation = new Annotation(tipo, texto, colorHex, strokeWidth, List.of(primerPunto, segundoPunto));
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
                    System.err.println("Error recargando actividad: " + e.getMessage());
                }
                System.out.println("Anotación guardada: " + annotation.getType() + " - " + annotation.getText() + " [" + colorHex + "] stroke=" + strokeWidth);
            }
        }
    }

    public List<Annotation> getAnotaciones() {
        if (activity == null) return List.of();
        return activity.getAnnotations();
    }
}