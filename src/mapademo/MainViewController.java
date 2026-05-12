package mapademo;

import java.net.URL;
import java.util.Arrays;
import java.util.ResourceBundle;
import javafx.animation.PauseTransition;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
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
import javafx.util.Duration;
import upv.ipc.sportlib.SportActivityApp;

public class MainViewController implements Initializable {

    @FXML private BorderPane rootPane;
    @FXML private HBox topBar;
    @FXML private VBox railNav;
    @FXML private Button btnImportar;
    @FXML private Label importStatusLabel;
    @FXML private Button btnNavResumen;
    @FXML private Button btnNavActividades;
    @FXML private Button btnNavPerfil;
    @FXML private Button btnNavHistorial;

    private static MainViewController instancia;
    private PauseTransition importStatusTimer;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        instancia = this;
        btnImportar.setTooltip(new Tooltip("Importación GPX pendiente de conexión."));
        importStatusTimer = new PauseTransition(Duration.seconds(2.4));
        importStatusTimer.setOnFinished(event -> {
            importStatusLabel.setVisible(false);
            importStatusLabel.setManaged(false);
        });
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
                setActiveRail(null);
            } else {
                mostrarShell();
                updateRailForView(fxmlPath);
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
    private void handleRailResumen() {
        mostrarPantallaPrincipal();
    }

    @FXML
    private void handleRailActividades() {
        mostrarActividadesPlaceholder();
    }

    @FXML
    private void handleRailPerfil() {
        handlePerfil();
    }

    @FXML
    private void handleRailHistorial() {
        handleHistorial();
    }

    @FXML
    private void handlePerfil() {
        setActiveRail(btnNavPerfil);
        cargarVista("ProfileView.fxml");
    }

    @FXML
    private void handleHistorial() {
        setActiveRail(btnNavHistorial);
        cargarVista("HistorialSesionesView.fxml");
    }

    @FXML
    private void handleLogout() {
        SportActivityApp.getInstance().logout();
        cargarVista("LoginView.fxml");
    }

    @FXML
    private void handleImportarPendiente() {
        importStatusLabel.setVisible(true);
        importStatusLabel.setManaged(true);
        importStatusTimer.playFromStart();
    }

    public void mostrarPantallaPrincipal() {
        mostrarShell();
        setActiveRail(btnNavResumen);
        rootPane.setCenter(crearPantallaPrincipal());
    }

    public void mostrarDetalleActividadPlaceholder() {
        mostrarShell();
        rootPane.setCenter(crearDetalleActividad(null));
    }

    private Node crearPantallaPrincipal() {
        VBox content = new VBox(24);
        content.getStyleClass().add("home-page");

        HBox header = new HBox(16);
        header.setAlignment(Pos.CENTER_LEFT);
        VBox titleGroup = new VBox(4);
        Label title = new Label("Resumen general");
        title.getStyleClass().add("page-title");
        Label subtitle = new Label("Base visual preparada para actividades importadas, sin datos reales conectados.");
        subtitle.getStyleClass().add("muted-label");
        titleGroup.getChildren().addAll(title, subtitle);
        Region headerSpacer = new Region();
        HBox.setHgrow(headerSpacer, Priority.ALWAYS);
        Label dataState = new Label("Datos pendientes");
        dataState.getStyleClass().add("status-pill-muted");
        header.getChildren().addAll(titleGroup, headerSpacer, dataState);

        GridPane statsGrid = new GridPane();
        statsGrid.setHgap(16);
        statsGrid.setVgap(16);
        statsGrid.getStyleClass().add("summary-grid");
        String[][] stats = {
            {"Distancia total", "-- km", "Sin actividades importadas"},
            {"Tiempo total", "--", "Pendiente de GPX real"},
            {"Desnivel", "-- m", "Reservado para cálculo real"},
            {"Actividades", "0", "Lista real pendiente"}
        };
        for (int i = 0; i < stats.length; i++) {
            Node card = crearMetricCard(stats[i][0], stats[i][1], stats[i][2], i);
            GridPane.setHgrow(card, Priority.ALWAYS);
            statsGrid.add(card, i % 4, i / 4);
        }

        HBox dashboardBody = new HBox(20);
        dashboardBody.getStyleClass().add("dashboard-body");

        VBox activityPanel = new VBox(16);
        activityPanel.getStyleClass().add("activity-panel");
        HBox.setHgrow(activityPanel, Priority.ALWAYS);

        HBox activityHeader = new HBox(10);
        activityHeader.setAlignment(Pos.CENTER_LEFT);
        Label activityTitle = new Label("Actividades previstas");
        activityTitle.getStyleClass().add("section-title");
        Label activityHint = new Label("Placeholder");
        activityHint.getStyleClass().add("status-pill");
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        activityHeader.getChildren().addAll(activityTitle, activityHint, spacer);

        VBox list = new VBox(10);
        list.getStyleClass().add("activity-list-placeholder");
        list.getChildren().addAll(
                crearActividadPlaceholder("Actividad importada", "Pendiente de GPX real y selección desde Analista", "-- km", "-- min", "--/km"),
                crearActividadPlaceholder("Ruta por revisar", "Espacio reservado para abrir detalle con mapa", "-- km", "-- min", "-- m"),
                crearActividadPlaceholder("Nueva actividad", "Aparecerá aquí cuando exista una actividad real", "-- km", "-- min", "--/km"));

        VBox emptyState = new VBox(8);
        emptyState.setAlignment(Pos.CENTER_LEFT);
        emptyState.getStyleClass().add("home-empty-state");
        Label emptyTitle = new Label("Sin datos reales todavía");
        emptyTitle.getStyleClass().add("empty-state-title");
        Label emptyText = new Label("Estos bloques solo reservan estructura. Importación, lista real y selección quedan para Analista.");
        emptyText.getStyleClass().add("empty-state-text");
        emptyText.setWrapText(true);
        emptyState.getChildren().addAll(emptyTitle, emptyText);

        activityPanel.getChildren().addAll(activityHeader, list, emptyState);
        VBox sideStack = new VBox(18);
        sideStack.getStyleClass().add("dashboard-side-stack");
        sideStack.getChildren().addAll(crearMapPreviewPlaceholder(), crearCalendarioPlaceholder(), crearChartPlaceholder());
        dashboardBody.getChildren().addAll(activityPanel, sideStack);
        content.getChildren().addAll(header, statsGrid, dashboardBody);

        ScrollPane scroll = new ScrollPane(content);
        scroll.setFitToWidth(true);
        scroll.getStyleClass().add("main-scroll");
        return scroll;
    }

    private void mostrarActividadesPlaceholder() {
        mostrarShell();
        setActiveRail(btnNavActividades);

        VBox content = new VBox(18);
        content.getStyleClass().add("home-page");

        VBox header = new VBox(4);
        Label title = new Label("Actividades");
        title.getStyleClass().add("page-title");
        Label subtitle = new Label("Vista preparada para el módulo de Analista. No hay importación, filtros ni selección real conectados todavía.");
        subtitle.getStyleClass().add("muted-label");
        subtitle.setWrapText(true);
        header.getChildren().addAll(title, subtitle);

        VBox panel = new VBox(12);
        panel.getStyleClass().add("activity-panel");
        panel.getChildren().addAll(
                crearActividadPlaceholder("Actividad futura", "Aquí se mostrará la actividad importada desde un GPX real.", "-- km", "-- min", "--/km"),
                crearActividadPlaceholder("Detalle con mapa", "Al seleccionar una actividad real se abrirá el detalle map-first.", "-- m", "Mapa", "Ruta"));

        VBox emptyState = new VBox(8);
        emptyState.setAlignment(Pos.CENTER_LEFT);
        emptyState.getStyleClass().add("home-empty-state");
        Label emptyTitle = new Label("Sin actividades reales");
        emptyTitle.getStyleClass().add("empty-state-title");
        Label emptyText = new Label("Este apartado solo reserva el espacio de navegación. La lista real y el GPX pertenecen al rol Analista.");
        emptyText.getStyleClass().add("empty-state-text");
        emptyText.setWrapText(true);
        emptyState.getChildren().addAll(emptyTitle, emptyText);

        content.getChildren().addAll(header, panel, emptyState);
        ScrollPane scroll = new ScrollPane(content);
        scroll.setFitToWidth(true);
        scroll.getStyleClass().add("main-scroll");
        rootPane.setCenter(scroll);
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

    private Node crearMetricCard(String label, String value, String helper, int visualOffset) {
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

    private Node crearActividadPlaceholder(String title, String detail, String... chips) {
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

    private Node crearCalendarioPlaceholder() {
        VBox panel = new VBox(12);
        panel.getStyleClass().add("calendar-panel");

        VBox header = new VBox(3);
        Label title = new Label("Racha");
        title.getStyleClass().add("section-title");
        Label subtitle = new Label("Heatmap visual, sin datos reales.");
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

    private Node crearMapPreviewPlaceholder() {
        VBox panel = new VBox(12);
        panel.getStyleClass().add("map-preview-panel");

        VBox header = new VBox(3);
        Label title = new Label("Última ruta");
        title.getStyleClass().add("section-title");
        Label subtitle = new Label("Preview visual, pendiente de actividad real.");
        subtitle.getStyleClass().add("muted-label");
        subtitle.setWrapText(true);
        header.getChildren().addAll(title, subtitle);

        Pane preview = new Pane();
        preview.getStyleClass().add("mini-map-placeholder");
        Region routeOne = crearRouteSegment(62, 92, 78, 4, -20);
        Region routeTwo = crearRouteSegment(126, 74, 64, 4, 22);
        Region routeThree = crearRouteSegment(178, 96, 46, 4, -28);
        Region start = crearMapMarker("mini-map-start", 54, 89);
        Region end = crearMapMarker("mini-map-end", 218, 75);
        Label zoomLabel = new Label("Zoom 1");
        zoomLabel.getStyleClass().add("mini-map-zoom-label");
        zoomLabel.setLayoutX(12);
        zoomLabel.setLayoutY(12);
        preview.getChildren().addAll(routeOne, routeTwo, routeThree, start, end, zoomLabel);

        panel.getChildren().addAll(header, preview);
        return panel;
    }

    private Region crearRouteSegment(double x, double y, double width, double height, double rotate) {
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

    private Region crearMapMarker(String styleClass, double x, double y) {
        Region marker = new Region();
        marker.getStyleClass().add(styleClass);
        marker.setLayoutX(x);
        marker.setLayoutY(y);
        return marker;
    }

    private Node crearChartPlaceholder() {
        VBox panel = new VBox(12);
        panel.getStyleClass().add("chart-panel");

        VBox header = new VBox(3);
        Label title = new Label("Gráfica semanal");
        title.getStyleClass().add("section-title");
        Label subtitle = new Label("Reservada para kilómetros reales por día.");
        subtitle.getStyleClass().add("muted-label");
        subtitle.setWrapText(true);
        header.getChildren().addAll(title, subtitle);

        HBox bars = new HBox(8);
        bars.setAlignment(Pos.BOTTOM_LEFT);
        bars.getStyleClass().add("chart-bars");
        String[] labels = {"L", "M", "X", "J", "V", "S", "D"};
        for (int i = 0; i < labels.length; i++) {
            VBox stack = new VBox(6);
            stack.setAlignment(Pos.BOTTOM_CENTER);
            Region bar = new Region();
            bar.getStyleClass().add(i == 2 || i == 5 ? "chart-bar-accent" : "chart-bar");
            Label day = new Label(labels[i]);
            day.getStyleClass().add("calendar-weekday");
            stack.getChildren().addAll(bar, day);
            bars.getChildren().add(stack);
        }

        panel.getChildren().addAll(header, bars);
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
        rootPane.setLeft(railNav);
    }

    private void ocultarShell() {
        rootPane.setTop(null);
        rootPane.setLeft(null);
    }

    private void updateRailForView(String fxmlPath) {
        if ("ProfileView.fxml".equals(fxmlPath)) {
            setActiveRail(btnNavPerfil);
        } else if ("HistorialSesionesView.fxml".equals(fxmlPath)) {
            setActiveRail(btnNavHistorial);
        }
    }

    private void setActiveRail(Button activeButton) {
        Button[] buttons = {btnNavResumen, btnNavActividades, btnNavPerfil, btnNavHistorial};
        for (Button button : buttons) {
            button.getStyleClass().remove("active");
            if (button == activeButton) {
                button.getStyleClass().add("active");
            }
        }
    }
}
