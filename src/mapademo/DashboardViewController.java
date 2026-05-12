package mapademo;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

public class DashboardViewController implements Initializable {

    @FXML private GridPane statsGrid;
    @FXML private VBox activityList;
    @FXML private Pane mapPreview;
    @FXML private GridPane calendarGrid;
    @FXML private HBox chartBars;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        populateMetrics();
        populateActivities();
        populateMapPreview();
        populateStreak();
        populateChart();
    }

    private void populateMetrics() {
        String[][] stats = {
            {"Distancia total", "-- km", "Sin actividades importadas"},
            {"Tiempo total", "--", "Pendiente de GPX real"},
            {"Desnivel", "-- m", "Reservado para cálculo real"},
            {"Actividades", "0", "Lista real pendiente"}
        };

        for (int i = 0; i < stats.length; i++) {
            VBox card = createMetricCard(stats[i][0], stats[i][1], stats[i][2], i);
            GridPane.setHgrow(card, Priority.ALWAYS);
            statsGrid.add(card, i, 0);
        }
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

    private void populateActivities() {
        activityList.getChildren().addAll(
                createActivityRow("Actividad importada", "Pendiente de GPX real y selección desde Analista", "-- km", "-- min", "--/km"),
                createActivityRow("Ruta por revisar", "Espacio reservado para abrir detalle con mapa", "-- km", "-- min", "-- m"),
                createActivityRow("Nueva actividad", "Aparecerá aquí cuando exista una actividad real", "-- km", "-- min", "--/km"));
    }

    private HBox createActivityRow(String title, String detail, String... chips) {
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
        for (String chip : chips) {
            Label chipNode = new Label(chip);
            chipNode.getStyleClass().add("activity-chip");
            chipRow.getChildren().add(chipNode);
        }

        body.getChildren().addAll(titleNode, detailNode, chipRow);
        row.getChildren().addAll(marker, body);
        return row;
    }

    private void populateMapPreview() {
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

    private void populateStreak() {
        String[] labels = {"L", "M", "X", "J", "V", "S", "D"};
        for (int i = 0; i < labels.length; i++) {
            Label label = new Label(labels[i]);
            label.getStyleClass().add("calendar-weekday");
            calendarGrid.add(label, i, 0);
        }

        for (int day = 1; day <= 21; day++) {
            Label cell = new Label(String.valueOf(day));
            cell.getStyleClass().add(day == 5 || day == 12 ? "calendar-day-active" : "calendar-day");
            calendarGrid.add(cell, (day - 1) % 7, ((day - 1) / 7) + 1);
        }
    }

    private void populateChart() {
        String[] labels = {"L", "M", "X", "J", "V", "S", "D"};
        for (int i = 0; i < labels.length; i++) {
            VBox stack = new VBox(6);
            stack.setAlignment(Pos.BOTTOM_CENTER);
            Region bar = new Region();
            bar.getStyleClass().add(i == 2 || i == 5 ? "chart-bar-accent" : "chart-bar");
            Label day = new Label(labels[i]);
            day.getStyleClass().add("calendar-weekday");
            stack.getChildren().addAll(bar, day);
            chartBars.getChildren().add(stack);
        }
    }
}
