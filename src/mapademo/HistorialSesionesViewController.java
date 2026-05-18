package mapademo;

import java.net.URL;
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
        private HistorialCellController controller;
        private javafx.scene.Node view;

        public SessionCell() {
            super();
            try {
                javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("HistorialCellView.fxml"));
                view = loader.load();
                controller = loader.getController();
            } catch (Exception e) {
                e.printStackTrace();
            }
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

                controller.setSessionData(session);
                setGraphic(view);
                setText(null);
            }
        }
    }
}