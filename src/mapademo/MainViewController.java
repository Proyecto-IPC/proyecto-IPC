package mapademo;

import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

public class MainViewController implements Initializable {

    @FXML private BorderPane rootPane;
    @FXML private HBox topBar;
    @FXML private VBox sideBar;
    @FXML private Label lblTopTitle;
    @FXML private Label lblTopContext;
    @FXML private Button btnImportar;
    @FXML private Button btnDashboard;
    @FXML private Button btnMapa;
    @FXML private Button btnPerfil;
    @FXML private Button btnHistorial;

    private static MainViewController instancia;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        instancia = this;
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
                rootPane.setCenter(vista);
            } else {
                mostrarShell();
                rootPane.setCenter(vista);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void mostrarShellInicial() {
        mostrarDashboard();
    }

    @FXML
    private void handleDashboard() {
        mostrarDashboard();
    }

    @FXML
    private void handleMapa() {
        cargarVistaShell("MapView.fxml", "Mapa", "Revisa la ruta y los controles de zoom", btnMapa);
    }

    @FXML
    private void handlePerfil() {
        cargarVistaShell("ProfileView.fxml", "Perfil", "Cuenta activa y cierre de sesión", btnPerfil);
    }

    @FXML
    private void handleHistorial() {
        mostrarShell();
        activar(btnHistorial);
        actualizarTopbar("Historial", "Sesiones y acceso reciente");
        rootPane.setCenter(crearPlaceholder(
                "Historial de sesiones",
                "Aquí aparecerán tus sesiones cuando el historial esté conectado.",
                "Pantalla base del rol Guardián. No invade actividades ni estadísticas reales."));
    }

    private void mostrarDashboard() {
        mostrarShell();
        activar(btnDashboard);
        actualizarTopbar("Dashboard", "Resumen inicial y acceso rápido al mapa");
        rootPane.setCenter(crearPlaceholder(
                "Bienvenido a Running La Safor",
                "Selecciona Mapa para revisar rutas o Perfil para ver tu cuenta.",
                "La importación y las estadísticas reales quedan reservadas para sus roles."));
    }

    private void cargarVistaShell(String fxmlPath, String titulo, String contexto, Button activo) {
        mostrarShell();
        activar(activo);
        actualizarTopbar(titulo, contexto);
        cargarVista(fxmlPath);
    }

    private VBox crearPlaceholder(String titulo, String texto, String detalle) {
        Label title = new Label(titulo);
        title.getStyleClass().add("page-title");

        Label body = new Label(texto);
        body.getStyleClass().add("placeholder-text");
        body.setWrapText(true);

        Label note = new Label(detalle);
        note.getStyleClass().add("muted-label");
        note.setWrapText(true);

        VBox box = new VBox(12, title, body, note);
        box.setAlignment(Pos.CENTER_LEFT);
        box.setMaxWidth(460);
        box.setMaxHeight(Region.USE_PREF_SIZE);
        box.getStyleClass().add("shell-placeholder");
        BorderPane.setAlignment(box, Pos.CENTER);
        return box;
    }

    private boolean esVistaAuth(String fxmlPath) {
        return "LoginView.fxml".equals(fxmlPath) || "RegisterView.fxml".equals(fxmlPath);
    }

    private void mostrarShell() {
        rootPane.setTop(topBar);
        rootPane.setLeft(sideBar);
    }

    private void ocultarShell() {
        rootPane.setTop(null);
        rootPane.setLeft(null);
    }

    private void actualizarTopbar(String titulo, String contexto) {
        lblTopTitle.setText(titulo);
        lblTopContext.setText(contexto);
    }

    private void activar(Button activo) {
        List<Button> botones = Arrays.asList(btnDashboard, btnMapa, btnPerfil, btnHistorial);
        for (Button boton : botones) {
            boton.getStyleClass().remove("active");
        }
        if (!activo.getStyleClass().contains("active")) {
            activo.getStyleClass().add("active");
        }
    }
}
