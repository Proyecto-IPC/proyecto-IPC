package mapademo;

import java.io.File;
import java.net.URL;
import java.util.Arrays;
import java.util.prefs.Preferences;
import java.util.ResourceBundle;
import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.Parent;
import javafx.stage.FileChooser;
import javafx.util.Duration;
import upv.ipc.sportlib.SportActivityApp;

public class MainViewController implements Initializable {

    @FXML private BorderPane rootPane;
    @FXML private HBox topBar;
    @FXML private VBox railNav;
    @FXML private Button btnImportar;
    @FXML private Button btnHome;
    @FXML private Label importStatusLabel;
    @FXML private Button btnNavResumen;
    @FXML private Button btnNavActividades;
    @FXML private Button btnNavPerfil;
    @FXML private Button btnNavHistorial;
    @FXML private Button btnLightMode;
    @FXML private Button btnDarkMode;
    @FXML private Label userNicknameLabel;
    @FXML private javafx.scene.shape.SVGPath defaultUserIcon;
    @FXML private javafx.scene.image.ImageView userAvatarImage;

    private boolean isDarkMode = false;
    private Preferences prefs;

    private static MainViewController instancia;
    private PauseTransition importStatusTimer;
    private AnotacionesManager anotacionesManager;
    private Parent dashboardViewCache;
    private DashboardViewController dashboardController;
    private String currentViewPath;
    private String detailReturnViewPath;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        instancia = this;
        rootPane.setFocusTraversable(true);
        anotacionesManager = new AnotacionesManager();
        importStatusTimer = new PauseTransition(Duration.seconds(2.4));
        importStatusTimer.setOnFinished(event -> {
            importStatusLabel.setVisible(false);
            importStatusLabel.setManaged(false);
        });
        AnimationBehavior.installHover(btnNavResumen);
        AnimationBehavior.installHover(btnNavActividades);
        AnimationBehavior.installHover(btnNavPerfil);
        AnimationBehavior.installHover(btnNavHistorial);
        AnimationBehavior.installHover(btnHome);

        prefs = Preferences.userNodeForPackage(MainViewController.class);
        isDarkMode = prefs.getBoolean("darkMode", false);
        aplicarTemaVisual();
        actualizarPerfilUsuario();

        cargarVista("LoginView.fxml");
    }

    public static MainViewController getInstancia() {
        return instancia;
    }

    public void cargarVista(String fxmlPath) {
        try {
            Parent vista = FXMLLoader.load(getClass().getResource(fxmlPath));
            if (esVistaAuth(fxmlPath)) {
                ocultarShell();
                setActiveRail(null);
            } else {
                mostrarShell();
                updateRailForView(fxmlPath);
            }
            rootPane.setCenter(vista);
            currentViewPath = fxmlPath;
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
        setActiveRail(btnNavActividades);
        cargarVista("ActividadesView.fxml");
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
    private void handleGestionMapas() {
        setActiveRail(null);
        cargarVista("AñadirMapaView.fxml");
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
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Cerrar sesión");
        confirm.setHeaderText(null);
        confirm.setContentText("¿Seguro que quieres cerrar sesión?");

        ButtonType btnCancelar = new ButtonType("Cancelar", javafx.scene.control.ButtonBar.ButtonData.LEFT);
        ButtonType btnCerrar = new ButtonType("Cerrar sesión", javafx.scene.control.ButtonBar.ButtonData.OK_DONE);
        confirm.getButtonTypes().setAll(btnCancelar, btnCerrar);

        if (rootPane != null && rootPane.getScene() != null) {
            confirm.initOwner(rootPane.getScene().getWindow());
            URL cssUrl = getClass().getResource("/resources/style.css");
            if (cssUrl != null) {
                confirm.getDialogPane().getStylesheets().add(cssUrl.toExternalForm());
            }
            confirm.getDialogPane().getStyleClass().add("logout-confirm-dialog");
            confirm.getDialogPane().lookupButton(btnCancelar).getStyleClass().add("logout-cancel-button");
            confirm.getDialogPane().lookupButton(btnCerrar).getStyleClass().add("logout-danger-button");
        }

        confirm.showAndWait().ifPresent(result -> {
            if (result == btnCerrar) {
                SportActivityApp.getInstance().logout();
                cargarVista("LoginView.fxml");
            }
        });
    }

    @FXML
    public void handleImportarPendiente() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Seleccionar archivo GPX");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Archivos GPX", "*.gpx"));

        File archivo = chooser.showOpenDialog(rootPane.getScene().getWindow());
        if (archivo != null) {
            try {
                upv.ipc.sportlib.Activity nueva = SportActivityApp.getInstance().importActivity(archivo);
                if (nueva != null) {
                    importStatusLabel.setText("Importado: " + nueva.getName());
                    importStatusLabel.setVisible(true);
                    importStatusLabel.setManaged(true);
                    importStatusTimer.playFromStart();
                    dashboardViewCache = null;
                    mostrarDetalleActividad(nueva);
                }
            } catch (Exception e) {
                importStatusLabel.setText("Error al importar");
                importStatusLabel.setVisible(true);
                importStatusLabel.setManaged(true);
                importStatusTimer.playFromStart();
                e.printStackTrace();
            }
        }
    }

    public void mostrarPantallaPrincipal() {
        mostrarShell();

        if (rootPane.getCenter() == dashboardViewCache && dashboardViewCache != null) {
            return;
        }

        try {
            if (dashboardViewCache == null) {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("DashboardView.fxml"));
                dashboardViewCache = loader.load();
                dashboardController = loader.getController();
            }

            rootPane.setCenter(dashboardViewCache);
            currentViewPath = "DashboardView.fxml";

            if (dashboardController != null) {
                dashboardController.reproducirAnimacionEntrada();
            }

            actualizarEstadoBotones(btnNavResumen);
            Platform.runLater(() -> rootPane.requestFocus());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    void refrescarDashboardSiExiste() {
        if (dashboardController != null) {
            dashboardController.refrescarDashboard();
        }
    }

    public void mostrarDetalleActividadPlaceholder() {
        mostrarShell();
        rootPane.setCenter(crearDetalleActividad(null));
    }

    private Node loadDashboardView() {
        try {
            return FXMLLoader.load(getClass().getResource("DashboardView.fxml"));
        } catch (Exception e) {
            e.printStackTrace();
            return crearPlaceholderError("No se pudo cargar el resumen.");
        }
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
            FXMLLoader loader = new FXMLLoader(getClass().getResource("MapView.fxml"));
            Pane mapView = loader.load();
            MapViewController mapController = loader.getController();

            if (activity instanceof upv.ipc.sportlib.Activity a) {
                mapController.setActivity(a);
                anotacionesManager.setActivity(a);
            }

            anotacionesManager.setMapController(mapController);
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
        actualizarPerfilUsuario();
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
        } else if ("ActividadesView.fxml".equals(fxmlPath)) {
            setActiveRail(btnNavActividades);
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

    @FXML
    private void setLightMode() {
        if (!isDarkMode) return;
        isDarkMode = false;
        prefs.putBoolean("darkMode", false);
        aplicarTemaVisual();
    }

    @FXML
    private void setDarkMode() {
        if (isDarkMode) return;
        isDarkMode = true;
        prefs.putBoolean("darkMode", true);
        aplicarTemaVisual();
    }

    private void aplicarTemaVisual() {
        if (isDarkMode) {
            if (!rootPane.getStyleClass().contains("theme-dark")) {
                rootPane.getStyleClass().add("theme-dark");
            }
            btnDarkMode.getStyleClass().add("active");
            btnLightMode.getStyleClass().remove("active");
        } else {
            rootPane.getStyleClass().remove("theme-dark");
            btnLightMode.getStyleClass().add("active");
            btnDarkMode.getStyleClass().remove("active");
        }
    }

    public void actualizarPerfilUsuario() {
        upv.ipc.sportlib.User currentUser = SportActivityApp.getInstance().getCurrentUser();

        if (currentUser != null) {
            userNicknameLabel.setText(currentUser.getNickName());

            javafx.scene.image.Image avatar = currentUser.getAvatar();
            if (avatar != null && !avatar.isError()) {
                userAvatarImage.setImage(avatar);
                javafx.scene.shape.Circle clip = new javafx.scene.shape.Circle(12, 12, 12);
                userAvatarImage.setClip(clip);
                userAvatarImage.setVisible(true);
                userAvatarImage.setManaged(true);
                defaultUserIcon.setVisible(false);
                defaultUserIcon.setManaged(false);
                return;
            }
        }

        userAvatarImage.setVisible(false);
        userAvatarImage.setManaged(false);
        defaultUserIcon.setVisible(true);
        defaultUserIcon.setManaged(true);
    }

    public void mostrarDetalleActividad(upv.ipc.sportlib.Activity activity) {
        mostrarShell();
        detailReturnViewPath = rootPane.getCenter() == dashboardViewCache ? "DashboardView.fxml" : currentViewPath;
        setActiveRail(btnNavActividades);

        try {
            FXMLLoader detailLoader = new FXMLLoader(getClass().getResource("ActivityDetailPanel.fxml"));
            Parent panelDetalle = detailLoader.load();
            ActivityDetailPanelController detailController = detailLoader.getController();

            FXMLLoader mapLoader = new FXMLLoader(getClass().getResource("MapView.fxml"));
            Pane mapView = mapLoader.load();
            MapViewController mapController = mapLoader.getController();

            if (activity != null) {
                mapController.setActivity(activity);
                anotacionesManager.setActivity(activity);
                detailController.setActivity(activity);
            }
            anotacionesManager.setMapController(mapController);

            detailController.setMapController(mapController);
            detailController.setMapNode(mapView);

            rootPane.setCenter(panelDetalle);
            currentViewPath = "ActivityDetailPanel.fxml";
        } catch (Exception e) {
            e.printStackTrace();
        }
        Platform.runLater(rootPane::requestFocus);
    }

    public void volverAPantallaAnteriorAlDetalle() {
        if ("ActividadesView.fxml".equals(detailReturnViewPath)) {
            cargarVista("ActividadesView.fxml");
        } else {
            mostrarPantallaPrincipal();
        }
    }

    private void actualizarEstadoBotones(javafx.scene.control.Button botonActivo) {
        java.util.List<javafx.scene.control.Button> botones = java.util.Arrays.asList(
            btnNavResumen, btnNavActividades, btnNavPerfil, btnNavHistorial
        );

        for (javafx.scene.control.Button btn : botones) {
            if (btn != null) {
                if (btn == botonActivo) {
                    if (!btn.getStyleClass().contains("active")) {
                        btn.getStyleClass().add("active");
                    }
                } else {
                    btn.getStyleClass().remove("active");
                }
            }
        }
    }
}
