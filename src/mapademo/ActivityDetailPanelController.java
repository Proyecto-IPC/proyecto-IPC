package mapademo;

import java.net.URL;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Point2D;
import javafx.scene.chart.AreaChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import upv.ipc.sportlib.Activity;
import upv.ipc.sportlib.TrackPoint;

public class ActivityDetailPanelController implements Initializable {

    @FXML private Label lblNombreActividad;
    @FXML private Label lblFechaActividad;
    @FXML private Label lblDistancia;
    @FXML private Label lblTiempo;
    @FXML private Label lblRitmo;
    @FXML private Label lblDesnivel;
    @FXML private Button btnVelocidad;
    @FXML private GridPane metricsGrid;
    @FXML private StackPane chartStackContainer;
    @FXML private VBox emptyChartState;
    @FXML private AreaChart<Number, Number> elevationChart;
    @FXML private NumberAxis xAxis;
    @FXML private NumberAxis yAxis;

    @FXML private Region crosshairLine;
    @FXML private Region trackerDot;
    @FXML private VBox tooltipCard;
    @FXML private Label tooltipDist;
    @FXML private Label tooltipElev;
    @FXML private StackPane mapContainer;

    private static final DateTimeFormatter DATE_TIME_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private XYChart.Series<Number, Number> elevationSeries;
    private List<XYChart.Data<Number, Number>> sampledDataPoints = new ArrayList<>();
    private List<TrackPoint> sampledTrackPoints = new ArrayList<>();
    private MapViewController mapController;
    private boolean speedModeEnabled = true;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        configurarFormatoEjes();
        configurarMouseInteractions();
    }

    public void setActivity(Activity activity) {
        if (activity == null) return;

        lblNombreActividad.setText(activity.getName() != null ? activity.getName() : "Actividad sin nombre");
        lblFechaActividad.setText(activity.getStartTime() != null ? activity.getStartTime().format(DATE_TIME_FORMAT) : "Fecha desconocida");

        lblDistancia.setText(String.format("%.1f km", activity.getTotalDistance() / 1000.0));
        lblTiempo.setText(Math.round(activity.getDuration().toSeconds() / 60.0) + " min");
        lblRitmo.setText(String.format("%.2f min/km", activity.getAveragePace()));
        lblDesnivel.setText(Math.round(activity.getElevationGain()) + " m");

        List<TrackPoint> puntos = activity.getTrackPoints();
        if (puntos == null || puntos.size() < 5) {
            elevationChart.setVisible(false);
            emptyChartState.setVisible(true);
            return;
        }

        elevationChart.setVisible(true);
        emptyChartState.setVisible(false);
        elevationSeries = new XYChart.Series<>();
        sampledDataPoints.clear();
        sampledTrackPoints.clear();

        int totalPoints = puntos.size();
        int desiredSamples = 150;
        int step = Math.max(1, (totalPoints - 1) / desiredSamples);

        double acumuladaMetros = 0.0;
        TrackPoint anterior = null;

        for (int i = 0; i < totalPoints; i++) {
            TrackPoint actual = puntos.get(i);
            if (i > 0 && anterior != null) {
                acumuladaMetros += actual.distanceTo(anterior);
            }
            anterior = actual;

            if (i == 0 || i == totalPoints - 1 || i % step == 0) {
                double kmAcumulados = acumuladaMetros / 1000.0;
                double altitud = actual.getElevation();

                XYChart.Data<Number, Number> dataNode = new XYChart.Data<>(kmAcumulados, altitud);
                sampledDataPoints.add(dataNode);
                sampledTrackPoints.add(actual);
                elevationSeries.getData().add(dataNode);
            }
        }

        elevationChart.getData().clear();
        elevationChart.getData().add(elevationSeries);
    }

    private void configurarFormatoEjes() {
        xAxis.setTickLabelFormatter(new NumberAxis.DefaultFormatter(xAxis, null, "") {
            @Override
            public String toString(Number object) {
                double km = object.doubleValue();
                return km < 1.0 ? String.format("%.0f m", km * 1000.0) : String.format("%.1f km", km);
            }
        });

        yAxis.setTickLabelFormatter(new NumberAxis.DefaultFormatter(yAxis, null, " m") {
            @Override
            public String toString(Number object) {
                return String.format("%.0f", object.doubleValue());
            }
        });
    }

    private void configurarMouseInteractions() {
        elevationChart.setOnMouseMoved(event -> {
            if (sampledDataPoints.isEmpty() || !elevationChart.isVisible()) return;
            if (!isMouseInsidePlot(event)) {
                ocultarIndicadoresChart();
                return;
            }

            Point2D localToAxis = xAxis.sceneToLocal(event.getSceneX(), event.getSceneY());
            double xValue = xAxis.getValueForDisplay(localToAxis.getX()).doubleValue();

            XYChart.Data<Number, Number> nearestData = null;
            double minDiff = Double.MAX_VALUE;
            for (XYChart.Data<Number, Number> data : sampledDataPoints) {
                double diff = Math.abs(data.getXValue().doubleValue() - xValue);
                if (diff < minDiff) {
                    minDiff = diff;
                    nearestData = data;
                }
            }

            if (nearestData != null) {
                crosshairLine.setVisible(true);
                trackerDot.setVisible(true);
                tooltipCard.setVisible(true);

                Point2D localToStack = chartStackContainer.sceneToLocal(event.getSceneX(), event.getSceneY());
                double displayX = localToStack.getX();

                double displayY = yAxis.getDisplayPosition(nearestData.getYValue());

                crosshairLine.setTranslateX(displayX);

                trackerDot.setTranslateX(displayX - 5.25);
                trackerDot.setTranslateY(displayY + 9.5);

                tooltipCard.setTranslateX(displayX + 12);
                tooltipCard.setTranslateY(displayY - 24);

                tooltipDist.setText(String.format("%.2f km", nearestData.getXValue().doubleValue()));
                tooltipElev.setText(Math.round(nearestData.getYValue().doubleValue()) + " m");
                resaltarPuntoEnMapa(nearestData);
            }
        });

        elevationChart.setOnMouseExited(event -> {
            ocultarIndicadoresChart();
        });
    }

    private boolean isMouseInsidePlot(MouseEvent event) {
        Node plotArea = elevationChart.lookup(".chart-plot-background");
        if (plotArea == null) return false;

        Point2D plotPoint = plotArea.sceneToLocal(event.getSceneX(), event.getSceneY());
        return plotArea.getBoundsInLocal().contains(plotPoint);
    }

    private void ocultarIndicadoresChart() {
        crosshairLine.setVisible(false);
        trackerDot.setVisible(false);
        tooltipCard.setVisible(false);
        if (mapController != null) {
            mapController.clearHighlightedTrackPoint();
        }
    }

    private void resaltarPuntoEnMapa(XYChart.Data<Number, Number> data) {
        if (mapController == null) return;
        int index = sampledDataPoints.indexOf(data);
        if (index >= 0 && index < sampledTrackPoints.size()) {
            mapController.highlightTrackPoint(sampledTrackPoints.get(index));
        }
    }

    @FXML
    private void handleVolver(ActionEvent event) {
        MainViewController.getInstancia().mostrarPantallaPrincipal();
    }

    @FXML
    private void handleToggleVelocidad(ActionEvent event) {
        speedModeEnabled = !speedModeEnabled;
        btnVelocidad.getStyleClass().removeAll("primary-button", "secondary-button");
        btnVelocidad.getStyleClass().add("secondary-button");
        btnVelocidad.setText(speedModeEnabled ? "Ocultar velocidad" : "Mostrar velocidad");
        if (mapController != null) {
            mapController.setSpeedVisualizationEnabled(speedModeEnabled);
        }
    }

    public void setMapNode(javafx.scene.Node mapNode) {
        mapContainer.getChildren().clear();
        if (mapNode != null) {
            mapContainer.getChildren().add(mapNode);
        }
    }

    public void setMapController(MapViewController mapController) {
        this.mapController = mapController;
        this.mapController.setSpeedVisualizationEnabled(speedModeEnabled);
    }
}
