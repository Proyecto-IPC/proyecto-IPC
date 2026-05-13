/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/javafx/FXMLController.java to edit this template
 */
package mapademo;

import java.io.File;
import java.net.URL;
import javafx.animation.PauseTransition;
import javafx.animation.Timeline;
import javafx.animation.Interpolator;
import javafx.application.Platform;
import java.util.ResourceBundle;
import java.util.function.Consumer;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
import javafx.scene.CacheHint;
import javafx.scene.shape.Polyline;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.paint.Color;
import javafx.scene.Group;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.control.Label;
import javafx.scene.text.Text;
import javafx.scene.AccessibleRole;
import javafx.scene.input.ScrollEvent;
import javafx.scene.input.MouseButton;
import javafx.util.Duration;
import upv.ipc.sportlib.Activity;
import upv.ipc.sportlib.Annotation;
import upv.ipc.sportlib.AnnotationType;
import upv.ipc.sportlib.MapProjection;
import upv.ipc.sportlib.MapRegion;
import upv.ipc.sportlib.TrackPoint;
import upv.ipc.sportlib.GeoPoint;
import java.util.List;


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
    private VBox emptyStatePanel;
    @FXML
    private Label zoomFeedbackLabel;
    @FXML
    private Label pendingToastLabel;

    private double zoomLevel = 1.0;
    private boolean mapLoaded;
    private PauseTransition zoomFeedbackTimer;
    private static final String EMPTY_STATE_MESSAGE = "Selecciona o importa una actividad para ver su recorrido sobre el mapa.";
    private static final String MAP_LOAD_ERROR_MESSAGE = "No se pudo cargar el mapa de la actividad. Revisa que exista un mapa compatible.";
    private static final double DEFAULT_MIN_ZOOM = 0.5;
    private static final double MAX_ZOOM = 2.5;
    private static final double ZOOM_STEP = 0.15;
    private static final double WHEEL_ZOOM_STEP = 0.025;
    private static final Color ROUTE_COLOR = Color.web("#2563eb");
    private static final Color START_COLOR = Color.web("#168a57");
    private static final Color END_COLOR = Color.web("#b9352f");
    private static final Color MARKER_BORDER_COLOR = Color.web("#f8faf9");
    private static final Color HIGHLIGHT_COLOR = Color.web("#d7aa4a");
    private static final Color HIGHLIGHT_BORDER_COLOR = Color.web("#111816");
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
    private java.util.List<javafx.scene.Node> annotationNodes = new java.util.ArrayList<>();
    private Consumer<GeoPoint> pendingSecondPointHandler;
    private javafx.scene.Node pendingMarker;

    // Premium zoom engine
    private Timeline zoomTimeline;
    private double targetZoom = 1.0;
    private final DoubleProperty animatedZoom = new SimpleDoubleProperty(1.0);
    private double pivotMouseX;
    private double pivotMouseY;
    private double pivotContentX;
    private double pivotContentY;

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        setMapControlsState(false, false);
        configureAccessibility();
        configureZoomFeedback();

        // Listener that computes scroll position at 60fps during animation
        animatedZoom.addListener((obs, oldVal, newVal) -> {
            double currentZoom = newVal.doubleValue();
            zoomLevel = currentZoom;
            zoomGroup.setScaleX(currentZoom);
            zoomGroup.setScaleY(currentZoom);

            if (!mapLoaded) return;

            Bounds viewport = mapScrollPane.getViewportBounds();
            double unscaledWidth = mapPane.getWidth();
            double unscaledHeight = mapPane.getHeight();

            double scaledWidth = unscaledWidth * currentZoom;
            double scaledHeight = unscaledHeight * currentZoom;

            double maxScrollX = Math.max(0, scaledWidth - viewport.getWidth());
            double maxScrollY = Math.max(0, scaledHeight - viewport.getHeight());

            double newH = maxScrollX == 0 ? 0.5 : (pivotContentX * currentZoom - pivotMouseX) / maxScrollX;
            double newV = maxScrollY == 0 ? 0.5 : (pivotContentY * currentZoom - pivotMouseY) / maxScrollY;

            mapScrollPane.setHvalue(clamp(newH, 0, 1));
            mapScrollPane.setVvalue(clamp(newV, 0, 1));

            if (zoomFeedbackLabel.isVisible()) {
                zoomFeedbackLabel.setText(Math.round(currentZoom * 100) + "%");
            }
        });

        AnimationBehavior.installPressOnly(zoomInButton);
        AnimationBehavior.installPressOnly(zoomOutButton);
        AnimationBehavior.installPressOnly(centerRouteButton);

        zoomInButton.setOnAction(event -> zoomIn());
        zoomOutButton.setOnAction(event -> zoomOut());
        centerRouteButton.setOnAction(event -> centerRoute());

        mapScrollPane.addEventFilter(ScrollEvent.SCROLL, event -> {
            if (!mapLoaded) return;
            event.consume();

            double delta = (event.getDeltaY() > 0) ? WHEEL_ZOOM_STEP : -WHEEL_ZOOM_STEP;
            animateZoomTo(targetZoom + delta, event.getX(), event.getY());
        });

        mapPane.setOnMouseClicked(event -> {
            if (event.getButton() == MouseButton.SECONDARY) {
                handleMapSecondaryClick(event.getX(), event.getY());
            } else if (event.getButton() == MouseButton.PRIMARY && pendingSecondPointHandler != null) {
                handlePendingSecondPoint(event.getX(), event.getY());
            }
        });

        setZoom(1.0);
    }

    private void configureAccessibility() {
        zoomInButton.setAccessibleRole(AccessibleRole.BUTTON);
        zoomOutButton.setAccessibleRole(AccessibleRole.BUTTON);
        centerRouteButton.setAccessibleRole(AccessibleRole.BUTTON);
    }

    private void configureZoomFeedback() {
        zoomFeedbackTimer = new PauseTransition(Duration.millis(900));
        zoomFeedbackTimer.setOnFinished(event -> {
            zoomFeedbackLabel.setVisible(false);
            zoomFeedbackLabel.setManaged(false);
        });
    }

    private void zoomIn() {
        Bounds viewport = mapScrollPane.getViewportBounds();
        animateZoomTo(targetZoom + ZOOM_STEP, viewport.getWidth() / 2, viewport.getHeight() / 2);
    }

    private void zoomOut() {
        Bounds viewport = mapScrollPane.getViewportBounds();
        animateZoomTo(targetZoom - ZOOM_STEP, viewport.getWidth() / 2, viewport.getHeight() / 2);
    }

    private void animateZoomTo(double newZoom, double mouseX, double mouseY) {
        double clampedZoom = Math.max(getMinZoom(), Math.min(MAX_ZOOM, newZoom));
        if (clampedZoom == targetZoom) return;

        if (zoomTimeline != null && zoomTimeline.getStatus() == javafx.animation.Animation.Status.RUNNING) {
            zoomTimeline.stop();
        }

        targetZoom = clampedZoom;
        pivotMouseX = mouseX;
        pivotMouseY = mouseY;

        Bounds viewport = mapScrollPane.getViewportBounds();
        double currentScaledWidth = mapPane.getWidth() * animatedZoom.get();
        double currentScaledHeight = mapPane.getHeight() * animatedZoom.get();

        double hValue = mapScrollPane.getHvalue();
        double vValue = mapScrollPane.getVvalue();

        pivotContentX = (pivotMouseX + hValue * Math.max(0, currentScaledWidth - viewport.getWidth())) / animatedZoom.get();
        pivotContentY = (pivotMouseY + vValue * Math.max(0, currentScaledHeight - viewport.getHeight())) / animatedZoom.get();

        zoomTimeline = new Timeline(
            new javafx.animation.KeyFrame(Duration.millis(150),
                new javafx.animation.KeyValue(animatedZoom, targetZoom, Motion.SMOOTH)
            )
        );

        zoomGroup.setCache(true);
        zoomGroup.setCacheHint(CacheHint.SPEED);

        zoomTimeline.setOnFinished(e -> {
            zoomGroup.setCache(false);
            updateZoomButtons();
        });

        zoomTimeline.play();
        showZoomFeedback();
    }

    private void showZoomFeedback() {
        if (!mapLoaded) return;
        zoomFeedbackLabel.setText(Math.round(zoomLevel * 100) + "%");
        zoomFeedbackLabel.setVisible(true);
        zoomFeedbackLabel.setManaged(true);
        zoomFeedbackTimer.playFromStart();
    }

    private void hideZoomFeedback() {
        zoomFeedbackTimer.stop();
        zoomFeedbackLabel.setVisible(false);
        zoomFeedbackLabel.setManaged(false);
    }

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
        if (projection == null || currentActivity == null) return;
        var geoPoint = projection.unproject(x, y);
        if (mapSecondaryClickHandler != null) {
            mapSecondaryClickHandler.accept(geoPoint);
        }
    }

    private void handlePendingSecondPoint(double x, double y) {
        if (pendingSecondPointHandler == null || projection == null) return;
        var geoPoint = projection.unproject(x, y);
        pendingSecondPointHandler.accept(geoPoint);
        cancelPendingMode();
    }

    public void startPendingSecondPoint(GeoPoint firstPoint, AnnotationType tipo, String texto, Consumer<GeoPoint> onComplete) {
        if (projection == null) return;
        Point2D pt = projection.project(firstPoint);

        Circle marker = new Circle(pt.getX(), pt.getY(), 8);
        marker.setFill(Color.web("#fef08a"));
        marker.setStroke(Color.web("#111816"));
        marker.setStrokeWidth(2);
        mapPane.getChildren().add(marker);
        pendingMarker = marker;

        String toastText = switch (tipo) {
            case LINE -> "Clic de nuevo para marcar el final de la línea";
            case CIRCLE -> "Clic de nuevo para marcar el radio del círculo";
            default -> "Clic para definir el segundo punto";
        };
        pendingToastLabel.setText(toastText);
        pendingToastLabel.setVisible(true);
        pendingToastLabel.setManaged(true);

        mapPane.getScene().setCursor(javafx.scene.Cursor.CROSSHAIR);

        pendingSecondPointHandler = secondPoint -> {
            GeoPoint[] puntos = {firstPoint, secondPoint};
            Annotation annotation = new Annotation(tipo, texto, colorPorTipo(tipo), 2.0, java.util.List.of(puntos));
            if (currentActivity != null) {
                upv.ipc.sportlib.SportActivityApp.getInstance().addAnnotation(currentActivity, annotation);
                try {
                    var refreshed = upv.ipc.sportlib.SportActivityApp.getInstance().getActivityById(currentActivity.getId());
                    if (refreshed != null) {
                        refreshAnnotations(refreshed.getAnnotations());
                    }
                } catch (java.sql.SQLException e) {
                    System.err.println("Error recargando actividad: " + e.getMessage());
                }
            }
            System.out.println("Anotacion guardada: " + annotation.getType() + " - " + annotation.getText());
            if (onComplete != null) onComplete.accept(secondPoint);
        };
    }

    public void cancelPendingMode() {
        if (pendingMarker != null) {
            mapPane.getChildren().remove(pendingMarker);
            pendingMarker = null;
        }
        pendingToastLabel.setVisible(false);
        pendingToastLabel.setManaged(false);
        if (mapPane.getScene() != null) {
            mapPane.getScene().setCursor(javafx.scene.Cursor.DEFAULT);
        }
        pendingSecondPointHandler = null;
    }

    private String colorPorTipo(AnnotationType tipo) {
        return switch (tipo) {
            case POINT -> "#f59e0b";
            case LINE -> "#3b82f6";
            case TEXT -> "#10b981";
            case CIRCLE -> "#8b5cf6";
        };
    }

    public void setActivity(Activity activity) {
        currentActivity = activity;
        clearMap();

        if (activity == null) {
            emptyStateLabel.setText(EMPTY_STATE_MESSAGE);
            emptyStatePanel.setVisible(true);
            emptyStatePanel.setManaged(true);
            setMapControlsState(false, false);
            return;
        }

        emptyStatePanel.setVisible(false);
        emptyStatePanel.setManaged(false);

        if (!loadMapForActivity(activity)) {
            emptyStateLabel.setText(MAP_LOAD_ERROR_MESSAGE);
            emptyStatePanel.setVisible(true);
            emptyStatePanel.setManaged(true);
            setMapControlsState(false, false);
            return;
        }

        drawActivity(activity);
        boolean hasRoute = routeBounds != null && !routeBounds.isEmpty();
        setMapControlsState(true, hasRoute);

        if (hasRoute) {
            Platform.runLater(() -> {
                resetZoom();
                centerRoute();
            });
        }
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
        annotationNodes.clear();
        mapLoaded = false;
        zoomLevel = 1.0;
        targetZoom = 1.0;
        animatedZoom.set(1.0);
        zoomGroup.setScaleX(1.0);
        zoomGroup.setScaleY(1.0);
        mapScrollPane.setHvalue(0);
        mapScrollPane.setVvalue(0);
        mapImageView.setImage(null);
        mapImageView.setFitWidth(0);
        mapImageView.setFitHeight(0);
        hideZoomFeedback();
        mapPane.getChildren().setAll(mapImageView);
    }

    private boolean loadMapForActivity(Activity activity) {
        MapRegion region = activity.getSuggestedMap();
        if (region == null) return false;

        Image mapImage = new Image(new File(region.getImagePath()).toURI().toString());
        if (mapImage.isError() || mapImage.getWidth() <= 0 || mapImage.getHeight() <= 0) return false;

        double width = mapImage.getWidth();
        double height = mapImage.getHeight();

        mapImageView.setImage(mapImage);
        mapImageView.setFitWidth(width);
        mapImageView.setFitHeight(height);

        mapPane.setPrefSize(width, height);
        mapPane.setMinSize(width, height);
        mapPane.setMaxSize(width, height);

        projection = new MapProjection(region, width, height);
        mapLoaded = true;
        return true;
    }

    private void drawActivity(Activity activity) {
        if (activity == null || projection == null) return;
        if (activity.getTrackPoints() == null || activity.getTrackPoints().isEmpty()) return;

        Polyline routeLine = new Polyline();
        routeLine.setStrokeWidth(ROUTE_WIDTH);
        routeLine.setStroke(ROUTE_COLOR);

        for (var trackPoint : activity.getTrackPoints()) {
            Point2D point = projection.project(trackPoint);
            routeLine.getPoints().addAll(point.getX(), point.getY());
        }

        mapPane.getChildren().add(routeLine);
        routeBounds = routeLine.getBoundsInLocal();

        drawRouteMarker(activity.getStartPoint(), START_COLOR);
        drawRouteMarker(activity.getEndPoint(), END_COLOR);
        drawAnnotations(activity.getAnnotations());
    }

    private void setMapControlsState(boolean hasMap, boolean hasRoute) {
        mapLoaded = hasMap;
        centerRouteButton.setDisable(!hasRoute);
        updateZoomButtons();
    }

    private void updateZoomButtons() {
        double minZoom = getMinZoom();
        zoomInButton.setDisable(!mapLoaded || targetZoom >= MAX_ZOOM - 0.001);
        zoomOutButton.setDisable(!mapLoaded || targetZoom <= minZoom + 0.001);
    }

    private void drawRouteMarker(TrackPoint trackPoint, Color color) {
        if (trackPoint == null || projection == null) return;
        Point2D point = projection.project(trackPoint);
        Circle marker = new Circle(point.getX(), point.getY(), MARKER_RADIUS);
        marker.setFill(color);
        marker.setStroke(MARKER_BORDER_COLOR);
        marker.setStrokeWidth(MARKER_BORDER_WIDTH);
        mapPane.getChildren().add(marker);
    }

    private void drawAnnotations(List<Annotation> annotations) {
        if (annotations == null || projection == null) return;
        for (Annotation annotation : annotations) {
            if (annotation.getGeoPoints() == null || annotation.getGeoPoints().isEmpty()) continue;

            Color color = Color.web(annotation.getColor() != null ? annotation.getColor() : "#f59e0b");
            double strokeWidth = annotation.getStrokeWidth();

            javafx.scene.Node node = switch (annotation.getType()) {
                case POINT -> createAnnotationPoint(annotation, color);
                case CIRCLE -> createAnnotationCircle(annotation, color, strokeWidth);
                case LINE -> createAnnotationLine(annotation, color, strokeWidth);
                case TEXT -> createAnnotationText(annotation, color);
            };
            annotationNodes.add(node);
            mapPane.getChildren().add(node);
        }
    }

    private Circle createAnnotationPoint(Annotation annotation, Color color) {
        GeoPoint gp = annotation.getGeoPoints().get(0);
        Point2D point = projection.project(gp);
        Circle dot = new Circle(point.getX(), point.getY(), 6);
        dot.setFill(color);
        dot.setStroke(Color.web("#111816"));
        dot.setStrokeWidth(1.5);
        return dot;
    }

    private Circle createAnnotationCircle(Annotation annotation, Color color, double strokeWidth) {
        GeoPoint gpCentro = annotation.getGeoPoints().get(0);
        GeoPoint gpBorde = annotation.getGeoPoints().get(1);
        Point2D center = projection.project(gpCentro);
        Point2D edge = projection.project(gpBorde);
        double radius = Math.hypot(edge.getX() - center.getX(), edge.getY() - center.getY());
        Circle circle = new Circle(center.getX(), center.getY(), radius);
        circle.setFill(Color.TRANSPARENT);
        circle.setStroke(color);
        circle.setStrokeWidth(strokeWidth);
        return circle;
    }

    private Polyline createAnnotationLine(Annotation annotation, Color color, double strokeWidth) {
        Polyline line = new Polyline();
        line.setStroke(color);
        line.setStrokeWidth(strokeWidth);
        for (GeoPoint gp : annotation.getGeoPoints()) {
            Point2D point = projection.project(gp);
            line.getPoints().addAll(point.getX(), point.getY());
        }
        return line;
    }

    private javafx.scene.Node createAnnotationText(Annotation annotation, Color color) {
        GeoPoint gp = annotation.getGeoPoints().get(0);
        Point2D point = projection.project(gp);
        Text textNode = new Text(annotation.getText());
        textNode.setFill(color);
        textNode.setFont(javafx.scene.text.Font.font("System", 12));

        Bounds b = textNode.getBoundsInLocal();
        double textWidth = b.getWidth();
        double textHeight = b.getHeight();
        double pad = 3;
        double rx = point.getX();
        double ry = point.getY() - textHeight - 2;

        Rectangle bg = new Rectangle(rx - pad, ry - pad, textWidth + pad * 2, textHeight + pad * 2);
        bg.setFill(Color.web("#ffffff", 0.90));
        bg.setStroke(Color.web("#d9e1dc"));
        bg.setStrokeWidth(0.8);
        bg.setArcWidth(3);
        bg.setArcHeight(3);

        textNode.setX(rx);
        textNode.setY(ry + textHeight * 0.75);

        bg.setMouseTransparent(true);
        textNode.setMouseTransparent(true);

        Group g = new Group(bg, textNode);
        g.setMouseTransparent(true);
        return g;
    }

    private String toHexString(Color color) {
        return String.format("#%02X%02X%02X",
            (int) (color.getRed() * 255),
            (int) (color.getGreen() * 255),
            (int) (color.getBlue() * 255));
    }

    public void refreshAnnotations(List<Annotation> annotations) {
        if (projection == null) return;
        annotationNodes.forEach(mapPane.getChildren()::remove);
        annotationNodes.clear();
        drawAnnotations(annotations);
    }

    private void centerRoute() {
        if (routeBounds == null) return;

        double mapWidth = mapPane.getWidth() * zoomLevel;
        double mapHeight = mapPane.getHeight() * zoomLevel;

        double viewportWidth = mapScrollPane.getViewportBounds().getWidth();
        double viewportHeight = mapScrollPane.getViewportBounds().getHeight();

        double centerX = routeBounds.getCenterX() * zoomLevel;
        double centerY = routeBounds.getCenterY() * zoomLevel;

        double targetH = (centerX - viewportWidth / 2) / Math.max(mapWidth - viewportWidth, 1);
        double targetV = (centerY - viewportHeight / 2) / Math.max(mapHeight - viewportHeight, 1);
        double targetHClamped = clamp(targetH, 0, 1);
        double targetVClamped = clamp(targetV, 0, 1);

        Timeline centerTimeline = new Timeline();
        centerTimeline.getKeyFrames().add(
            new javafx.animation.KeyFrame(Duration.millis(250),
                new javafx.animation.KeyValue(mapScrollPane.hvalueProperty(), targetHClamped, Interpolator.SPLINE(0.2, 0.8, 0.2, 1.0)),
                new javafx.animation.KeyValue(mapScrollPane.vvalueProperty(), targetVClamped, Interpolator.SPLINE(0.2, 0.8, 0.2, 1.0))
            )
        );
        centerTimeline.play();
    }

    private void resetZoom() {
        if (zoomTimeline != null) zoomTimeline.stop();
        targetZoom = 1.0;
        zoomLevel = 1.0;
        animatedZoom.set(1.0);
        zoomGroup.setScaleX(1.0);
        zoomGroup.setScaleY(1.0);
        mapScrollPane.setHvalue(0.5);
        mapScrollPane.setVvalue(0.5);
        updateZoomButtons();
    }

    private void setZoom(double zoom) {
        double newZoom = Math.max(getMinZoom(), Math.min(MAX_ZOOM, zoom));

        Bounds viewportBounds = mapScrollPane.getViewportBounds();
        Bounds contentBounds = contentGroup.getBoundsInLocal();

        double viewportWidth = viewportBounds.getWidth();
        double viewportHeight = viewportBounds.getHeight();
        double contentWidth = contentBounds.getWidth();
        double contentHeight = contentBounds.getHeight();

        double centerX = mapScrollPane.getHvalue() * Math.max(contentWidth - viewportWidth, 0) + viewportWidth / 2;
        double centerY = mapScrollPane.getVvalue() * Math.max(contentHeight - viewportHeight, 0) + viewportHeight / 2;

        zoomLevel = newZoom;
        targetZoom = newZoom;
        animatedZoom.set(newZoom);
        zoomGroup.setScaleX(zoomLevel);
        zoomGroup.setScaleY(zoomLevel);

        Bounds newContentBounds = contentGroup.getBoundsInLocal();
        double newContentWidth = newContentBounds.getWidth();
        double newContentHeight = newContentBounds.getHeight();

        double newH = (centerX * (newContentWidth / contentWidth) - viewportWidth / 2) / Math.max(newContentWidth - viewportWidth, 1);
        double newV = (centerY * (newContentHeight / contentHeight) - viewportHeight / 2) / Math.max(newContentHeight - viewportHeight, 1);

        mapScrollPane.setHvalue(clamp(newH, 0, 1));
        mapScrollPane.setVvalue(clamp(newV, 0, 1));
        updateZoomButtons();
        showZoomFeedback();
    }

}