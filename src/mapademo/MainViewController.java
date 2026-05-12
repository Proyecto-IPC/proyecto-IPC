package mapademo;

import java.net.URL;
import java.util.Arrays;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import upv.ipc.sportlib.SportActivityApp;

public class MainViewController implements Initializable {

    @FXML private BorderPane rootPane;
    @FXML private HBox topBar;
    @FXML private Button btnImportar;

    private static MainViewController instancia;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        instancia = this;
        btnImportar.setTooltip(new Tooltip("Importación GPX pendiente de conexión."));
        cargarVista("LoginView.fxml");
    }

    public static MainViewController getInstancia() {
        return instancia;
    }

    public void cargarVista(String fxmlPath) {
        try {
            Pane vista = FXMLLoader.load(getClass().getResource(fxmlPath));
            if (esVistaAuth(fxmlPath)) {
                ocultarShell();
            } else {
                mostrarShell();
            }
            rootPane.setCenter(vista);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void mostrarShellInicial() {
        mostrarPantallaPrincipal();
    }

    @FXML
    private void handleHome() {
        mostrarPantallaPrincipal();
    }

    @FXML
    private void handlePerfil() {
        cargarVista("ProfileView.fxml");
    }

    @FXML
    private void handleHistorial() {
        cargarVista("HistorialSesionesView.fxml");
    }

    @FXML
    private void handleLogout() {
        SportActivityApp.getInstance().logout();
        cargarVista("LoginView.fxml");
    }

    @FXML
    private void handleImportarPendiente() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Importar actividad");
        alert.setHeaderText("Importación GPX pendiente");
        alert.setContentText("El botón queda preparado para el flujo principal. La importación se conectará cuando esté lista la gestión de actividades.");
        alert.showAndWait();
    }

    public void mostrarPantallaPrincipal() {
        mostrarShell();
        rootPane.setCenter(crearPantallaPrincipal());
    }

    public void mostrarDetalleActividadPlaceholder() {
        mostrarShell();
        rootPane.setCenter(crearDetalleActividad(null));
    }

    private Node crearPantallaPrincipal() {
        VBox content = new VBox(22);
        content.getStyleClass().add("home-page");

        VBox header = new VBox(4);
        Label title = new Label("Resumen general");
        title.getStyleClass().add("page-title");
        Label subtitle = new Label("Aquí aparecerán tus actividades importadas y su evolución cuando el módulo de datos esté conectado.");
        subtitle.getStyleClass().add("muted-label");
        header.getChildren().addAll(title, subtitle);

        GridPane statsGrid = new GridPane();
        statsGrid.setHgap(12);
        statsGrid.setVgap(12);
        statsGrid.getStyleClass().add("summary-grid");
        String[][] stats = {
            {"Distancia total", "-- km"},
            {"Tiempo total", "--"},
            {"Desnivel acumulado", "-- m"},
            {"Actividades", "0"}
        };
        for (int i = 0; i < stats.length; i++) {
            Node card = crearMetricCard(stats[i][0], stats[i][1]);
            GridPane.setHgrow(card, Priority.ALWAYS);
            statsGrid.add(card, i % 4, i / 4);
        }

        HBox dashboardBody = new HBox(16);
        dashboardBody.getStyleClass().add("dashboard-body");

        VBox activityPanel = new VBox(14);
        activityPanel.getStyleClass().add("activity-panel");
        HBox.setHgrow(activityPanel, Priority.ALWAYS);

        HBox activityHeader = new HBox(10);
        activityHeader.setAlignment(Pos.CENTER_LEFT);
        Label activityTitle = new Label("Actividades");
        activityTitle.getStyleClass().add("section-title");
        Label activityHint = new Label("Vista prevista");
        activityHint.getStyleClass().add("status-pill");
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        activityHeader.getChildren().addAll(activityTitle, activityHint, spacer);

        VBox list = new VBox(8);
        list.getStyleClass().add("activity-list-placeholder");
        list.getChildren().addAll(
                crearActividadPlaceholder("Ejemplo de actividad en Gandia", "Vista de ejemplo, pendiente de datos reales", "5.2 km", "32 min", "6:08/km"),
                crearActividadPlaceholder("Ejemplo de ruta en La Safor", "Vista de ejemplo, pendiente de importación GPX", "8.4 km", "51 min", "145 m"),
                crearActividadPlaceholder("Nueva actividad importada", "Aparecerá aquí cuando exista una actividad real", "-- km", "-- min", "--/km"));

        VBox emptyState = new VBox(8);
        emptyState.setAlignment(Pos.CENTER_LEFT);
        emptyState.getStyleClass().add("home-empty-state");
        Label emptyTitle = new Label("Aún no hay actividades");
        emptyTitle.getStyleClass().add("empty-state-title");
        Label emptyText = new Label("Cuando se importe una actividad, aparecerá en esta lista y se podrá abrir su detalle con el mapa.");
        emptyText.getStyleClass().add("empty-state-text");
        emptyText.setWrapText(true);
        emptyState.getChildren().addAll(emptyTitle, emptyText);

        activityPanel.getChildren().addAll(activityHeader, list, emptyState);
        dashboardBody.getChildren().addAll(activityPanel, crearCalendarioPlaceholder());
        content.getChildren().addAll(header, statsGrid, dashboardBody);

        ScrollPane scroll = new ScrollPane(content);
        scroll.setFitToWidth(true);
        scroll.getStyleClass().add("main-scroll");
        return scroll;
    }

    private Node crearDetalleActividad(Object activity) {
        BorderPane detail = new BorderPane();
        detail.getStyleClass().add("activity-detail");

        try {
            Pane mapView = FXMLLoader.load(getClass().getResource("MapView.fxml"));
            detail.setCenter(mapView);
        } catch (Exception e) {
            detail.setCenter(crearPlaceholderError("No se pudo cargar el mapa."));
        }

        VBox side = new VBox(14);
        side.getStyleClass().add("detail-side-panel");
        side.getChildren().addAll(
                crearDetailBlock("Stats", "Distancia, tiempo, ritmo y desnivel quedan listos para datos reales."),
                crearDetailBlock("Desnivel", "Perfil pendiente de datos reales."),
                crearDetailBlock("Velocidad", "Color por tramo pendiente de datos de velocidad."),
                crearDetailBlock("Anotaciones", "Espacio preparado sin anotaciones guardadas."));
        detail.setRight(side);
        return detail;
    }

    private Node crearMetricCard(String label, String value) {
        VBox card = new VBox(6);
        card.getStyleClass().add("metric-card");
        card.setMaxWidth(Double.MAX_VALUE);
        Label labelNode = new Label(label);
        labelNode.getStyleClass().add("metric-label");
        Label valueNode = new Label(value);
        valueNode.getStyleClass().add("metric-value");
        card.getChildren().addAll(labelNode, valueNode);
        return card;
    }

    private Node crearActividadPlaceholder(String title, String detail, String... chips) {
        VBox row = new VBox(8);
        row.getStyleClass().add("activity-row-placeholder");
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
        row.getChildren().addAll(titleNode, detailNode, chipRow);
        return row;
    }

    private Node crearCalendarioPlaceholder() {
        VBox panel = new VBox(12);
        panel.getStyleClass().add("calendar-panel");

        VBox header = new VBox(3);
        Label title = new Label("Calendario");
        title.getStyleClass().add("section-title");
        Label subtitle = new Label("Ejemplo visual pendiente de actividades reales.");
        subtitle.getStyleClass().add("muted-label");
        subtitle.setWrapText(true);
        header.getChildren().addAll(title, subtitle);

        GridPane days = new GridPane();
        days.setHgap(6);
        days.setVgap(6);
        days.getStyleClass().add("calendar-grid");
        String[] labels = {"L", "M", "X", "J", "V", "S", "D"};
        for (int i = 0; i < labels.length; i++) {
            Label label = new Label(labels[i]);
            label.getStyleClass().add("calendar-weekday");
            days.add(label, i, 0);
        }
        for (int day = 1; day <= 21; day++) {
            Label cell = new Label(String.valueOf(day));
            cell.getStyleClass().add(day == 5 || day == 12 ? "calendar-day-active" : "calendar-day");
            days.add(cell, (day - 1) % 7, ((day - 1) / 7) + 1);
        }

        panel.getChildren().addAll(header, days);
        return panel;
    }

    private Node crearDetailBlock(String title, String body) {
        VBox block = new VBox(6);
        block.getStyleClass().add("detail-block");
        Label titleNode = new Label(title);
        titleNode.getStyleClass().add("section-title");
        Label bodyNode = new Label(body);
        bodyNode.getStyleClass().add("muted-label");
        bodyNode.setWrapText(true);
        block.getChildren().addAll(titleNode, bodyNode);
        return block;
    }

    private Node crearPlaceholderError(String message) {
        Label label = new Label(message);
        label.getStyleClass().addAll(Arrays.asList("error", "shell-placeholder"));
        BorderPane.setMargin(label, new Insets(24));
        return label;
    }

    private boolean esVistaAuth(String fxmlPath) {
        return "LoginView.fxml".equals(fxmlPath) || "RegisterView.fxml".equals(fxmlPath);
    }

    private void mostrarShell() {
        rootPane.setTop(topBar);
        rootPane.setLeft(null);
    }

    private void ocultarShell() {
        rootPane.setTop(null);
        rootPane.setLeft(null);
    }
}
