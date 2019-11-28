package com.swipecrowd.dinogame.game;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.awt.image.BufferedImage;
import java.util.Collections;
import java.util.List;

@AllArgsConstructor
public class Obstacle {
    @Getter
    @Setter
    private double xPos;

    @Getter
    private double yPos;

    @Getter
    private final List<BufferedImage> images;

    public Obstacle(final int xPos, final double yPos, final BufferedImage image) {
        this(xPos, yPos, Collections.singletonList(image));
    }

    public int getHeight() {
        return images.get(0).getHeight();
    }

    public int getWidth() {
        return images.get(0).getWidth();
    }
}

