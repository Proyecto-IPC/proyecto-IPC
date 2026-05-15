package mapademo;

import java.net.URL;
import java.util.List;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import upv.ipc.sportlib.Activity;
import upv.ipc.sportlib.SportActivityApp;

public class EstadisticasViewController implements Initializable {

    @FXML private GridPane statsGrid;
    @FXML private BarChart<String, Number> barChartKm;
    @FXML private LineChart<String, Number> lineChartDesnivel;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        List<Activity> actividades = SportActivityApp.getInstance()
                                        .getCurrentUser()
                                        .getActivities();

        poblarMetricas(actividades);
        poblarBarChart(actividades);
        poblarLineChart(actividades);
    }

    // ─── Tarjetas de resumen (distancia total, tiempo, desnivel, nº actividades)
    private void poblarMetricas(List<Activity> actividades) {
        double totalKm = 0;
        long totalSeg = 0;
        double totalDesnivel = 0;

        if (actividades != null) {
            for (Activity a : actividades) {
                totalKm       += a.getTotalDistance() / 1000.0;
                totalSeg      += a.getDuration().toSeconds();
                totalDesnivel += a.getElevationGain();
            }
        }

        int total = actividades == null ? 0 : actividades.size();

        String[][] datos = {
            {"Distancia total",  String.format("%.1f km", totalKm),        "Suma de todas las rutas"},
            {"Tiempo total",     formatearTiempo(totalSeg),                 "Tiempo en movimiento"},
            {"Desnivel",         String.format("%.0f m", totalDesnivel),    "Desnivel positivo acumulado"},
            {"Actividades",      String.valueOf(total),                     "Rutas importadas"}
        };

        for (int i = 0; i < datos.length; i++) {
            VBox card = crearTarjeta(datos[i][0], datos[i][1], datos[i][2]);
            GridPane.setHgrow(card, Priority.ALWAYS);
            statsGrid.add(card, i, 0);
        }
    }

    private VBox crearTarjeta(String titulo, String valor, String ayuda) {
        VBox card = new VBox(8);
        card.getStyleClass().add("metric-card");
        card.setMaxWidth(Double.MAX_VALUE);
        card.setAlignment(Pos.TOP_LEFT);

        Label lTitulo = new Label(titulo);
        lTitulo.getStyleClass().add("metric-label");

        Label lValor = new Label(valor);
        lValor.getStyleClass().add("metric-value");

        Label lAyuda = new Label(ayuda);
        lAyuda.getStyleClass().add("metric-helper");

        card.getChildren().addAll(lTitulo, lValor, lAyuda);
        return card;
    }

    // ─── BarChart: km agrupados por semana ──────────────────────────────────
    private void poblarBarChart(List<Activity> actividades) {
        if (actividades == null || actividades.isEmpty()) {
            barChartKm.setTitle("Sin datos");
            return;
        }

        // Agrupa km por semana (semana 1, 2, 3...)
        Map<String, Double> porSemana = new LinkedHashMap<>();
        int semana = 1;
        for (int i = 0; i < actividades.size(); i++) {
            if (i % 7 == 0 && i != 0) semana++;
            String key = "S" + semana;
            porSemana.merge(key, actividades.get(i).getTotalDistance() / 1000.0, Double::sum);
        }

        XYChart.Series<String, Number> serie = new XYChart.Series<>();
        porSemana.forEach((s, km) ->
            serie.getData().add(new XYChart.Data<>(s, Math.round(km * 10.0) / 10.0))
        );

        barChartKm.getData().add(serie);
    }

    // ─── LineChart: desnivel acumulado por actividad ─────────────────────────
    private void poblarLineChart(List<Activity> actividades) {
        if (actividades == null || actividades.isEmpty()) {
            lineChartDesnivel.setTitle("Sin datos");
            return;
        }

        XYChart.Series<String, Number> serie = new XYChart.Series<>();
        double acumulado = 0;

        for (int i = 0; i < actividades.size(); i++) {
            acumulado += actividades.get(i).getElevationGain();
            String etiqueta = "Act " + (i + 1);
            serie.getData().add(new XYChart.Data<>(etiqueta, Math.round(acumulado)));
        }

        lineChartDesnivel.getData().add(serie);
    }

    // ─── Utilidad: segundos → "1h 23min" ────────────────────────────────────
    private String formatearTiempo(long segundos) {
        long horas   = segundos / 3600;
        long minutos = (segundos % 3600) / 60;
        if (horas > 0) return horas + "h " + minutos + "min";
        return minutos + "min";
    }
}