package uk.ac.ed.inf;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public enum CompassDirection {
    N (0),
    NNE (22.5),
    NE(45),
    ENE(67.5),
    E(90),
    ESE(112.5),
    SE(135),
    SSE(157.5),
    S(180),
    SSW(202.5),
    SW(225),
    WSW(247.5),
    W(270),
    WNW(292.5),
    NW(315),
    NNW(337.5);


    private final double angle;
    CompassDirection(double angle) {
        this.angle = angle;
    }
    public double angle(){
        return angle;
    }

    public static List<CompassDirection>  compassValues() {
        return new ArrayList<CompassDirection>(Arrays.asList(CompassDirection.values()));
    }

}
