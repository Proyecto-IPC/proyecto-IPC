package mapademo;

import java.time.Duration;
import java.time.format.DateTimeFormatter;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import upv.ipc.sportlib.Session;

public class HistorialCellController {
    @FXML private HBox root;
    @FXML private Label lblTitulo;
    @FXML private Label lblSubtitulo;
    @FXML private Label lblImp;
    @FXML private Label lblVis;
    @FXML private Label lblAno;

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm");

    public void setSessionData(Session session) {
        String fecha = session.getStartTime() != null ? session.getStartTime().format(DATE_FORMAT) : "Desconocida";
        String horaInicio = session.getStartTime() != null ? session.getStartTime().format(TIME_FORMAT) : "--:--";
        String horaFin = session.getEndTime() != null ? session.getEndTime().format(TIME_FORMAT) : "Activa";

        lblTitulo.setText("Sesión del " + fecha);
        lblSubtitulo.setText("De " + horaInicio + " a " + horaFin + " · Duración: " + formatDuration(session.getDuration()));

        lblImp.setText(session.getImportedActivities() + " importaciones");
        lblVis.setText(session.getViewedActivities() + " vistas");
        lblAno.setText(session.getAnnotationsCreated() + " anotaciones");
    }

    public HBox getRoot() {
        return root;
    }

    private String formatDuration(Duration duration) {
        if (duration == null || duration.isNegative()) return "N/A";
        long hours = duration.toHours();
        long minutes = duration.toMinutesPart();
        long seconds = duration.toSecondsPart();
        if (hours > 0) return hours + "h " + minutes + "min";
        if (minutes > 0) return minutes + "min " + seconds + "s";
        return seconds + "s";
    }
}