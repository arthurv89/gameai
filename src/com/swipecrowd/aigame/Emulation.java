package com.swipecrowd.aigame;

import lombok.Getter;

import java.awt.Rectangle;
import java.util.concurrent.CopyOnWriteArrayList;

import static com.swipecrowd.aigame.GamePanel.DINOSAUR_HEIGHT;
import static com.swipecrowd.aigame.GamePanel.DINOSAUR_WIDTH;
import static com.swipecrowd.aigame.GamePanel.DINOSAUR_X_POS;
import static com.swipecrowd.aigame.GamePanel.OBSTACLE_HEIGHT;
import static com.swipecrowd.aigame.GamePanel.OBSTACLE_WIDTH;
import static com.swipecrowd.aigame.GamePanel.OBSTACLE_Y_POS;

public class Emulation {
    private static final int POP_SIZE = 1;
    private static final int FPS = 100000;
    private static final double timeInBetween = 1000 / FPS;
    private static final double SPAWN_PROBABILITY = 0.01;
    private static final double GRAVITY = 1;
    private static final double X_SPEED = 10;
    private static final double FORCE_UP = GRAVITY * 20;

    private double forceUp;
    private boolean jumping;
    private long lastDrawTime;

    @Getter
    private CopyOnWriteArrayList<Obstacle> obstacles;

    @Getter
    private Population population;

    @Getter
    private int emulationNo = 0;
    private int time;

    public void start() {
        final Gui gui = new Gui();
        gui.setup(this);

        run(gui);
    }

    private void run(final Gui gui) {
        while (true) {
            resetEmulation();
            runGeneration(gui);
            emulationNo++;
        }
    }

    private void resetEmulation() {
        obstacles = new CopyOnWriteArrayList<>();
        population = new Population(POP_SIZE);
        lastDrawTime = 0;
        jumping = false;
        forceUp = 0;
        time = 0;
    }

    private void runGeneration(final Gui gui) {
        boolean hasAliveDinos = true;
        while(hasAliveDinos) {
            waitTillNextFrame();

            final Action action = calculateAction();
            updateState(action, gui.getPanel().getWidth());

            gui.redraw(time);

            hasAliveDinos = hasAliveDinos();
        }
    }

    private Action calculateAction() {
        return new JumpAction();
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

    private void updateState(final Action action, final int xPos) {
        if(shouldAddObstacle()) {
            obstacles.add(new Obstacle(xPos, OBSTACLE_Y_POS, OBSTACLE_WIDTH, OBSTACLE_HEIGHT));
        }

        applyAction(action);

        updateDinosaurs();
        updateObstacles();

        markDeadDinos();
        time++;
    }

    private void applyAction(final Action action) {
        if(action instanceof JumpAction) {
            jump();
        }
    }

    private boolean hasAliveDinos() {
        return population.getDinosaurs().stream().anyMatch(x -> !x.isDead());
    }

    private void markDeadDinos() {
        population.getDinosaurs().forEach(dino -> {
            obstacles.forEach(obstacle -> {
                if(isColliding(dino, obstacle)) {
                    dino.setDead();
                }
            });
        });
    }

    private boolean isColliding(final Dinosaur dino, final Obstacle obstacle) {
        final Rectangle dinoBoundingBox = dinoBoundingBox(dino);
        final Rectangle obstacleBoundingBox = obstacleBoundingBox(obstacle);
        return dinoBoundingBox.intersects(obstacleBoundingBox);
    }

    private Rectangle obstacleBoundingBox(final Obstacle obstacle) {
        return new Rectangle((int) obstacle.getXPos(), (int) obstacle.getYPos(),
                (int) obstacle.getWidth(), (int) obstacle.getHeight());
    }

    private Rectangle dinoBoundingBox(final Dinosaur dino) {
        return new Rectangle(DINOSAUR_X_POS, (int) dino.getYPos(),
                DINOSAUR_WIDTH, DINOSAUR_HEIGHT);
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
//            System.out.printf("Force up %s, Y = %s %n", forceUp, dino.getYPos());
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
