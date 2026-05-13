package mapademo;

import java.util.Optional;
import javafx.geometry.Insets;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import upv.ipc.sportlib.Activity;
import upv.ipc.sportlib.Annotation;
import upv.ipc.sportlib.AnnotationType;
import upv.ipc.sportlib.GeoPoint;
import upv.ipc.sportlib.SportActivityApp;
import java.sql.SQLException;
import javafx.scene.paint.Color;

public class AnotacionesManager {

    private record AnnotationTypeOption(AnnotationType tipo, String nombre) {
        @Override public String toString() { return nombre; }
    }

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
        Dialog<ButtonType> mainDialog = new Dialog<>();
        mainDialog.setTitle("Nueva Anotación");
        mainDialog.setHeaderText("Crear anotación en " + String.format("%.5f, %.5f", lat, lon));

        GridPane content = new GridPane();
        content.setHgap(10);
        content.setVgap(10);
        content.setPadding(new Insets(10, 10, 10, 10));

        Label lblTipo = new Label("Tipo:");
        javafx.scene.control.ChoiceBox<AnnotationTypeOption> choiceTipo = new javafx.scene.control.ChoiceBox<>();
        choiceTipo.getItems().addAll(
            new AnnotationTypeOption(AnnotationType.POINT, "Punto"),
            new AnnotationTypeOption(AnnotationType.LINE, "Línea"),
            new AnnotationTypeOption(AnnotationType.TEXT, "Texto"),
            new AnnotationTypeOption(AnnotationType.CIRCLE, "Círculo")
        );
        choiceTipo.setValue(choiceTipo.getItems().get(0));

        Label lblColor = new Label("Color:");
        ColorPicker colorPicker = new ColorPicker();
        colorPicker.setValue(Color.web("#f59e0b"));

        Label lblTexto = new Label("Texto:");
        javafx.scene.control.TextField txtTexto = new javafx.scene.control.TextField();
        txtTexto.setPromptText("Descripción de la anotación...");
        txtTexto.setPrefWidth(280);

        content.add(lblTipo, 0, 0);
        content.add(choiceTipo, 1, 0);
        content.add(lblColor, 0, 1);
        content.add(colorPicker, 1, 1);
        content.add(lblTexto, 0, 2);
        content.add(txtTexto, 1, 2);

        mainDialog.getDialogPane().setContent(content);
        mainDialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        Optional<ButtonType> result = mainDialog.showAndWait();
        if (result.isEmpty() || result.get() != ButtonType.OK) return;

        AnnotationType tipo = choiceTipo.getValue().tipo();
        Color color = colorPicker.getValue();
        String texto = txtTexto.getText().trim();
        if (texto.isEmpty()) texto = "Sin descripción";

        String colorHex = toHexString(color);
        GeoPoint primerPunto = new GeoPoint(lat, lon);

        if (tipo == AnnotationType.LINE || tipo == AnnotationType.CIRCLE) {
            if (mapController != null) {
                mapController.startPendingSecondPoint(primerPunto, tipo, texto, colorHex, null);
            }
        } else {
            guardarAnotacion(tipo, texto, java.util.List.of(primerPunto), colorHex);
        }
    }

    private void guardarAnotacion(AnnotationType tipo, String texto, java.util.List<GeoPoint> puntos, String colorHex) {
        Annotation annotation = new Annotation(tipo, texto, colorHex, 2.0, puntos);

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
            System.out.println("Anotación guardada: " + annotation.getType() + " - " + annotation.getText() + " [" + colorHex + "]");
        } else {
            System.out.println("Sin actividad activa, anotación no persistida.");
        }
    }

    private String toHexString(Color color) {
        return String.format("#%02X%02X%02X",
            (int) (color.getRed() * 255),
            (int) (color.getGreen() * 255),
            (int) (color.getBlue() * 255));
    }

    public java.util.List<Annotation> getAnotaciones() {
        if (activity == null) return java.util.Collections.emptyList();
        return activity.getAnnotations();
    }
}