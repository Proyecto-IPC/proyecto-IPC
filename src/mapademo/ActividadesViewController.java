package mapademo;

import java.io.File;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import upv.ipc.sportlib.Activity;
import upv.ipc.sportlib.SportActivityApp;

public class ActividadesViewController implements Initializable {

    @FXML private VBox listaActividades;
    @FXML private VBox emptyState;
    @FXML private Button btnImportar;
    @FXML private Label lblEstado;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Al abrir la pantalla, carga las actividades que ya tiene el usuario
        cargarActividades();
    }

    // ─── Carga la lista de actividades del usuario actual ───────────────────
    private void cargarActividades() {
        listaActividades.getChildren().clear();

        List<Activity> actividades = SportActivityApp.getInstance()
                                        .getCurrentUser()
                                        .getActivities();

        if (actividades == null || actividades.isEmpty()) {
            // Muestra el mensaje de "sin actividades"
            emptyState.setVisible(true);
            emptyState.setManaged(true);
        } else {
            // Oculta el estado vacío y muestra cada actividad
            emptyState.setVisible(false);
            emptyState.setManaged(false);
            for (Activity act : actividades) {
                listaActividades.getChildren().add(crearFilaActividad(act));
            }
        }
    }

    // ─── Cuando el usuario pulsa "Importar GPX" ─────────────────────────────
    @FXML
    private void handleImportar() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Seleccionar archivo GPX");
        chooser.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("Archivos GPX", "*.gpx")
        );

        File archivo = chooser.showOpenDialog(btnImportar.getScene().getWindow());

        if (archivo != null) {
            try {
                // ✅ Correcto: recibe File, no String
                Activity nueva = SportActivityApp.getInstance()
                                    .importActivity(archivo);

                if (nueva != null) {
                    lblEstado.setText("✓ Importado: " + nueva.getName());
                    cargarActividades(); // Recarga la lista
                } else {
                    lblEstado.setText("✗ No se pudo importar el archivo");
                }
            } catch (Exception e) {
                lblEstado.setText("✗ Error: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    // ─── Crea visualmente una fila para cada actividad ───────────────────────
    private HBox crearFilaActividad(Activity act) {
        HBox fila = new HBox(10);
        fila.getStyleClass().add("activity-row-placeholder");
        fila.setAlignment(Pos.CENTER_LEFT);
        fila.setFocusTraversable(false); // ← AÑADE ESTA LÍNEA

        // Marcador de color a la izquierda
        Region marker = new Region();
        marker.getStyleClass().add("activity-row-marker");

        // Contenido de texto
        VBox body = new VBox(4);
        HBox.setHgrow(body, Priority.ALWAYS);

        Label nombre = new Label(act.getName() != null ? act.getName() : "Actividad sin nombre");
        nombre.getStyleClass().add("activity-row-title");

        // Chips con distancia, tiempo y ritmo
        HBox chips = new HBox(6);
        chips.getChildren().addAll(
            crearChip(String.format("%.1f km", act.getTotalDistance() / 1000.0)),
            crearChip(formatearTiempo(act.getDuration().toSeconds())),
            crearChip(String.format("%d m desnivel", (int) act.getElevationGain()))
        );

        body.getChildren().addAll(nombre, chips);
        fila.getChildren().addAll(marker, body);

        // Al hacer clic en una actividad, navega al mapa con esa actividad
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

    // Convierte segundos a "1h 23min" o "45min"
    private String formatearTiempo(long segundos) {
        long horas = segundos / 3600;
        long minutos = (segundos % 3600) / 60;
        if (horas > 0) return horas + "h " + minutos + "min";
        return minutos + "min";
    }
}