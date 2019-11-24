package com.swipecrowd.aigame;

import lombok.Getter;

public class Dinosaur {
    @Getter
    private double yPos = 0;

    public void goUp(final double yDiff) {
        yPos += yDiff;
    }

    public void setYPos(final double yPos) {
        this.yPos = yPos;
    }
}
