package com.swipecrowd.aigame;

import lombok.Getter;
import lombok.Setter;

public class Dinosaur {
    @Getter
    private double yPos = 0;

    @Getter
    private boolean dead = false;

    @Getter
    @Setter
    private double forceUp = 0;

    @Getter
    @Setter
    private boolean jumping = false;

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
