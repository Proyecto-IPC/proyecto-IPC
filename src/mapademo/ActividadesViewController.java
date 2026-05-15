package mapademo;

import java.io.File;
import java.net.URL;
import java.time.YearMonth;
import java.time.ZoneId;
import java.util.List;
import java.util.ResourceBundle;
import java.util.TreeMap;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import upv.ipc.sportlib.Activity;
import upv.ipc.sportlib.SportActivityApp;

public class ActividadesViewController implements Initializable {

    @FXML private ToggleButton btnViewLista;
    @FXML private ToggleButton btnViewStats;
    @FXML private VBox containerLista;
    @FXML private VBox containerStats;
    @FXML private VBox emptyState;
    @FXML private ScrollPane scrollActividades;
    @FXML private VBox listaActividades;
    @FXML private Button btnImportar;
    
    @FXML @SuppressWarnings("unchecked") private BarChart barChartKm;
    @FXML @SuppressWarnings("unchecked") private LineChart lineChartDesnivel;

    private static boolean preferStatsView = false;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        cargarActividades();

        ToggleGroup group = new ToggleGroup();
        btnViewLista.setToggleGroup(group);
        btnViewStats.setToggleGroup(group);

        if (preferStatsView) {
            btnViewStats.setSelected(true);
            btnViewStats.getStyleClass().add("selected");
            mostrarEstadisticas();
        } else {
            btnViewLista.setSelected(true);
            btnViewLista.getStyleClass().add("selected");
            mostrarLista();
        }

        btnViewLista.setOnAction(e -> {
            preferStatsView = false;
            btnViewLista.getStyleClass().add("selected");
            btnViewStats.getStyleClass().remove("selected");
            mostrarLista();
        });

        btnViewStats.setOnAction(e -> {
            preferStatsView = true;
            btnViewStats.getStyleClass().add("selected");
            btnViewLista.getStyleClass().remove("selected");
            mostrarEstadisticas();
        });
    }

    private void mostrarLista() {
        containerStats.setVisible(false);
        containerLista.setVisible(true);
        boolean isEmpty = listaActividades.getChildren().isEmpty();
        emptyState.setVisible(isEmpty);
        emptyState.setManaged(isEmpty);
        scrollActividades.setVisible(!isEmpty);
        scrollActividades.setManaged(!isEmpty);
    }

    private void mostrarEstadisticas() {
        containerLista.setVisible(false);
        containerStats.setVisible(true);
        poblarGraficos();
    }

    private void poblarGraficos() {
        barChartKm.getData().clear();
        lineChartDesnivel.getData().clear();

        List<Activity> actividades = SportActivityApp.getInstance().getUserActivities();
        if (actividades == null || actividades.isEmpty()) return;

        XYChart.Series<String, Number> serieKm = new XYChart.Series<>();
        TreeMap<YearMonth, Double> kmPorMes = new TreeMap<>();

        double acumuladoDesnivel = 0;
        XYChart.Series<String, Number> serieDesnivel = new XYChart.Series<>();
        java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter.ofPattern("dd/MM");

        for (Activity act : actividades) {
            java.time.LocalDate localDate = act.getStartTime().toLocalDate();
            if (localDate == null) continue;

            YearMonth mes = YearMonth.from(localDate);
            kmPorMes.merge(mes, act.getTotalDistance() / 1000.0, Double::sum);

            acumuladoDesnivel += act.getElevationGain();
            serieDesnivel.getData().add(new XYChart.Data<>(localDate.format(formatter), Math.round(acumuladoDesnivel)));
        }

        for (var entry : kmPorMes.entrySet()) {
            serieKm.getData().add(new XYChart.Data<>(entry.getKey().toString(), entry.getValue()));
        }

        barChartKm.getData().add(serieKm);
        lineChartDesnivel.getData().add(serieDesnivel);
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
        fila.setFocusTraversable(false);

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

    @FXML
    private void handleImportar() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Seleccionar archivo GPX");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Archivos GPX", "*.gpx"));

        File archivo = chooser.showOpenDialog(btnImportar.getScene().getWindow());
        if (archivo != null) {
            try {
                Activity nueva = SportActivityApp.getInstance().importActivity(archivo);
                cargarActividades();
                if (preferStatsView) poblarGraficos();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}