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
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import upv.ipc.sportlib.Activity;
import upv.ipc.sportlib.MapRegion;
import upv.ipc.sportlib.SportActivityApp;

public class DashboardViewController implements Initializable {

    @FXML private GridPane statsGrid;
    @FXML private Label activityCountPill;
    @FXML private VBox activityList;
    @FXML private VBox latestActivityPanel;
    @FXML private VBox latestActivitySummary;
    @FXML private StackPane mapPreview;
    private VBox latestMapChips;
    private HBox latestActivityActions;
    private ActivityMapPreview latestMapView;
    @FXML private GridPane calendarGrid;
    @FXML private HBox chartBars;
    @FXML private Button btnImportar;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        populateMetrics();
        populateActivities();
        populateMapPreview();
        populateLatestActivitySummary();
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
        String ritmoStr = formatearRitmo(distanciaTotal, tiempoTotalSegundos);
        String tiempoStr = formatearTiempo(tiempoTotalSegundos);
        String desnivelStr = formatearEntero(desnivelTotal) + " m";

        activityCountPill.setText(numActividades + " actividad" + (numActividades != 1 ? "es" : ""));

        statsGrid.getChildren().clear();
        String[][] stats = {
            {"Distancia total", distKm},
            {"Ritmo medio", ritmoStr},
            {"Tiempo total", tiempoStr},
            {"Desnivel", desnivelStr}
        };

        for (int i = 0; i < stats.length; i++) {
            VBox card = createMetricCard(stats[i][0], stats[i][1]);
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

        if (actividades.size() <= 1) {
            activityList.getChildren().add(createActivityRow(null, "Sin más actividades", "Aquí aparecerán tus entrenamientos anteriores."));
            return;
        }

        int max = Math.min(actividades.size(), 4);
        List<Activity> recientes = actividades.subList(1, max);

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
        marker.setTranslateY(4);

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
            Label chipRitmo = new Label(formatearRitmo(act.getTotalDistance(), act.getDuration().getSeconds()));
            chipRitmo.getStyleClass().add("activity-chip");
            Label chipTiempo = new Label(formatearTiempo(act.getDuration().getSeconds()));
            chipTiempo.getStyleClass().add("activity-chip");
            Label chipDesn = new Label(String.format("%d m", Math.round(act.getElevationGain())));
            chipDesn.getStyleClass().add("activity-chip");
            chipRow.getChildren().addAll(chipDist, chipRitmo, chipTiempo, chipDesn);
        }

        body.getChildren().addAll(titleNode, detailNode, chipRow);
        row.getChildren().addAll(marker, body);

        if (act != null) {
            Activity clicked = act;
            row.getChildren().add(ActivityActions.create(clicked, this::refrescarDashboard));
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
        mapPreview.getChildren().clear();

        latestMapChips = new VBox(9);
        latestMapChips.getStyleClass().add("latest-map-chips");
        latestMapChips.setAlignment(Pos.CENTER);
        latestMapChips.setFillWidth(false);
        latestMapChips.setMinSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);
        latestMapChips.setMaxSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);
        StackPane.setAlignment(latestMapChips, Pos.CENTER_LEFT);
        StackPane.setMargin(latestMapChips, new Insets(0, 0, 0, 6));

        latestMapView = new ActivityMapPreview();
        latestMapView.setManaged(false);
        latestMapView.setMinSize(0, 0);
        latestMapView.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        latestMapView.resize(mapPreview.getWidth(), mapPreview.getHeight());
        mapPreview.widthProperty().addListener((obs, oldValue, newValue) ->
                latestMapView.resize(newValue.doubleValue(), mapPreview.getHeight())
        );
        mapPreview.heightProperty().addListener((obs, oldValue, newValue) ->
                latestMapView.resize(mapPreview.getWidth(), newValue.doubleValue())
        );

        List<Activity> actividades = getActividadesOrdenadas();
        latestMapView.setActivity(actividades.isEmpty() ? null : actividades.get(0));
        StackPane.setAlignment(latestMapView, Pos.CENTER);
        mapPreview.getChildren().addAll(latestMapView, latestMapChips);
    }

    private void populateLatestActivitySummary() {
        latestActivitySummary.getChildren().clear();
        List<Activity> actividades = getActividadesOrdenadas();

        if (actividades.isEmpty()) {
            if (latestMapChips != null) {
                latestMapChips.getChildren().clear();
            }
            if (latestActivityActions != null) {
                latestActivityActions.getChildren().clear();
            }
            Label empty = new Label("Importa una actividad para ver aquí tu último entrenamiento.");
            empty.getStyleClass().add("muted-label");
            empty.setWrapText(true);
            latestActivitySummary.getChildren().add(empty);
            latestActivityPanel.setOnMouseClicked(null);
            latestActivityPanel.setOnKeyPressed(null);
            latestActivityPanel.setFocusTraversable(false);
            latestActivityPanel.setCursor(javafx.scene.Cursor.DEFAULT);
            return;
        }

        Activity latest = actividades.get(0);
        VBox text = new VBox(2);
        Label name = new Label(latest.getName() != null ? latest.getName() : "Actividad sin nombre");
        name.getStyleClass().add("activity-row-title");
        Label date = new Label(formatearFechaHora(latest));
        date.getStyleClass().add("muted-label");
        text.getChildren().addAll(name, date);

        if (latestMapChips != null) {
            Label statsTitle = new Label("Datos:");
            statsTitle.getStyleClass().add("latest-map-stats-title");
            latestMapChips.getChildren().setAll(
                statsTitle,
                crearChip(String.format("%.1f km", latest.getTotalDistance() / 1000.0)),
                crearChip(formatearRitmo(latest.getTotalDistance(), latest.getDuration().getSeconds())),
                crearChip(formatearTiempo(latest.getDuration().getSeconds())),
                crearChip(String.format("%d m", Math.round(latest.getElevationGain())))
            );
        }

        latestActivityActions = ActivityActions.create(latest, this::refrescarDashboard);
        HBox footer = new HBox(12);
        footer.setAlignment(Pos.CENTER_LEFT);
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        footer.getChildren().addAll(text, spacer, latestActivityActions);

        latestActivitySummary.getChildren().add(footer);

        latestActivityPanel.setCursor(javafx.scene.Cursor.HAND);
        latestActivityPanel.setFocusTraversable(true);
        latestActivityPanel.setOnMouseClicked(e -> MainViewController.getInstancia().mostrarDetalleActividad(latest));
        latestActivityPanel.setOnKeyPressed(e -> {
            if (e.getCode() == javafx.scene.input.KeyCode.ENTER || e.getCode() == javafx.scene.input.KeyCode.SPACE) {
                MainViewController.getInstancia().mostrarDetalleActividad(latest);
                e.consume();
            }
        });
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

        for (int i = 0; i < 7; i++) {
            LocalDate dayDate = today.minusDays(6 - i);
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

            Label day = new Label(formatearInicialDia(dayDate));
            day.getStyleClass().add("calendar-weekday");
            stack.getChildren().addAll(bar, day);
            chartBars.getChildren().add(stack);
        }
    }

    private Label crearChip(String text) {
        Label chip = new Label(text);
        chip.getStyleClass().add("activity-chip");
        return chip;
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

    private VBox createMetricCard(String label, String value) {
        VBox card = new VBox(8);
        card.getStyleClass().add("metric-card");
        card.setMaxWidth(Double.MAX_VALUE);

        Label labelNode = new Label(label);
        labelNode.getStyleClass().add("metric-label");
        Label valueNode = new Label(value);
        valueNode.getStyleClass().add("metric-value");
        card.getChildren().addAll(labelNode, valueNode);
        return card;
    }

    private String formatearDistancia(double metros) {
        if (metros < 1000) {
            return Math.round(metros) + " m";
        }
        double km = metros / 1000.0;
        return String.format("%.1f km", km);
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
            return fecha + " · " + inicio + formatearMapa(act);
        }
        String fin = act.getEndTime().format(DateTimeFormatter.ofPattern("HH:mm"));
        return fecha + " · " + inicio + " - " + fin + formatearMapa(act);
    }

    private String formatearMapa(Activity act) {
        MapRegion mapa = act.getSuggestedMap();
        if (mapa == null || mapa.getName() == null || mapa.getName().isBlank()) {
            return "";
        }
        return " · " + mapa.getName();
    }

    void refrescarDashboard() {
        populateMetrics();
        populateActivities();
        populateMapPreview();
        populateLatestActivitySummary();
        populateStreak();
        populateChart();
    }

    private String formatearInicialDia(LocalDate date) {
        switch (date.getDayOfWeek()) {
            case MONDAY: return "L";
            case TUESDAY: return "M";
            case WEDNESDAY: return "X";
            case THURSDAY: return "J";
            case FRIDAY: return "V";
            case SATURDAY: return "S";
            case SUNDAY: return "D";
            default: return "";
        }
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
