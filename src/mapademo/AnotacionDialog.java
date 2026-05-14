package mapademo;

import javafx.geometry.Insets;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import upv.ipc.sportlib.Annotation;
import upv.ipc.sportlib.AnnotationType;

public class AnotacionDialog {

    public enum Mode { CREAR, EDITAR }

    public static class Result {
        public final AnnotationType tipo;
        public final String colorHex;
        public final double strokeWidth;
        public final String texto;
        public final boolean eliminar;
        public final boolean tipoCambiado;

        public Result(AnnotationType tipo, String colorHex, double strokeWidth, String texto, boolean eliminar, boolean tipoCambiado) {
            this.tipo = tipo;
            this.colorHex = colorHex;
            this.strokeWidth = strokeWidth;
            this.texto = texto;
            this.eliminar = eliminar;
            this.tipoCambiado = tipoCambiado;
        }
    }

    private static final java.util.Map<AnnotationType, String> DEFAULT_COLORS = java.util.Map.of(
        AnnotationType.POINT, "#f59e0b",
        AnnotationType.LINE, "#47e4e4",
        AnnotationType.TEXT, "#993300",
        AnnotationType.CIRCLE, "#8066cc"
    );

    private static final java.util.Map<AnnotationType, Double> DEFAULT_STROKE = java.util.Map.of(
        AnnotationType.POINT, 5.0,
        AnnotationType.LINE, 2.5,
        AnnotationType.TEXT, 10.0,
        AnnotationType.CIRCLE, 2.5
    );

    private Mode mode;
    private AnnotationType tipoOriginal;
    private double lat, lon;

    public AnotacionDialog(double lat, double lon, Annotation existing) {
        this.lat = lat;
        this.lon = lon;
        this.mode = (existing == null) ? Mode.CREAR : Mode.EDITAR;
        this.tipoOriginal = (existing != null) ? existing.getType() : null;
    }

    public Result showAndWait() {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle(mode == Mode.CREAR ? "Nueva Anotación" : "Editar Anotación");
        dialog.setHeaderText(String.format("Coordenadas: %.5f, %.5f", lat, lon));

        GridPane content = new GridPane();
        content.setHgap(10);
        content.setVgap(10);
        content.setPadding(new Insets(10, 10, 10, 10));

        record AnnotationTypeOption(AnnotationType tipo, String nombre) {
            @Override public String toString() { return nombre; }
        }

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

        Label lblTam = new Label("Tamaño:");
        TextField txtTam = new TextField();
        txtTam.setPrefWidth(80);

        Label lblTexto = new Label("Texto:");
        TextField txtTexto = new TextField();
        txtTexto.setPromptText("Descripción de la anotación...");
        txtTexto.setPrefWidth(250);

        if (mode == Mode.CREAR) {
            AnnotationType initialType = choiceTipo.getValue().tipo();
            colorPicker.setValue(Color.web(DEFAULT_COLORS.get(initialType)));
            txtTam.setText(String.valueOf(DEFAULT_STROKE.get(initialType)));
        }

        choiceTipo.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && mode == Mode.CREAR) {
                AnnotationType t = newVal.tipo();
                colorPicker.setValue(Color.web(DEFAULT_COLORS.get(t)));
                txtTam.setText(String.valueOf(DEFAULT_STROKE.get(t)));
            }
        });

        content.add(lblTipo, 0, 0);
        content.add(choiceTipo, 1, 0);
        content.add(lblColor, 0, 1);
        content.add(colorPicker, 1, 1);
        content.add(lblTam, 0, 2);
        content.add(txtTam, 1, 2);
        content.add(lblTexto, 0, 3);
        content.add(txtTexto, 1, 3);

        if (mode == Mode.EDITAR) {
            Annotation ann = getExistingAnnotation();
            if (ann != null) {
                for (AnnotationTypeOption opt : choiceTipo.getItems()) {
                    if (opt.tipo() == ann.getType()) {
                        choiceTipo.setValue(opt);
                        break;
                    }
                }
                colorPicker.setValue(Color.web(ann.getColor()));
                txtTam.setText(String.valueOf(ann.getStrokeWidth()));
                txtTexto.setText(ann.getText().equals("Sin descripción") ? "" : ann.getText());
            }
        }

        dialog.getDialogPane().setContent(content);

        if (mode == Mode.EDITAR) {
            ButtonType btnEliminar = new ButtonType("Eliminar", javafx.scene.control.ButtonBar.ButtonData.LEFT);
            dialog.getDialogPane().getButtonTypes().addAll(btnEliminar, ButtonType.CANCEL, ButtonType.OK);
        } else {
            dialog.getDialogPane().getButtonTypes().addAll(ButtonType.CANCEL, ButtonType.OK);
        }

        var result = dialog.showAndWait();
        if (result.isEmpty() || result.get() == ButtonType.CANCEL) {
            return null;
        }

        ButtonType clickedButton = result.get();
        String clickedText = clickedButton.getText();
        boolean eliminar = (mode == Mode.EDITAR && "Eliminar".equals(clickedText));
        if (eliminar) {
            return new Result(null, null, 0, null, true, false);
        }

        AnnotationType tipoSeleccionado = choiceTipo.getValue().tipo();
        Color color = colorPicker.getValue();
        String texto = txtTexto.getText().trim();
        if (texto.isEmpty()) texto = "Sin descripción";

        double tam;
        try {
            tam = Double.parseDouble(txtTam.getText());
            if (tam < 0.5) tam = 0.5;
            if (tam > 50) tam = 50;
        } catch (NumberFormatException e) {
            tam = DEFAULT_STROKE.getOrDefault(tipoSeleccionado, 2.0);
        }

        boolean tipoCambiado = (mode == Mode.EDITAR && tipoSeleccionado != tipoOriginal);

        String colorHex = String.format("#%02X%02X%02X",
            (int)(color.getRed() * 255),
            (int)(color.getGreen() * 255),
            (int)(color.getBlue() * 255));

        return new Result(tipoSeleccionado, colorHex, tam, texto, false, tipoCambiado);
    }

    private Annotation existingAnnotation;

    public void setExistingAnnotation(Annotation ann) {
        this.existingAnnotation = ann;
    }

    private Annotation getExistingAnnotation() {
        return existingAnnotation;
    }
}