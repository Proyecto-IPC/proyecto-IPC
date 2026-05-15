package mapademo;

import java.net.URL;
import java.time.Duration;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import upv.ipc.sportlib.Session;
import upv.ipc.sportlib.SportActivityApp;
import upv.ipc.sportlib.User;

public class HistorialSesionesViewController implements Initializable {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm");

    @FXML private ListView<Session> sessionsList;
    @FXML private VBox emptyState;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        sessionsList.setCellFactory(list -> new SessionCell());
        sessionsList.setMouseTransparent(true);
        sessionsList.setFocusTraversable(false);
        loadSessions();
    }

    private void loadSessions() {
        User user = SportActivityApp.getInstance().getCurrentUser();
        List<Session> sessions = user == null ? List.of() : user.getSessions();

        sessionsList.getItems().setAll(sessions);
        boolean isEmpty = sessions.isEmpty();
        emptyState.setVisible(isEmpty);
        emptyState.setManaged(isEmpty);
        sessionsList.setVisible(!isEmpty);
        sessionsList.setManaged(!isEmpty);
    }

    private static class SessionCell extends ListCell<Session> {
        private HBox root;
        private Label lblTitulo;
        private Label lblSubtitulo;
        private Label lblImp, lblVis, lblAno;

        public SessionCell() {
            super();
            root = new HBox(12);
            root.setAlignment(Pos.CENTER_LEFT);
            root.setPadding(new javafx.geometry.Insets(4, 0, 4, 0)); 

            Region marker = new Region();
            marker.getStyleClass().add("activity-row-marker");

            VBox textContainer = new VBox(6);
            HBox.setHgrow(textContainer, Priority.ALWAYS);

            lblTitulo = new Label();
            lblTitulo.getStyleClass().add("activity-row-title");

            lblSubtitulo = new Label();
            lblSubtitulo.getStyleClass().add("muted-label");

            HBox chipContainer = new HBox(8);
            lblImp = new Label(); lblImp.getStyleClass().add("activity-chip");
            lblVis = new Label(); lblVis.getStyleClass().add("activity-chip");
            lblAno = new Label(); lblAno.getStyleClass().add("activity-chip");

            chipContainer.getChildren().addAll(lblImp, lblVis, lblAno);
            textContainer.getChildren().addAll(lblTitulo, lblSubtitulo, chipContainer);
            
            root.getChildren().addAll(marker, textContainer);
        }

        @Override
        protected void updateItem(Session session, boolean empty) {
            super.updateItem(session, empty);

            if (empty || session == null) {
                setGraphic(null);
                setText(null);
                getStyleClass().remove("history-cell");
            } else {
                if (!getStyleClass().contains("history-cell")) {
                    getStyleClass().add("history-cell");
                }

                String fecha = session.getStartTime() != null ? session.getStartTime().format(DATE_FORMAT) : "Desconocida";
                String horaInicio = session.getStartTime() != null ? session.getStartTime().format(TIME_FORMAT) : "--:--";
                String horaFin = session.getEndTime() != null ? session.getEndTime().format(TIME_FORMAT) : "Activa";

                lblTitulo.setText("Sesión del " + fecha);
                lblSubtitulo.setText("De " + horaInicio + " a " + horaFin + " · Duración: " + formatDuration(session.getDuration()));
                
                lblImp.setText(session.getImportedActivities() + " importaciones");
                lblVis.setText(session.getViewedActivities() + " vistas");
                lblAno.setText(session.getAnnotationsCreated() + " anotaciones");

                setGraphic(root);
                setText(null);
            }
        }

        private static String formatDuration(Duration duration) {
            if (duration == null || duration.isNegative()) return "N/A";
            long hours = duration.toHours();
            long minutes = duration.toMinutesPart();
            long seconds = duration.toSecondsPart();
            if (hours > 0) return hours + "h " + minutes + "min";
            if (minutes > 0) return minutes + "min " + seconds + "s";
            return seconds + "s";
        }
    }
}