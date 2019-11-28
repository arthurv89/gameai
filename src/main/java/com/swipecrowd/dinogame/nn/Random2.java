package com.swipecrowd.dinogame.nn;

import java.util.Random;

public class Random2 {
    static Random r = new Random(1);

    public static double random() {
        return r.nextDouble();
    }

    public double nextGaussian() {
        return r.nextGaussian();
    }
}
