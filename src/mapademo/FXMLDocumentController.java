package mapademo;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Point2D;
import javafx.scene.Group;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Slider;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Duration;

/**
 * Legacy/demo controller for FXMLDocument.fxml.
 *
 * The main application flow uses MainView.fxml and MapView.fxml. This class is
 * kept only as the original IPC map/POI demo so it does not contaminate the
 * product UI or the global style.css system.
 */
public class FXMLDocumentController implements Initializable {

    private Group zoomGroup;
    private Pane mapPane;
    private ContextMenu mapContextMenu;
    private boolean insertionMode = false;

    @FXML private ListView<Poi> map_listview;
    @FXML private ScrollPane map_scrollpane;
    @FXML private Slider zoom_slider;
    @FXML private Label mousePosition;
    @FXML private SplitPane splitPane;

    @FXML
    void zoomIn(ActionEvent event) {
        zoom_slider.setValue(zoom_slider.getValue() + 0.1);
    }

    @FXML
    void zoomOut(ActionEvent event) {
        zoom_slider.setValue(zoom_slider.getValue() - 0.1);
    }

    private void zoom(double scaleValue) {
        double scrollH = map_scrollpane.getHvalue();
        double scrollV = map_scrollpane.getVvalue();

        zoomGroup.setScaleX(scaleValue);
        zoomGroup.setScaleY(scaleValue);

        map_scrollpane.setHvalue(scrollH);
        map_scrollpane.setVvalue(scrollV);
    }

    @FXML
    void listClicked(MouseEvent event) {
        Poi itemSelected = map_listview.getSelectionModel().getSelectedItem();
        if (itemSelected == null) {
            return;
        }

        double mapWidth = mapPane.getWidth() * zoomGroup.getScaleX();
        double mapHeight = mapPane.getHeight() * zoomGroup.getScaleY();
        double poiX = itemSelected.getPosition().getX() * zoomGroup.getScaleX();
        double poiY = itemSelected.getPosition().getY() * zoomGroup.getScaleY();

        double viewW = map_scrollpane.getViewportBounds().getWidth();
        double viewH = map_scrollpane.getViewportBounds().getHeight();

        double scrollH = (poiX - viewW / 2) / (mapWidth - viewW);
        double scrollV = (poiY - viewH / 2) / (mapHeight - viewH);

        scrollH = Math.max(0, Math.min(1, scrollH));
        scrollV = Math.max(0, Math.min(1, scrollV));

        Timeline timeline = new Timeline();
        timeline.getKeyFrames().add(new KeyFrame(
                Duration.millis(500),
                new KeyValue(map_scrollpane.hvalueProperty(), scrollH),
                new KeyValue(map_scrollpane.vvalueProperty(), scrollV)));
        timeline.play();
    }

    private void buildMap(File imgFile) {
        if (!imgFile.exists()) {
            map_scrollpane.setContent(new Label("Imagen no encontrada: " + imgFile.getPath()));
            return;
        }

        Image img = new Image(imgFile.toURI().toString());
        double width = img.getWidth();
        double height = img.getHeight();

        mapPane = new Pane();
        mapPane.setPrefSize(width, height);
        mapPane.setMinSize(width, height);
        mapPane.setMaxSize(width, height);

        ImageView imageView = new ImageView(img);
        imageView.setFitWidth(width);
        imageView.setFitHeight(height);
        mapPane.getChildren().add(imageView);

        mapPane.setOnMouseClicked(event -> {
            if (event.getButton() == MouseButton.SECONDARY) {
                onMapRightClick(event.getX(), event.getY());
            } else if (event.getButton() == MouseButton.PRIMARY && insertionMode) {
                insertionMode = false;
                mapPane.setStyle("");
                addPoi(event.getX(), event.getY());
            }
        });

        zoomGroup = new Group();
        Group contentGroup = new Group();
        zoomGroup.getChildren().add(mapPane);
        contentGroup.getChildren().add(zoomGroup);

        double zoom = zoom_slider.getValue();
        zoomGroup.setScaleX(zoom);
        zoomGroup.setScaleY(zoom);

        map_scrollpane.setContent(contentGroup);
    }

    private void onMapRightClick(double x, double y) {
        mapContextMenu.hide();

        mapContextMenu.getItems().get(0).setOnAction(event -> addPoi(x, y));
        mapContextMenu.getItems().get(1).setOnAction(event -> addCircle(x, y));

        mapContextMenu.show(
                mapPane.getScene().getWindow(),
                mapPane.localToScreen(x, y).getX(),
                mapPane.localToScreen(x, y).getY());
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        zoom_slider.setMin(0.5);
        zoom_slider.setMax(1.5);
        zoom_slider.setValue(1.0);
        zoom_slider.valueProperty().addListener(
                (observable, oldVal, newVal) -> zoom((Double) newVal));

        MenuItem miText = new MenuItem("Añadir texto");
        MenuItem miCircle = new MenuItem("Añadir circulo");
        mapContextMenu = new ContextMenu(miText, miCircle);

        map_listview.setCellFactory(listView -> new ListCell<Poi>() {
            @Override
            protected void updateItem(Poi poi, boolean empty) {
                super.updateItem(poi, empty);

                if (empty || poi == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    setText(poi.getCode() + " - " + poi.getPosition());
                }
            }
        });

        buildMap(new File("src/resources/upv.jpg"));
    }

    @FXML
    private void showPosition(MouseEvent event) {
        mousePosition.setText(
                "sceneX: " + (int) event.getSceneX()
                + ", sceneY: " + (int) event.getSceneY() + "\n"
                + "         X: " + (int) event.getX()
                + ",          Y: " + (int) event.getY());
    }

    @FXML
    private void about(ActionEvent event) {
        Alert mensaje = new Alert(Alert.AlertType.INFORMATION);

        Stage dialogStage = (Stage) mensaje.getDialogPane().getScene().getWindow();
        dialogStage.getIcons().add(
                new Image(getClass().getResourceAsStream("/resources/logo.png")));

        mensaje.setTitle("Acerca de");
        mensaje.setHeaderText("IPC - 2026");
        mensaje.showAndWait();
    }

    private void addPoi(double x, double y) {
        Dialog<Poi> poiDialog = new Dialog<>();
        poiDialog.setTitle("Nuevo POI");
        poiDialog.setHeaderText("Introduce un nuevo POI");

        Stage dialogStage = (Stage) poiDialog.getDialogPane().getScene().getWindow();
        dialogStage.getIcons().add(
                new Image(getClass().getResourceAsStream("/resources/logo.png")));

        ButtonType okButton = new ButtonType("Aceptar", ButtonBar.ButtonData.OK_DONE);
        poiDialog.getDialogPane().getButtonTypes().addAll(okButton, ButtonType.CANCEL);

        TextField nameField = new TextField();
        nameField.setPromptText("Nombre del POI");

        VBox vbox = new VBox(10, new Label("Nombre:"), nameField);
        poiDialog.getDialogPane().setContent(vbox);

        poiDialog.setResultConverter(dialogButton -> {
            if (dialogButton == okButton) {
                return new Poi(nameField.getText().trim(), x, y);
            }
            return null;
        });

        Optional<Poi> result = poiDialog.showAndWait();
        if (result.isPresent()) {
            Poi poi = result.get();
            poi.setPosition(new Point2D(x, y));
            map_listview.getItems().add(poi);

            Text text = new Text(poi.getCode());
            text.setX(x);
            text.setY(y);
            mapPane.getChildren().add(text);
        }
    }

    @FXML
    private void cambiarMapa(ActionEvent event) throws IOException {
        FileChooser fc = new FileChooser();
        fc.setInitialDirectory(new File("."));

        File imgFile = fc.showOpenDialog(zoom_slider.getScene().getWindow());
        if (imgFile != null) {
            System.out.println("Mapa seleccionado: " + imgFile.getCanonicalPath());
            buildMap(imgFile);
            map_listview.getItems().clear();
        }
    }

    private void addCircle(double x, double y) {
        Circle circle = new Circle(10, Color.web("#b9352f"));
        circle.setCenterX(x);
        circle.setCenterY(y);
        mapPane.getChildren().add(circle);
    }
}
