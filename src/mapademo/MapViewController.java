/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/javafx/FXMLController.java to edit this template
 */
package mapademo;

import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.function.Consumer;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Bounds; // Para zoom centrado
import javafx.geometry.Point2D;
import javafx.scene.shape.Polyline;
import javafx.scene.shape.Circle;
import javafx.scene.paint.Color;
import javafx.scene.Group;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image; // Para la imagen del mapa
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.input.ScrollEvent;
import javafx.scene.input.MouseButton;
import upv.ipc.sportlib.Activity;
import upv.ipc.sportlib.MapProjection;
import upv.ipc.sportlib.MapRegion;
import upv.ipc.sportlib.TrackPoint;
import upv.ipc.sportlib.GeoPoint;


/**
 * FXML Controller class
 *
 * @author david
 */
public class MapViewController implements Initializable {

    @FXML
    private StackPane mapArea;
    @FXML
    private ScrollPane mapScrollPane;
    @FXML
    private Group contentGroup;
    @FXML
    private Group zoomGroup;
    @FXML
    private Pane mapPane;
    @FXML
    private ImageView mapImageView;
    @FXML
    private VBox zoomControls;
    @FXML
    private Button zoomInButton;
    @FXML
    private Button zoomOutButton;
    @FXML
    private Button centerRouteButton;
    @FXML
    private Label emptyStateLabel;
    @FXML
    private VBox zoomButtonGroup;
    
    private double zoomLevel = 1.0;
    private static final String EMPTY_STATE_MESSAGE = "Selecciona una actividad para ver su recorrido";
    private static final String MAP_LOAD_ERROR_MESSAGE = "No se pudo cargar el mapa de la actividad";
    private static final double DEFAULT_MIN_ZOOM = 0.5;
    private static final double MAX_ZOOM = 2.5;
    private static final double ZOOM_STEP = 0.15;
    private static final double WHEEL_ZOOM_STEP = 0.025;
    private static final Color ROUTE_COLOR = Color.BLUE;
    private static final Color START_COLOR = Color.GREEN;
    private static final Color END_COLOR = Color.RED;
    private static final Color MARKER_BORDER_COLOR = Color.WHITE;
    private static final Color HIGHLIGHT_COLOR = Color.YELLOW;
    private static final Color HIGHLIGHT_BORDER_COLOR = Color.BLACK;
    private static final double ROUTE_WIDTH = 4.0;
    private static final double MARKER_RADIUS = 7.0;
    private static final double MARKER_BORDER_WIDTH = 2.0;
    private static final double HIGHLIGHT_RADIUS = 9.0;
    private static final double HIGHLIGHT_BORDER_WIDTH = 2.0;
    
    private Activity currentActivity;
    private MapProjection projection;
    private Bounds routeBounds;
    private Circle highlightedTrackPoint;
    private Consumer<GeoPoint> mapSecondaryClickHandler;

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        centerRouteButton.setDisable(true);
        
        zoomInButton.setOnAction(event -> zoomIn());
        zoomOutButton.setOnAction(event -> zoomOut());
        centerRouteButton.setOnAction(event -> centerRoute());
        
        mapScrollPane.addEventFilter(ScrollEvent.SCROLL, event -> {
            if (event.getDeltaY() > 0) {
                setZoomAt(zoomLevel + WHEEL_ZOOM_STEP, event.getX(), event.getY());
            } else if (event.getDeltaY() < 0) {
                setZoomAt(zoomLevel - WHEEL_ZOOM_STEP, event.getX(), event.getY());
            }
            
            event.consume();
        });
        
        mapPane.setOnMouseClicked(event -> {
            if (event.getButton() == MouseButton.SECONDARY) {
                handleMapSecondaryClick(event.getX(), event.getY());
            }
        });
        
        setZoom(1.0);
    }
    
    private void zoomIn(double step) {
        setZoom(zoomLevel + step);
    }
    
    private void zoomOut(double step) {
        setZoom(zoomLevel - step);
    }
    
    private void zoomIn() {
        zoomIn(ZOOM_STEP);
    }
        
    private void zoomOut() {
        zoomOut(ZOOM_STEP);
    }
    
    private void setZoom(double zoom) {
        // El zoom se mantiene entre sus limites
        double newZoom = Math.max(getMinZoom(), Math.min(MAX_ZOOM, zoom));
        
        // Rectangulo visible del ScrollPane
        Bounds viewportBounds = mapScrollPane.getViewportBounds();
        // Tamanyo total del contendido dentro del ScrollPane (mapa escalado)
        Bounds contentBounds = contentGroup.getBoundsInLocal();
        
        // Obtiene las dimensiones visibles y las del contenido
        double viewportWidth = viewportBounds.getWidth();
        double viewportHeight = viewportBounds.getHeight();
        double contentWidth = contentBounds.getWidth();
        double contentHeight = contentBounds.getHeight();
        
        // Calcular el centro visible del ScrollPane
        double centerX = mapScrollPane.getHvalue() * Math.max(contentWidth - viewportWidth, 0) + viewportWidth / 2;
        double centerY = mapScrollPane.getVvalue() * Math.max(contentHeight - viewportHeight, 0) + viewportHeight / 2;
        
        // Ajusta el zoom solicitado a los limites permitidos
        zoomLevel = newZoom;
        zoomGroup.setScaleX(zoomLevel);
        zoomGroup.setScaleY(zoomLevel);
        
        // Mide el nuevo tamanyo del contenido tras el zoom
        Bounds newContentBounds = contentGroup.getBoundsInLocal();
        double newContentWidth = newContentBounds.getWidth();
        double newContentHeight = newContentBounds.getHeight();
        
        // Calcula la nueva posicion del scroll (H y V)
        double newH = (centerX * (newContentWidth / contentWidth) - viewportWidth / 2) / Math.max(newContentWidth - viewportWidth, 1);
        double newV = (centerY * (newContentHeight / contentHeight) - viewportHeight / 2) / Math.max(newContentHeight - viewportHeight, 1);
        
        // Aplica el nuevo scroll
        mapScrollPane.setHvalue(clamp(newH, 0, 1));
        mapScrollPane.setVvalue(clamp(newV, 0, 1));
    }
    
    private void setZoomAt(double zoom, double mouseX, double mouseY) {
        double newZoom = Math.max(getMinZoom(), Math.min(MAX_ZOOM, zoom));

        Bounds viewportBounds = mapScrollPane.getViewportBounds();

        double viewportWidth = viewportBounds.getWidth();
        double viewportHeight = viewportBounds.getHeight();

        double oldContentWidth = contentGroup.getBoundsInLocal().getWidth();
        double oldContentHeight = contentGroup.getBoundsInLocal().getHeight();

        double contentX = mapScrollPane.getHvalue() * Math.max(oldContentWidth - viewportWidth, 0) + mouseX;
        double contentY = mapScrollPane.getVvalue() * Math.max(oldContentHeight - viewportHeight, 0) + mouseY;

        zoomLevel = newZoom;
        zoomGroup.setScaleX(zoomLevel);
        zoomGroup.setScaleY(zoomLevel);

        double newContentWidth = contentGroup.getBoundsInLocal().getWidth();
        double newContentHeight = contentGroup.getBoundsInLocal().getHeight();

        double scaleX = newContentWidth / oldContentWidth;
        double scaleY = newContentHeight / oldContentHeight;

        double newH = (contentX * scaleX - mouseX) / Math.max(newContentWidth - viewportWidth, 1);
        double newV = (contentY * scaleY - mouseY) / Math.max(newContentHeight - viewportHeight, 1);

        mapScrollPane.setHvalue(clamp(newH, 0, 1));
        mapScrollPane.setVvalue(clamp(newV, 0, 1));
    }
    
    // Asegura que nunca se salga de los limites de ScrollPane
    private double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }
    
    private double getMinZoom() {
        double viewportWidth = mapScrollPane.getViewportBounds().getWidth();
        double viewportHeight = mapScrollPane.getViewportBounds().getHeight();

        double mapWidth = mapPane.getWidth();
        double mapHeight = mapPane.getHeight();

        if (viewportWidth <= 0 || viewportHeight <= 0 || mapWidth <= 0 || mapHeight <= 0) {
            return DEFAULT_MIN_ZOOM;
        }

        double fitWidthZoom = viewportWidth / mapWidth;
        double fitHeightZoom = viewportHeight / mapHeight;

        return Math.max(fitWidthZoom, fitHeightZoom);
    }
    
    private void handleMapSecondaryClick(double x, double y) {
        if (projection == null || currentActivity == null) {
            return;
        }
        
        var geoPoint = projection.unproject(x, y);
        
        if (mapSecondaryClickHandler != null) {
            mapSecondaryClickHandler.accept(geoPoint);
        }
    }
    
    public void setActivity(Activity activity) {
        currentActivity = activity;
        clearMap();
        
        if (activity == null) {
            emptyStateLabel.setText(EMPTY_STATE_MESSAGE);
            emptyStateLabel.setVisible(true);
            centerRouteButton.setDisable(true);
            return;
        }
        
        emptyStateLabel.setVisible(false);
        
        if (!loadMapForActivity(activity)) {
            emptyStateLabel.setText(MAP_LOAD_ERROR_MESSAGE);
            emptyStateLabel.setVisible(true);
            centerRouteButton.setDisable(true);
            return;
        }
        
        drawActivity(activity);
        centerRouteButton.setDisable(false);
    }
    
    public void setOnMapSecondaryClick(Consumer<GeoPoint> handler) {
        mapSecondaryClickHandler = handler;
    }
    
    public void highlightTrackPoint(TrackPoint trackPoint) {
        if (trackPoint == null || projection == null) {
            clearHighlightedTrackPoint();
            return;
        }
        
        Point2D point = projection.project(trackPoint);
        
        if (highlightedTrackPoint == null) {
            highlightedTrackPoint = new Circle();
            highlightedTrackPoint.setRadius(HIGHLIGHT_RADIUS);
            highlightedTrackPoint.setFill(HIGHLIGHT_COLOR);
            highlightedTrackPoint.setStroke(HIGHLIGHT_BORDER_COLOR);
            highlightedTrackPoint.setStrokeWidth(HIGHLIGHT_BORDER_WIDTH);
            mapPane.getChildren().add(highlightedTrackPoint);
        }
        
        highlightedTrackPoint.setCenterX(point.getX());
        highlightedTrackPoint.setCenterY(point.getY());
    }
    
    public void clearHighlightedTrackPoint() {
        if (highlightedTrackPoint != null) {
            mapPane.getChildren().remove(highlightedTrackPoint);
            highlightedTrackPoint = null;
        }
    }
    
    private void clearMap() {
        projection = null;
        routeBounds = null;
        highlightedTrackPoint = null;
        mapImageView.setImage(null);
        mapImageView.setFitWidth(0);
        mapImageView.setFitHeight(0);
        // Borra todos los hijos del mapPane y deja unicamente mapImageView
        mapPane.getChildren().setAll(mapImageView);
    }
    
    private boolean loadMapForActivity(Activity activity) {
        MapRegion region = activity.getSuggestedMap();
        if (region == null) {
            return false;
        }
        
        Image mapImage = new Image(new File(region.getImagePath()).toURI().toString());
        if (mapImage.isError() || mapImage.getWidth() <= 0 || mapImage.getHeight() <= 0) {
            return false;
        }
        
        double width = mapImage.getWidth();
        double height = mapImage.getHeight();
        
        mapImageView.setImage(mapImage);
        mapImageView.setFitWidth(width);
        mapImageView.setFitHeight(height);
        
        mapPane.setPrefSize(width, height);
        mapPane.setMinSize(width, height);
        mapPane.setMaxSize(width, height);
        
        projection = new MapProjection(region, width, height);
        return true;
    }
    
    private void drawActivity(Activity activity) {
        if (activity == null || projection == null) {
            return;
        }
        
        Polyline routeLine = new Polyline();
        routeLine.setStrokeWidth(ROUTE_WIDTH);
        routeLine.setStroke(ROUTE_COLOR);
        
        for (var trackPoint : activity.getTrackPoints()) {
            Point2D point = projection.project(trackPoint);
            routeLine.getPoints().addAll(point.getX(), point.getY());
        }
        
        mapPane.getChildren().add(routeLine);
        
        // Guarda el rectangulo que ocupa la ruta
        routeBounds = routeLine.getBoundsInLocal();
        
        drawRouteMarker(activity.getStartPoint(), START_COLOR);
        drawRouteMarker(activity.getEndPoint(), END_COLOR);
    }
    
    private void drawRouteMarker(TrackPoint trackPoint, Color color) {
        if (trackPoint == null || projection == null) {
            return;
        }
        
        Point2D point = projection.project(trackPoint);
        
        Circle marker = new Circle(point.getX(), point.getY(), MARKER_RADIUS);
        marker.setFill(color);
        marker.setStroke(MARKER_BORDER_COLOR);
        marker.setStrokeWidth(MARKER_BORDER_WIDTH);
        
        mapPane.getChildren().add(marker);
    }
    
    private void centerRoute() {
        if (routeBounds == null) {
            return;
        }
        
        double mapWidth = mapPane.getWidth() * zoomLevel;
        double mapHeight = mapPane.getHeight() * zoomLevel;
        
        double viewportWidth = mapScrollPane.getViewportBounds().getWidth();
        double viewportHeight = mapScrollPane.getViewportBounds().getHeight();
        
        double centerX = routeBounds.getCenterX() * zoomLevel;
        double centerY = routeBounds.getCenterY() * zoomLevel;
        
        double h = (centerX - viewportWidth / 2) / Math.max(mapWidth - viewportWidth, 1);
        double v = (centerY - viewportHeight / 2) / Math.max(mapHeight - viewportHeight, 1);
        
        mapScrollPane.setHvalue(clamp(h, 0, 1));
        mapScrollPane.setVvalue(clamp(v, 0, 1));
    }
    
}
