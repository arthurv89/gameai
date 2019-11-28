package com.swipecrowd.dinogame.game;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.awt.image.BufferedImage;

@AllArgsConstructor
public class Obstacle {
    @Getter
    @Setter
    private double xPos;

    @Getter
    private double yPos;

    @Getter
    private final BufferedImage image;

    public double getHeight() {
        return image.getHeight();
    }

    public double getWidth() {
        return image.getWidth();
    }
}
