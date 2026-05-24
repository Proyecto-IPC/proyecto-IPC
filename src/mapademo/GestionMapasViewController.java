package mapademo;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.stream.Collectors;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import upv.ipc.sportlib.MapRegion;
import upv.ipc.sportlib.SportActivityApp;

public class GestionMapasViewController implements Initializable {

    @FXML private VBox mapsTableBody;
    @FXML private VBox emptyMapsState;
    @FXML private Button btnAddMap;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        AnimationBehavior.installHover(btnAddMap);
        cargarMapas();
    }

    private void cargarMapas() {
        mapsTableBody.getChildren().clear();
        List<MapRegion> mapas = SportActivityApp.getInstance().getMapRegions();
        Set<Long> unusedMapIds = SportActivityApp.getInstance().getUnusedMapRegions().stream()
                .map(MapRegion::getId)
                .collect(Collectors.toSet());

        boolean empty = mapas.isEmpty();
        emptyMapsState.setVisible(empty);
        emptyMapsState.setManaged(empty);

        for (MapRegion mapa : mapas) {
            mapsTableBody.getChildren().add(crearFilaMapa(mapa, unusedMapIds.contains(mapa.getId())));
        }
    }

    private HBox crearFilaMapa(MapRegion mapa, boolean canDelete) {
        HBox row = new HBox(12);
        row.setAlignment(Pos.CENTER_LEFT);
        row.getStyleClass().add("map-table-row");

        Label name = new Label(mapa.getName());
        name.getStyleClass().add("map-table-name");
        name.setMinWidth(190);
        name.setPrefWidth(190);

        Label path = new Label(mapa.getImagePath());
        path.getStyleClass().add("map-table-path");
        path.setMaxWidth(Double.MAX_VALUE);
        path.setTooltip(new Tooltip(mapa.getImagePath()));
        HBox.setHgrow(path, Priority.ALWAYS);

        HBox actionBox = new HBox(6, crearBotonEliminar(mapa, canDelete));
        actionBox.setAlignment(Pos.CENTER);
        actionBox.setMinWidth(96);
        actionBox.setPrefWidth(96);

        row.getChildren().addAll(name, path, actionBox);
        return row;
    }

    private Button crearBotonEliminar(MapRegion mapa, boolean canDelete) {
        Button button = new Button();
        button.getStyleClass().addAll("activity-action-button", "activity-action-danger");
        if (!canDelete) {
            button.getStyleClass().add("map-action-unavailable");
        }
        button.setTooltip(new Tooltip(canDelete ? "Eliminar mapa" : "No se puede eliminar: mapa usado por una actividad"));
        button.setGraphic(cargarIcono(canDelete ? "/resources/icons/TrashIcon.fxml" : "/resources/icons/TrashSlashIcon.fxml"));
        button.setOnAction(event -> {
            if (canDelete) {
                eliminarMapa(mapa);
            }
            event.consume();
        });
        return button;
    }

    private Node cargarIcono(String iconPath) {
        try {
            return FXMLLoader.load(getClass().getResource(iconPath));
        } catch (Exception e) {
            return new Region();
        }
    }

    private void eliminarMapa(MapRegion mapa) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Eliminar mapa");
        confirm.setHeaderText(null);
        confirm.setContentText("¿Eliminar \"" + mapa.getName() + "\"?");
        ButtonType cancel = new ButtonType("Cancelar", ButtonBar.ButtonData.LEFT);
        ButtonType delete = new ButtonType("Eliminar", ButtonBar.ButtonData.OK_DONE);
        confirm.getButtonTypes().setAll(cancel, delete);
        aplicarEstiloDialogo(confirm);
        confirm.getDialogPane().getStyleClass().add("logout-confirm-dialog");
        confirm.getDialogPane().lookupButton(cancel).getStyleClass().add("logout-cancel-button");
        confirm.getDialogPane().lookupButton(delete).getStyleClass().add("logout-danger-button");

        confirm.showAndWait().ifPresent(result -> {
            if (result == delete) {
                boolean removed = SportActivityApp.getInstance().removeMapRegion(mapa);
                if (removed) {
                    cargarMapas();
                } else {
                    mostrarAlerta("No se puede eliminar", "Este mapa está siendo usado por alguna actividad.");
                }
            }
        });
    }

    @FXML
    private void handleAddMap(ActionEvent event) {
        MainViewController.getInstancia().cargarVista("AñadirMapaView.fxml");
    }

    private void mostrarAlerta(String titulo, String mensaje) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        aplicarEstiloDialogo(alert);
        alert.showAndWait();
    }

    private void aplicarEstiloDialogo(Alert alert) {
        URL cssUrl = getClass().getResource("/resources/style.css");
        if (cssUrl != null) {
            alert.getDialogPane().getStylesheets().add(cssUrl.toExternalForm());
        }
    }

}
