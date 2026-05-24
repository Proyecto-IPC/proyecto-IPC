package mapademo;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import javafx.geometry.BoundingBox;
import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Polyline;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import upv.ipc.sportlib.Activity;
import upv.ipc.sportlib.Annotation;
import upv.ipc.sportlib.GeoPoint;
import upv.ipc.sportlib.MapProjection;
import upv.ipc.sportlib.TrackPoint;

final class ActivityMapRenderer {

    record Style(
            Color routeColor,
            Color speedSlowColor,
            Color speedMediumColor,
            Color speedFastColor,
            Color startColor,
            Color endColor,
            Color markerBorderColor,
            double routeWidth,
            double markerRadius,
            double markerBorderWidth,
            double minAnnotationPointRadius,
            double minAnnotationStrokeWidth,
            double minTextSize
    ) {}

    record Result(Bounds routeBounds, List<Node> annotationNodes) {}

    private ActivityMapRenderer() {
    }

    static Result drawActivity(
            Pane target,
            Activity activity,
            MapProjection projection,
            boolean speedVisualizationEnabled,
            Style style,
            Function<Point2D, Point2D> pointTransform
    ) {
        if (target == null || activity == null || projection == null || activity.getTrackPoints() == null) {
            return new Result(null, List.of());
        }

        Bounds bounds = calculateRouteBounds(activity.getTrackPoints(), projection);
        if (speedVisualizationEnabled) {
            drawSpeedRoute(target, activity.getTrackPoints(), projection, style, pointTransform);
        } else {
            drawPlainRoute(target, activity.getTrackPoints(), projection, style, pointTransform);
        }

        drawRouteMarker(target, activity.getStartPoint(), projection, style.startColor(), style, pointTransform);
        drawRouteMarker(target, activity.getEndPoint(), projection, style.endColor(), style, pointTransform);
        List<Node> annotationNodes = drawAnnotations(target, activity.getAnnotations(), projection, style, pointTransform);
        return new Result(bounds, annotationNodes);
    }

    static List<Node> drawAnnotations(
            Pane target,
            List<Annotation> annotations,
            MapProjection projection,
            Style style,
            Function<Point2D, Point2D> pointTransform
    ) {
        List<Node> nodes = new ArrayList<>();
        if (target == null || annotations == null || projection == null) return nodes;

        for (Annotation annotation : annotations) {
            if (annotation == null || annotation.getType() == null
                    || annotation.getGeoPoints() == null || annotation.getGeoPoints().isEmpty()) {
                continue;
            }

            Color color = parseColor(annotation.getColor(), Color.web("#f59e0b"));
            Node node = switch (annotation.getType()) {
                case POINT -> createAnnotationPoint(annotation, projection, color, style, pointTransform);
                case CIRCLE -> createAnnotationCircle(annotation, projection, color, style, pointTransform);
                case LINE -> createAnnotationLine(annotation, projection, color, style, pointTransform);
                case TEXT -> createAnnotationText(annotation, projection, color, style, pointTransform);
            };
            if (node != null) {
                nodes.add(node);
                target.getChildren().add(node);
            }
        }
        return nodes;
    }

    static Bounds calculateRouteBounds(List<TrackPoint> trackPoints, MapProjection projection) {
        if (trackPoints == null || projection == null || trackPoints.isEmpty()) return null;

        double minX = Double.MAX_VALUE;
        double minY = Double.MAX_VALUE;
        double maxX = -Double.MAX_VALUE;
        double maxY = -Double.MAX_VALUE;
        boolean hasPoint = false;

        for (TrackPoint trackPoint : trackPoints) {
            Point2D point = projection.project(trackPoint);
            if (!isFinite(point)) continue;
            hasPoint = true;
            minX = Math.min(minX, point.getX());
            minY = Math.min(minY, point.getY());
            maxX = Math.max(maxX, point.getX());
            maxY = Math.max(maxY, point.getY());
        }

        return hasPoint
                ? new BoundingBox(minX, minY, Math.max(1, maxX - minX), Math.max(1, maxY - minY))
                : null;
    }

    private static void drawPlainRoute(
            Pane target,
            List<TrackPoint> trackPoints,
            MapProjection projection,
            Style style,
            Function<Point2D, Point2D> pointTransform
    ) {
        Polyline routeLine = new Polyline();
        routeLine.setStrokeWidth(style.routeWidth());
        routeLine.setStroke(style.routeColor());

        for (TrackPoint trackPoint : trackPoints) {
            Point2D point = transform(projection.project(trackPoint), pointTransform);
            if (isFinite(point)) {
                routeLine.getPoints().addAll(point.getX(), point.getY());
            }
        }

        if (routeLine.getPoints().size() >= 4) {
            target.getChildren().add(routeLine);
        }
    }

    private static void drawSpeedRoute(
            Pane target,
            List<TrackPoint> trackPoints,
            MapProjection projection,
            Style style,
            Function<Point2D, Point2D> pointTransform
    ) {
        if (trackPoints.size() < 2) return;

        double minSpeed = Double.MAX_VALUE;
        double maxSpeed = 0.0;
        double[] speeds = new double[trackPoints.size() - 1];
        for (int i = 1; i < trackPoints.size(); i++) {
            double speed = trackPoints.get(i - 1).speedTo(trackPoints.get(i));
            if (!Double.isFinite(speed)) speed = 0.0;
            speeds[i - 1] = speed;
            minSpeed = Math.min(minSpeed, speed);
            maxSpeed = Math.max(maxSpeed, speed);
        }

        for (int i = 1; i < trackPoints.size(); i++) {
            Point2D previous = transform(projection.project(trackPoints.get(i - 1)), pointTransform);
            Point2D current = transform(projection.project(trackPoints.get(i)), pointTransform);
            if (!isFinite(previous) || !isFinite(current)) continue;

            Polyline segment = new Polyline(previous.getX(), previous.getY(), current.getX(), current.getY());
            segment.setStrokeWidth(style.routeWidth());
            segment.setStrokeLineCap(javafx.scene.shape.StrokeLineCap.ROUND);
            segment.setStroke(getSpeedColor(speeds[i - 1], minSpeed, maxSpeed, style));
            target.getChildren().add(segment);
        }
    }

    private static Color getSpeedColor(double speed, double minSpeed, double maxSpeed, Style style) {
        if (maxSpeed <= minSpeed) return style.speedMediumColor();
        double ratio = (speed - minSpeed) / (maxSpeed - minSpeed);
        if (ratio < 0.33) return style.speedSlowColor();
        if (ratio < 0.66) return style.speedMediumColor();
        return style.speedFastColor();
    }

    private static void drawRouteMarker(
            Pane target,
            TrackPoint trackPoint,
            MapProjection projection,
            Color color,
            Style style,
            Function<Point2D, Point2D> pointTransform
    ) {
        if (trackPoint == null) return;
        Point2D point = transform(projection.project(trackPoint), pointTransform);
        if (!isFinite(point)) return;

        Circle marker = new Circle(point.getX(), point.getY(), style.markerRadius());
        marker.setFill(color);
        marker.setStroke(style.markerBorderColor());
        marker.setStrokeWidth(style.markerBorderWidth());
        target.getChildren().add(marker);
    }

    private static Circle createAnnotationPoint(
            Annotation annotation,
            MapProjection projection,
            Color color,
            Style style,
            Function<Point2D, Point2D> pointTransform
    ) {
        Point2D point = transform(projection.project(annotation.getGeoPoints().get(0)), pointTransform);
        if (!isFinite(point)) return null;

        Circle dot = new Circle(point.getX(), point.getY(), Math.max(annotation.getStrokeWidth(), style.minAnnotationPointRadius()));
        dot.setFill(color);
        dot.setStroke(Color.web("#111816"));
        dot.setStrokeWidth(1.5);
        return dot;
    }

    private static Circle createAnnotationCircle(
            Annotation annotation,
            MapProjection projection,
            Color color,
            Style style,
            Function<Point2D, Point2D> pointTransform
    ) {
        if (annotation.getGeoPoints().size() < 2) return null;
        Point2D center = transform(projection.project(annotation.getGeoPoints().get(0)), pointTransform);
        Point2D edge = transform(projection.project(annotation.getGeoPoints().get(1)), pointTransform);
        if (!isFinite(center) || !isFinite(edge)) return null;

        Circle circle = new Circle(center.getX(), center.getY(), Math.hypot(edge.getX() - center.getX(), edge.getY() - center.getY()));
        circle.setFill(Color.TRANSPARENT);
        circle.setStroke(color);
        circle.setStrokeWidth(Math.max(annotation.getStrokeWidth(), style.minAnnotationStrokeWidth()));
        return circle;
    }

    private static Polyline createAnnotationLine(
            Annotation annotation,
            MapProjection projection,
            Color color,
            Style style,
            Function<Point2D, Point2D> pointTransform
    ) {
        Polyline line = new Polyline();
        line.setStroke(color);
        line.setStrokeWidth(Math.max(annotation.getStrokeWidth(), style.minAnnotationStrokeWidth()));
        for (GeoPoint geoPoint : annotation.getGeoPoints()) {
            Point2D point = transform(projection.project(geoPoint), pointTransform);
            if (isFinite(point)) {
                line.getPoints().addAll(point.getX(), point.getY());
            }
        }
        return line.getPoints().size() >= 4 ? line : null;
    }

    private static Node createAnnotationText(
            Annotation annotation,
            MapProjection projection,
            Color color,
            Style style,
            Function<Point2D, Point2D> pointTransform
    ) {
        Point2D point = transform(projection.project(annotation.getGeoPoints().get(0)), pointTransform);
        if (!isFinite(point)) return null;

        Text text = new Text(annotation.getText() != null ? annotation.getText() : "");
        text.setFill(color);
        text.setFont(Font.font("System", Math.max(annotation.getStrokeWidth(), style.minTextSize())));

        Bounds bounds = text.getLayoutBounds();
        double pad = 3;
        Rectangle background = new Rectangle(point.getX() - pad, point.getY() - bounds.getHeight() - pad, bounds.getWidth() + pad * 2, bounds.getHeight() + pad * 2);
        background.setFill(Color.web("#ffffff", 0.90));
        background.setStroke(Color.web("#d9e1dc"));
        background.setStrokeWidth(0.8);
        background.setArcWidth(3);
        background.setArcHeight(3);

        text.setX(point.getX());
        text.setY(point.getY() - bounds.getHeight() * 0.25);
        background.setMouseTransparent(true);
        text.setMouseTransparent(true);

        Group group = new Group(background, text);
        group.setMouseTransparent(true);
        return group;
    }

    private static Point2D transform(Point2D point, Function<Point2D, Point2D> pointTransform) {
        if (!isFinite(point)) return null;
        return pointTransform != null ? pointTransform.apply(point) : point;
    }

    private static Color parseColor(String color, Color fallback) {
        if (color == null || color.isBlank()) return fallback;
        try {
            return Color.web(color);
        } catch (IllegalArgumentException ex) {
            return fallback;
        }
    }

    private static boolean isFinite(Point2D point) {
        return point != null
                && Double.isFinite(point.getX())
                && Double.isFinite(point.getY());
    }
}
