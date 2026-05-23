package mapademo;

import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Comparator;
import java.util.stream.Collectors;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import upv.ipc.sportlib.Activity;
import upv.ipc.sportlib.SportActivityApp;

public class DashboardViewController implements Initializable {

    @FXML private GridPane statsGrid;
    @FXML private VBox activityList;
    @FXML private Pane mapPreview;
    @FXML private GridPane calendarGrid;
    @FXML private HBox chartBars;
    @FXML private Button btnImportar;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        populateMetrics();
        populateActivities();
        populateMapPreview();
        populateStreak();
        populateChart();
        AnimationBehavior.installHover(btnImportar);
    }

    public void reproducirAnimacionEntrada() {
        statsGrid.setOpacity(0);
        statsGrid.setTranslateY(20);
        Platform.runLater(() -> AnimationBehavior.slideFadeIn(statsGrid));
    }

    private void populateMetrics() {
        List<Activity> actividades = getActividadesOrdenadas();

        double distanciaTotal = actividades.stream()
                .mapToDouble(Activity::getTotalDistance)
                .sum();
        long tiempoTotalSegundos = actividades.stream()
                .mapToLong(a -> a.getDuration().getSeconds())
                .sum();
        double desnivelTotal = actividades.stream()
                .mapToDouble(Activity::getElevationGain)
                .sum();
        int numActividades = actividades.size();

        String distKm = formatearDistancia(distanciaTotal);
        String tiempoStr = formatearTiempo(tiempoTotalSegundos);
        String desnivelStr = formatearEntero(desnivelTotal) + " m";

        String helperDist = numActividades == 0 ? "Sin actividades importadas" : numActividades + " actividad" + (numActividades != 1 ? "es" : "");
        String helperTiempo = numActividades == 0 ? "Sin actividades importadas" : "Acumulado total";
        String helperDesnivel = numActividades == 0 ? "Sin actividades importadas" : "Desnivel positivo";

        statsGrid.getChildren().clear();
        String[][] stats = {
            {"Distancia total", distKm, helperDist},
            {"Tiempo total", tiempoStr, helperTiempo},
            {"Desnivel", desnivelStr, helperDesnivel},
            {"Actividades", String.valueOf(numActividades), numActividades == 0 ? "Sin actividades" : "Registradas"}
        };

        for (int i = 0; i < stats.length; i++) {
            VBox card = createMetricCard(stats[i][0], stats[i][1], stats[i][2], i);
            GridPane.setHgrow(card, Priority.ALWAYS);
            statsGrid.add(card, i, 0);
        }
    }

    private void populateActivities() {
        activityList.getChildren().clear();

        List<Activity> actividades = getActividadesOrdenadas();

        if (actividades.isEmpty()) {
            activityList.getChildren().add(createActivityRow(null, "Sin actividades", "Importa tu primer GPX para ver el resumen aquí."));
            return;
        }

        int max = Math.min(actividades.size(), 3);
        List<Activity> recientes = actividades.subList(0, max);

        for (Activity act : recientes) {
            String nombre = act.getName() != null ? act.getName() : "Actividad sin nombre";
            activityList.getChildren().add(createActivityRow(act, nombre, formatearFechaHora(act)));
        }
    }

    private HBox createActivityRow(Activity act, String title, String detail) {
        HBox row = new HBox(10);
        row.getStyleClass().add("activity-row-placeholder");
        row.setAlignment(Pos.TOP_LEFT);

        Region marker = new Region();
        marker.getStyleClass().add("activity-row-marker");

        VBox body = new VBox(7);
        HBox.setHgrow(body, Priority.ALWAYS);
        Label titleNode = new Label(title);
        titleNode.getStyleClass().add("activity-row-title");
        Label detailNode = new Label(detail);
        detailNode.getStyleClass().add("muted-label");
        detailNode.setWrapText(true);

        HBox chipRow = new HBox(6);

        if (act != null) {
            Label chipDist = new Label(String.format("%.1f km", act.getTotalDistance() / 1000.0));
            chipDist.getStyleClass().add("activity-chip");
            Label chipTiempo = new Label(formatearTiempo(act.getDuration().getSeconds()));
            chipTiempo.getStyleClass().add("activity-chip");
            Label chipDesn = new Label(String.format("%d m", Math.round(act.getElevationGain())));
            chipDesn.getStyleClass().add("activity-chip");
            chipRow.getChildren().addAll(chipDist, chipTiempo, chipDesn);
        }

        body.getChildren().addAll(titleNode, detailNode, chipRow);
        row.getChildren().addAll(marker, body);

        if (act != null) {
            Activity clicked = act;
            row.setOnMouseClicked(e -> MainViewController.getInstancia().mostrarDetalleActividad(clicked));
            row.setCursor(javafx.scene.Cursor.HAND);
            row.setFocusTraversable(true);
            row.setOnKeyPressed(e -> {
                if (e.getCode() == javafx.scene.input.KeyCode.ENTER || e.getCode() == javafx.scene.input.KeyCode.SPACE) {
                    MainViewController.getInstancia().mostrarDetalleActividad(clicked);
                    e.consume();
                }
            });
        }

        return row;
    }

    private void populateMapPreview() {
        if (!mapPreview.getChildren().isEmpty()) return;

        Region routeOne = createRouteSegment(62, 92, 78, 4, -20);
        Region routeTwo = createRouteSegment(126, 74, 64, 4, 22);
        Region routeThree = createRouteSegment(178, 96, 46, 4, -28);
        Region start = createMapMarker("mini-map-start", 54, 89);
        Region end = createMapMarker("mini-map-end", 218, 75);
        Label zoomLabel = new Label("Zoom 1");
        zoomLabel.getStyleClass().add("mini-map-zoom-label");
        zoomLabel.setLayoutX(12);
        zoomLabel.setLayoutY(12);
        mapPreview.getChildren().addAll(routeOne, routeTwo, routeThree, start, end, zoomLabel);
    }

    private void populateStreak() {
        calendarGrid.getChildren().clear();

        String[] labels = {"L", "M", "X", "J", "V", "S", "D"};
        for (int i = 0; i < labels.length; i++) {
            Label label = new Label(labels[i]);
            label.getStyleClass().add("calendar-weekday");
            calendarGrid.add(label, i, 0);
        }

        List<Activity> actividades = getActividadMap();
        LocalDate today = LocalDate.now();
        int offset = today.getDayOfWeek().getValue() - 1;
        LocalDate mondayOfOldestWeek = today.minusDays(offset + 21);

        for (int week = 0; week < 4; week++) {
            for (int col = 0; col < 7; col++) {
                LocalDate day = mondayOfOldestWeek.plusDays(week * 7 + col);
                Label cell = new Label(String.valueOf(day.getDayOfMonth()));
                boolean hayActividad = actividades.stream()
                        .anyMatch(a -> a.getStartTime() != null
                                && a.getStartTime().toLocalDate().equals(day));

                if (hayActividad) {
                    cell.getStyleClass().add("calendar-day-active");
                } else {
                    cell.getStyleClass().add("calendar-day");
                }
                calendarGrid.add(cell, col, week + 1);
            }
        }
    }

    private void populateChart() {
        chartBars.getChildren().clear();

        List<Activity> actividades = getActividadMap();
        LocalDate today = LocalDate.now();

        double[] dayKms = new double[7];
        for (int i = 0; i < 7; i++) {
            LocalDate day = today.minusDays(6 - i);
            final LocalDate d = day;
            dayKms[i] = actividades.stream()
                    .filter(a -> a.getStartTime() != null
                            && a.getStartTime().toLocalDate().equals(d))
                    .mapToDouble(a -> a.getTotalDistance() / 1000.0)
                    .sum();
        }

        double maxKm = 0.001;
        for (double km : dayKms) {
            if (km > maxKm) maxKm = km;
        }

        String[] dayLabels = {"L", "M", "X", "J", "V", "S", "D"};

        for (int i = 0; i < 7; i++) {
            VBox stack = new VBox(6);
            stack.setAlignment(Pos.BOTTOM_CENTER);

            double heightRatio = dayKms[i] / maxKm;
            double barHeight = 20 + (heightRatio * 80);

            Region bar = new Region();
            bar.setPrefHeight(barHeight);
            bar.setMinHeight(barHeight);
            bar.setMaxHeight(barHeight);
            bar.setPrefWidth(20);
            bar.setMinWidth(20);
            bar.setMaxWidth(20);

            if (dayKms[i] > 0) {
                bar.getStyleClass().add("chart-bar-active");
            } else {
                bar.getStyleClass().add("chart-bar-empty");
            }

            if (dayKms[i] > 0) {
                Tooltip tooltip = new Tooltip(String.format("%.1f km", dayKms[i]));
                tooltip.setShowDelay(javafx.util.Duration.millis(400));
                tooltip.setShowDuration(javafx.util.Duration.seconds(3));
                Tooltip.install(bar, tooltip);
            }

            Label day = new Label(dayLabels[i]);
            day.getStyleClass().add("calendar-weekday");
            stack.getChildren().addAll(bar, day);
            chartBars.getChildren().add(stack);
        }
    }

    private List<Activity> getActividades() {
        return SportActivityApp.getInstance().getUserActivities();
    }

    private List<Activity> getActividadesOrdenadas() {
        List<Activity> lista = getActividades();
        return lista.stream()
                .sorted(Comparator.comparing(
                        Activity::getStartTime,
                        Comparator.nullsLast(Comparator.naturalOrder())).reversed())
                .collect(Collectors.toList());
    }

    private List<Activity> getActividadMap() {
        return getActividadesOrdenadas();
    }

    private VBox createMetricCard(String label, String value, String helper, int visualOffset) {
        VBox card = new VBox(8);
        card.getStyleClass().add("metric-card");
        card.setMaxWidth(Double.MAX_VALUE);

        Label labelNode = new Label(label);
        labelNode.getStyleClass().add("metric-label");
        Label valueNode = new Label(value);
        valueNode.getStyleClass().add("metric-value");
        Label helperNode = new Label(helper);
        helperNode.getStyleClass().add("metric-helper");

        HBox bars = new HBox(4);
        bars.getStyleClass().add("metric-bars");
        for (int i = 0; i < 7; i++) {
            Region bar = new Region();
            bar.getStyleClass().add(i <= visualOffset ? "metric-bar-soft" : "metric-bar");
            bars.getChildren().add(bar);
        }

        card.getChildren().addAll(labelNode, valueNode, helperNode, bars);
        return card;
    }

    private Region createRouteSegment(double x, double y, double width, double height, double rotate) {
        Region segment = new Region();
        segment.getStyleClass().add("mini-route-segment");
        segment.setLayoutX(x);
        segment.setLayoutY(y);
        segment.setRotate(rotate);
        segment.setPrefSize(width, height);
        segment.setMinSize(width, height);
        segment.setMaxSize(width, height);
        return segment;
    }

    private Region createMapMarker(String styleClass, double x, double y) {
        Region marker = new Region();
        marker.getStyleClass().add(styleClass);
        marker.setLayoutX(x);
        marker.setLayoutY(y);
        return marker;
    }

    private String formatearDistancia(double metros) {
        if (metros < 1000) {
            return Math.round(metros) + " m";
        }
        double km = metros / 1000.0;
        return String.format("%.1f km", km);
    }

    private String formatearTiempo(long totalSegundos) {
        if (totalSegundos <= 0) return "0 min";
        long minutosTotales = Math.round(totalSegundos / 60.0);
        long horas = minutosTotales / 60;
        long minutos = minutosTotales % 60;
        if (horas > 0) {
            return horas + "h " + minutos + "min";
        }
        return minutos + " min";
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

    private String formatearEntero(double valor) {
        return String.format("%.0f", valor);
    }

    @FXML
    private void handleImportar() {
        MainViewController.getInstancia().handleImportarPendiente();
    }

    @FXML
    private void handleVerTodasActividades() {
        MainViewController.getInstancia().cargarVista("ActividadesView.fxml");
    }
}
