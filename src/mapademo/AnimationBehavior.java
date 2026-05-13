package mapademo;

import javafx.animation.FadeTransition;
import javafx.animation.ParallelTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.TranslateTransition;
import javafx.geometry.Point2D;
import javafx.scene.CacheHint;
import javafx.scene.Node;
import javafx.scene.input.MouseEvent;
import javafx.util.Duration;

public class AnimationBehavior {

    private static final String HOVER_INSTALLED = "motion.hover.installed";

    public static void installHover(Node node) {
        installHover(node, Motion.SCALE_HOVER);
    }

    public static void installPressOnly(Node node) {
        installHover(node, 1.0);
    }

    public static void installHover(Node node, double scaleUp) {
        if (Boolean.TRUE.equals(node.getProperties().get(HOVER_INSTALLED))) return;
        node.getProperties().put(HOVER_INSTALLED, true);

        boolean originalCache = node.isCache();
        CacheHint originalCacheHint = node.getCacheHint();
        double baseScaleX = node.getScaleX();
        double baseScaleY = node.getScaleY();

        ScaleTransition transition = new ScaleTransition(Motion.T_HOVER, node);
        transition.setInterpolator(Motion.SMOOTH);

        node.addEventHandler(MouseEvent.MOUSE_ENTERED, e -> {
            node.setCache(true);
            node.setCacheHint(CacheHint.SPEED);
            transition.stop();
            transition.setDuration(Motion.T_HOVER);
            transition.setToX(baseScaleX * scaleUp);
            transition.setToY(baseScaleY * scaleUp);
            transition.setOnFinished(null);
            transition.playFromStart();
        });

        node.addEventHandler(MouseEvent.MOUSE_EXITED, e -> {
            node.setCache(true);
            node.setCacheHint(CacheHint.SPEED);
            transition.stop();
            transition.setDuration(Motion.T_EXIT);
            transition.setToX(baseScaleX);
            transition.setToY(baseScaleY);
            transition.setOnFinished(ev -> {
                node.setCache(originalCache);
                node.setCacheHint(originalCacheHint);
            });
            transition.playFromStart();
        });

        node.addEventHandler(MouseEvent.MOUSE_PRESSED, e -> {
            node.setCache(true);
            node.setCacheHint(CacheHint.SPEED);
            transition.stop();
            transition.setDuration(Motion.T_PRESS);
            transition.setToX(baseScaleX * Motion.SCALE_PRESS);
            transition.setToY(baseScaleY * Motion.SCALE_PRESS);
            transition.setOnFinished(null);
            transition.playFromStart();
        });

        node.addEventHandler(MouseEvent.MOUSE_RELEASED, e -> {
            node.setCache(true);
            node.setCacheHint(CacheHint.SPEED);
            transition.stop();
            transition.setDuration(Motion.T_PRESS);
            Point2D localPoint = node.sceneToLocal(e.getSceneX(), e.getSceneY());
            boolean stillInside = localPoint != null && node.contains(localPoint);
            transition.setToX(stillInside ? baseScaleX * scaleUp : baseScaleX);
            transition.setToY(stillInside ? baseScaleY * scaleUp : baseScaleY);
            transition.setOnFinished(ev -> {
                node.setCache(originalCache);
                node.setCacheHint(originalCacheHint);
            });
            transition.playFromStart();
        });
    }

    public static void slideFadeIn(Node node) {
        slideFadeIn(node, Duration.millis(0));
    }

    public static void slideFadeIn(Node node, Duration delay) {
        node.setOpacity(0);
        node.setTranslateY(15);
        FadeTransition fade = new FadeTransition(Motion.T_ENTER, node);
        fade.setDelay(delay);
        fade.setToValue(1.0);
        fade.setInterpolator(Motion.SMOOTH);
        TranslateTransition slide = new TranslateTransition(Motion.T_ENTER, node);
        slide.setDelay(delay);
        slide.setToY(0);
        slide.setInterpolator(Motion.SMOOTH);
        ParallelTransition appear = new ParallelTransition(fade, slide);

        boolean originalCache = node.isCache();
        CacheHint originalCacheHint = node.getCacheHint();
        node.setCache(true);
        node.setCacheHint(CacheHint.SPEED);
        appear.setOnFinished(e -> {
            node.setCache(originalCache);
            node.setCacheHint(originalCacheHint);
        });
        appear.play();
    }

    public static void slideFadeInStagger(java.util.List<Node> nodes, Duration staggerDelay) {
        for (int i = 0; i < nodes.size(); i++) {
            slideFadeIn(nodes.get(i), staggerDelay.multiply(i));
        }
    }
}