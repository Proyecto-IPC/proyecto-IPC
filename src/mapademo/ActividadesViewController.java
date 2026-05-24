package mapademo;

import java.net.URL;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
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
    @FXML private Button btnImportar;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        cargarActividades();
        AnimationBehavior.installHover(btnImportar);
    }

    private void cargarActividades() {
        listaActividades.getChildren().clear();

        List<Activity> actividades = SportActivityApp.getInstance().getUserActivities();
        List<Activity> ordenadas = actividades.stream()
                .sorted(Comparator.comparing(
                        Activity::getStartTime,
                        Comparator.nullsLast(Comparator.naturalOrder())).reversed())
                .collect(Collectors.toList());
        if (ordenadas.isEmpty()) {
            emptyState.setVisible(true);
            emptyState.setManaged(true);
            scrollActividades.setVisible(false);
            scrollActividades.setManaged(false);
        } else {
            emptyState.setVisible(false);
            emptyState.setManaged(false);
            scrollActividades.setVisible(true);
            scrollActividades.setManaged(true);
            for (Activity act : ordenadas) {
                listaActividades.getChildren().add(crearFilaActividad(act));
            }
        }
    }

    private HBox crearFilaActividad(Activity act) {
        HBox fila = new HBox(10);
        fila.getStyleClass().add("activity-row-placeholder");
        fila.setAlignment(Pos.TOP_LEFT);
        fila.setCursor(javafx.scene.Cursor.HAND);
        fila.setFocusTraversable(true);

        fila.setOnKeyPressed(e -> {
            if (e.getCode() == javafx.scene.input.KeyCode.ENTER || e.getCode() == javafx.scene.input.KeyCode.SPACE) {
                MainViewController.getInstancia().mostrarDetalleActividad(act);
                e.consume();
            }
        });

        Region marker = new Region();
        marker.getStyleClass().add("activity-row-marker");
        marker.setTranslateY(4);

        VBox body = new VBox(7);
        HBox.setHgrow(body, Priority.ALWAYS);

        Label nombre = new Label(act.getName() != null ? act.getName() : "Actividad sin nombre");
        nombre.getStyleClass().add("activity-row-title");
        Label fechaHora = new Label(formatearFechaHora(act));
        fechaHora.getStyleClass().add("muted-label");

        HBox chips = new HBox(6);
        chips.getChildren().addAll(
            crearChip(String.format("%.1f km", act.getTotalDistance() / 1000.0)),
            crearChip(formatearRitmo(act.getTotalDistance(), act.getDuration().getSeconds())),
            crearChip(formatearTiempo(act.getDuration().getSeconds())),
            crearChip(String.format("%d m", Math.round(act.getElevationGain())))
        );

        body.getChildren().addAll(nombre, fechaHora, chips);
        fila.getChildren().addAll(marker, body, ActivityActions.create(act, this::cargarActividades));

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

    private String formatearRitmo(double distanciaMetros, long segundos) {
        if (distanciaMetros <= 0 || segundos <= 0) return "-- /km";
        double km = distanciaMetros / 1000.0;
        double segPorKm = segundos / km;
        long min = (long) (segPorKm / 60);
        long sec = Math.round(segPorKm - min * 60);
        if (sec == 60) { min++; sec = 0; }
        return String.format("%d:%02d /km", min, sec);
    }

    private String formatearTiempo(long segundos) {
        long horas = segundos / 3600;
        long minutos = (segundos % 3600) / 60;
        if (horas > 0) return horas + "h " + minutos + "min";
        return minutos + "min";
    }

    private String formatearFechaHora(Activity act) {
        if (act.getStartTime() == null) {
            return "--";
        }
        String fecha = act.getStartTime().toLocalDate().format(DateTimeFormatter.ofPattern("dd/MM"));
        String inicio = act.getStartTime().format(DateTimeFormatter.ofPattern("HH:mm"));
        if (act.getEndTime() == null) {
            return fecha + " · " + inicio;
        }
        String fin = act.getEndTime().format(DateTimeFormatter.ofPattern("HH:mm"));
        return fecha + " · " + inicio + " - " + fin;
    }

    @FXML
    private void handleImportar() {
        MainViewController.getInstancia().handleImportarPendiente();
    }
}
