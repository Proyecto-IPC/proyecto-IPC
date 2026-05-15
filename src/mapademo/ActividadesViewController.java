package mapademo;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import upv.ipc.sportlib.Activity;
import upv.ipc.sportlib.SportActivityApp;

public class ActividadesViewController implements Initializable {

    @FXML private ScrollPane scrollActividades;
    @FXML private VBox listaActividades;
    @FXML private VBox emptyState;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        cargarActividades();
    }

    private void cargarActividades() {
        listaActividades.getChildren().clear();

        List<Activity> actividades = SportActivityApp.getInstance().getUserActivities();
        if (actividades == null || actividades.isEmpty()) {
            emptyState.setVisible(true);
            emptyState.setManaged(true);
            scrollActividades.setVisible(false);
            scrollActividades.setManaged(false);
        } else {
            emptyState.setVisible(false);
            emptyState.setManaged(false);
            scrollActividades.setVisible(true);
            scrollActividades.setManaged(true);
            for (Activity act : actividades) {
                listaActividades.getChildren().add(crearFilaActividad(act));
            }
        }
    }

    private HBox crearFilaActividad(Activity act) {
        HBox fila = new HBox(10);
        fila.getStyleClass().add("activity-row-placeholder");
        fila.setAlignment(Pos.CENTER_LEFT);
        fila.setCursor(javafx.scene.Cursor.HAND);
        fila.setFocusTraversable(true);

        fila.setOnKeyPressed(e -> {
            if (e.getCode() == javafx.scene.input.KeyCode.ENTER || e.getCode() == javafx.scene.input.KeyCode.SPACE) {
                MainViewController.getInstancia().mostrarDetalleActividad(act);
            }
        });

        Region marker = new Region();
        marker.getStyleClass().add("activity-row-marker");

        VBox body = new VBox(4);
        HBox.setHgrow(body, Priority.ALWAYS);

        Label nombre = new Label(act.getName() != null ? act.getName() : "Actividad sin nombre");
        nombre.getStyleClass().add("activity-row-title");

        HBox chips = new HBox(6);
        chips.getChildren().addAll(
            crearChip(String.format("%.1f km", act.getTotalDistance() / 1000.0)),
            crearChip(formatearTiempo(act.getDuration().toSeconds())),
            crearChip(String.format("%d m", (int) act.getElevationGain()))
        );

        body.getChildren().addAll(nombre, chips);
        fila.getChildren().addAll(marker, body);

        fila.setOnMouseClicked(e -> {
            MainViewController.getInstancia().mostrarDetalleActividad(act);
        });

        return fila;
    }

    private Label crearChip(String texto) {
        Label chip = new Label(texto);
        chip.getStyleClass().add("activity-chip");
        return chip;
    }

    private String formatearTiempo(long segundos) {
        long horas = segundos / 3600;
        long minutos = (segundos % 3600) / 60;
        if (horas > 0) return horas + "h " + minutos + "min";
        return minutos + "min";
    }
}