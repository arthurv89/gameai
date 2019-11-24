package com.swipecrowd.aigame;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
public class Obstacle {
    @Getter
    @Setter
    private double xPos;

    @Getter
    private double yPos;
}
