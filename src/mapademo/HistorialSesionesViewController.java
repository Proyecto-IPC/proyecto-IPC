package mapademo;

import java.net.URL;
import java.time.Duration;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.VBox;
import upv.ipc.sportlib.Session;
import upv.ipc.sportlib.SportActivityApp;
import upv.ipc.sportlib.User;

public class HistorialSesionesViewController implements Initializable {

    private static final DateTimeFormatter DATE_FORMAT =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    @FXML private ListView<Session> sessionsList;
    @FXML private VBox emptyState;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        sessionsList.setCellFactory(list -> new SessionCell());
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
        @Override
        protected void updateItem(Session session, boolean empty) {
            super.updateItem(session, empty);

            if (empty || session == null) {
                setText(null);
                getStyleClass().remove("history-cell");
                return;
            }

            if (!getStyleClass().contains("history-cell")) {
                getStyleClass().add("history-cell");
            }

            setText(formatSession(session));
        }

        private static String formatSession(Session session) {
            String start = session.getStartTime() == null
                    ? "Inicio no disponible"
                    : session.getStartTime().format(DATE_FORMAT);
            String end = session.getEndTime() == null
                    ? "Sesión abierta"
                    : session.getEndTime().format(DATE_FORMAT);

            return start
                    + "\nFin: " + end
                    + " · Duración: " + formatDuration(session.getDuration())
                    + "\nImportadas: " + session.getImportedActivities()
                    + " · Vistas: " + session.getViewedActivities()
                    + " · Anotaciones: " + session.getAnnotationsCreated();
        }

        private static String formatDuration(Duration duration) {
            if (duration == null || duration.isNegative()) {
                return "no disponible";
            }

            long hours = duration.toHours();
            long minutes = duration.toMinutesPart();
            long seconds = duration.toSecondsPart();

            if (hours > 0) {
                return hours + " h " + minutes + " min";
            }
            if (minutes > 0) {
                return minutes + " min " + seconds + " s";
            }
            return seconds + " s";
        }
    }
}
