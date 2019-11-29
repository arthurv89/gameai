package com.swipecrowd.dinogame.utils;

import java.util.concurrent.atomic.AtomicLong;

public class Tick {
    public static void waitTillNextFrame(AtomicLong lastDrawTime, final double fps) {
        if (lastDrawTime.get() == 0) {
            lastDrawTime.set(System.currentTimeMillis());
        } else {
            // Wait a bit until we're allowed to draw the frame
            final long currentTime = System.currentTimeMillis();
            final int timeBetweenFrames = timeBetweenFrames(fps);
            if(currentTime - lastDrawTime.get() < timeBetweenFrames) {
                try {
                    Thread.sleep(lastDrawTime.get() + timeBetweenFrames - currentTime);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
            lastDrawTime.getAndAdd(timeBetweenFrames);
        }
    }

    private static int timeBetweenFrames(final double fps) {
        return (int) (1000 / fps);
    }
}
