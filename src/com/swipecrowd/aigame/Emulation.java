package com.swipecrowd.aigame;

import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

public class Emulation {
    private static final int POP_SIZE = 1;
    private static final int FPS = 100;
    private static final double timeInBetween = 1000 / FPS;
    private static final double SPAWN_PROBABILITY = 0.01;
    private static final double GRAVITY = 1;
    private static final double X_SPEED = 10;
    private static final double FORCE_UP = GRAVITY * 20;

    private double forceUp;
    private boolean jumping = false;
    private long lastDrawTime = 0;

    @Getter
    private List<Obstacle> obstacles = new ArrayList<>();

    @Getter
    private Population population = new Population(POP_SIZE);;

    public void start() {
        final Gui gui = new Gui();
        gui.setup(this);

        run(gui);
    }

    private void run(final Gui gui) {
        while (true) {
            runGeneration(gui);

            population.naturalSelection();
            gui.removeObstacles();
        }
    }

    private void runGeneration(final Gui gui) {
        while(!population.done()) {
            waitTillNextFrame();

            updateState(gui.getPanel().getWidth());

            gui.redraw();
            population.updateAlive();
        }
    }

    private void waitTillNextFrame() {
        if (lastDrawTime == 0) {
            lastDrawTime = System.currentTimeMillis();
        } else {
            // Wait a bit until we're allowed to draw the frame
            final long currentTime = System.currentTimeMillis();
            if(currentTime - lastDrawTime < timeInBetween) {
                try {
                    Thread.sleep((long) (lastDrawTime + timeInBetween - currentTime));
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
            lastDrawTime += timeInBetween;
        }
    }

    private void updateState(final int width) {
        if(shouldAddObstacle()) {
            final double height = 0;
            obstacles.add(new Obstacle(width, height));
        }

        updateDinosaurs();
        updateObstacles();
    }

    private boolean shouldAddObstacle() {
        return Math.random() < SPAWN_PROBABILITY;
    }

    private void updateObstacles() {
        obstacles.forEach(obstacle -> {
            obstacle.setXPos(obstacle.getXPos() - X_SPEED);
        });
    }

    private void updateDinosaurs() {
        population.getDinosaurs().forEach(dino -> {
            System.out.printf("Force up %s, Y = %s %n", forceUp, dino.getYPos());
            dino.goUp(this.forceUp);

            if(dino.getYPos() < 0) {
                dino.setYPos(0);
                this.forceUp = 0;
            }
            if(dino.getYPos() == 0) {
                jumping = false;
            } else {
                this.forceUp -= GRAVITY;
            }
        });
    }

    public void jump() {
        if(!jumping) {
            jumping = true;
            forceUp = FORCE_UP;
        }
    }
}
