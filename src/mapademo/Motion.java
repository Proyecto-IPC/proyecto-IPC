package mapademo;

import javafx.animation.Interpolator;
import javafx.util.Duration;

public class Motion {
    public static final Duration T_PRESS = Duration.millis(80);
    public static final Duration T_HOVER = Duration.millis(120);
    public static final Duration T_EXIT = Duration.millis(160);
    public static final Duration T_ENTER = Duration.millis(240);
    public static final Interpolator SMOOTH = Interpolator.SPLINE(0.2, 0.8, 0.2, 1.0);

    public static final double SCALE_PRESS = 0.97;
    public static final double SCALE_HOVER = 1.015;
}