package mapademo;

import java.io.File;
import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
import javafx.geometry.Rectangle2D;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import upv.ipc.sportlib.Activity;
import upv.ipc.sportlib.MapProjection;
import upv.ipc.sportlib.MapRegion;

final class ActivityMapPreview extends Pane {

    private static final double PADDING_X_RATIO = 0.5;
    private static final double PADDING_Y_RATIO = 0.25;
    private static final double MIN_PADDING_X = 96.0;
    private static final double MIN_PADDING_Y = 56.0;
    private static final double MAX_PADDING_X = 360.0;
    private static final double MAX_PADDING_Y = 220.0;
    private static final ActivityMapRenderer.Style PREVIEW_STYLE = new ActivityMapRenderer.Style(
            Color.web("#2563eb"),
            Color.web("#22c55e"),
            Color.web("#d7aa4a"),
            Color.web("#b9352f"),
            Color.web("#168a57"),
            Color.web("#b9352f"),
            Color.web("#f8faf9"),
            5.0,
            7.0,
            2.0,
            4.0,
            2.0,
            9.0
    );

    private Activity activity;
    private Image image;
    private MapProjection projection;
    private double imageWidth;
    private double imageHeight;
    private Rectangle2D viewport;

    ActivityMapPreview() {
        getStyleClass().add("mini-map-content");

        Rectangle clip = new Rectangle();
        clip.setArcWidth(8);
        clip.setArcHeight(8);
        clip.widthProperty().bind(widthProperty());
        clip.heightProperty().bind(heightProperty());
        setClip(clip);

        widthProperty().addListener((obs, oldValue, newValue) -> render());
        heightProperty().addListener((obs, oldValue, newValue) -> render());
    }

    void setActivity(Activity activity) {
        this.activity = activity;
        this.image = null;
        this.projection = null;
        this.viewport = null;
        this.imageWidth = 0;
        this.imageHeight = 0;

        if (activity == null) {
            showEmpty("Sin actividad");
            return;
        }

        MapRegion region = activity.getSuggestedMap();
        if (region == null || activity.getTrackPoints() == null || activity.getTrackPoints().isEmpty()) {
            showEmpty("Mapa no disponible");
            return;
        }

        Image loadedImage = new Image(new File(region.getImagePath()).toURI().toString(), false);
        if (loadedImage.isError() || loadedImage.getWidth() <= 0 || loadedImage.getHeight() <= 0) {
            showEmpty("Mapa no disponible");
            return;
        }

        this.image = loadedImage;
        this.imageWidth = image.getWidth();
        this.imageHeight = image.getHeight();
        this.projection = new MapProjection(region, imageWidth, imageHeight);
        render();
    }

    private void render() {
        getChildren().clear();

        if (activity == null) {
            showEmpty("Sin actividad");
            return;
        }
        if (image == null || projection == null) {
            showEmpty("Mapa no disponible");
            return;
        }

        double width = getWidth();
        double height = getHeight();
        if (width <= 0 || height <= 0) {
            return;
        }

        viewport = calculateViewport(width, height);

        ImageView imageView = new ImageView(image);
        imageView.setViewport(viewport);
        imageView.setFitWidth(width);
        imageView.setFitHeight(height);
        imageView.setPreserveRatio(false);
        imageView.setSmooth(true);

        getChildren().add(imageView);
        ActivityMapRenderer.drawActivity(this, activity, projection, true, PREVIEW_STYLE, this::toPreview);
    }

    private Rectangle2D calculateViewport(double previewWidth, double previewHeight) {
        Bounds routeBounds = ActivityMapRenderer.calculateRouteBounds(activity.getTrackPoints(), projection);
        if (routeBounds == null || routeBounds.isEmpty()) {
            return new Rectangle2D(0, 0, imageWidth, imageHeight);
        }

        double paddingX = clamp(routeBounds.getWidth() * PADDING_X_RATIO, MIN_PADDING_X, MAX_PADDING_X);
        double paddingY = clamp(routeBounds.getHeight() * PADDING_Y_RATIO, MIN_PADDING_Y, MAX_PADDING_Y);
        double targetWidth = Math.min(imageWidth, routeBounds.getWidth() + paddingX * 2);
        double targetHeight = Math.min(imageHeight, routeBounds.getHeight() + paddingY * 2);
        double aspect = previewWidth / previewHeight;

        if (targetWidth / targetHeight < aspect) {
            targetWidth = Math.min(imageWidth, targetHeight * aspect);
        } else {
            targetHeight = Math.min(imageHeight, targetWidth / aspect);
        }

        double centerX = clamp(routeBounds.getCenterX(), targetWidth / 2, imageWidth - targetWidth / 2);
        double centerY = clamp(routeBounds.getCenterY(), targetHeight / 2, imageHeight - targetHeight / 2);
        double minX = clamp(centerX - targetWidth / 2, 0, imageWidth - targetWidth);
        double minY = clamp(centerY - targetHeight / 2, 0, imageHeight - targetHeight);

        return new Rectangle2D(minX, minY, targetWidth, targetHeight);
    }

    private Point2D toPreview(Point2D sourcePoint) {
        if (!isFinite(sourcePoint) || viewport == null) return null;
        double x = (sourcePoint.getX() - viewport.getMinX()) / viewport.getWidth() * getWidth();
        double y = (sourcePoint.getY() - viewport.getMinY()) / viewport.getHeight() * getHeight();
        return new Point2D(x, y);
    }

    private void showEmpty(String message) {
        getChildren().clear();
        Label empty = new Label(message);
        empty.getStyleClass().add("muted-label");
        empty.layoutXProperty().bind(widthProperty().subtract(empty.widthProperty()).divide(2));
        empty.layoutYProperty().bind(heightProperty().subtract(empty.heightProperty()).divide(2));
        getChildren().add(empty);
    }

    private double clamp(double value, double min, double max) {
        if (max < min) return min;
        return Math.max(min, Math.min(max, value));
    }

    private boolean isFinite(Point2D point) {
        return point != null
                && Double.isFinite(point.getX())
                && Double.isFinite(point.getY());
    }
}
