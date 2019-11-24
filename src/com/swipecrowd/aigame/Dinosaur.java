package com.swipecrowd.aigame;

import lombok.Getter;

public class Dinosaur {
    @Getter
    private double yPos = 0;

    @Getter
    private boolean dead = false;

    public void goUp(final double yDiff) {
        yPos += yDiff;
    }

    public void setYPos(final double yPos) {
        this.yPos = yPos;
    }

    public void setDead() {
        this.dead = true;
    }
}
