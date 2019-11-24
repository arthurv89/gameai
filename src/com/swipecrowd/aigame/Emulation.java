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
    private static final int POP_SIZE = 500;
    private static final int FPS = 100;
    private static final double timeInBetween = 1000 / FPS;
    private static final double SPAWN_PROBABILITY = 0.02;
    private static final double GRAVITY = 1;
    private static final double X_SPEED = 10;
    private static final double FORCE_UP = GRAVITY * 20;

    private long lastDrawTime;

    @Getter
    private CopyOnWriteArrayList<Obstacle> obstacles = new CopyOnWriteArrayList<>();

    @Getter
    private Population population = new Population(POP_SIZE);

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
        obstacles.clear();
        population.clear();
        lastDrawTime = 0;
        time = 0;
    }

    private void runGeneration(final Gui gui) {
        int aliveDinos = population.getDinosaurs().size();
        while(aliveDinos > 0) {
            waitTillNextFrame();

            updateState(gui.getPanel().getWidth());

            aliveDinos = countAliveDinos();

            gui.redraw(time, aliveDinos);
        }
    }

    private Action calculateAction(final Dinosaur dino) {
        if(Math.random() < 0.5) {
            return new JumpAction();
        } else {
            return new NullAction();
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

    private void updateState(final int xPos) {
        if(shouldAddObstacle()) {
            obstacles.add(new Obstacle(xPos, OBSTACLE_Y_POS, OBSTACLE_WIDTH, OBSTACLE_HEIGHT));
        }

        updateDinosaurs();
        updateObstacles();

        markDeadDinos();
        time++;
    }

    private void applyAction(final Action action, final Dinosaur dino) {
        if(action instanceof JumpAction) {
            jump(dino);
        }
    }

    private int countAliveDinos() {
        return (int) population.getDinosaurs().stream().filter(x -> !x.isDead()).count();
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
            final Action action = calculateAction(dino);
            applyAction(action, dino);

//            System.out.printf("Force up %s, Y = %s %n", forceUp, dino.getYPos());
            dino.goUp(dino.getForceUp());

            if(dino.getYPos() < 0) {
                dino.setYPos(0);
                dino.setForceUp(0);
            }
            if(dino.getYPos() == 0) {
                dino.setJumping(false);
            } else {
                dino.setForceUp(dino.getForceUp() - GRAVITY);
            }
        });
    }

    public void jump(final Dinosaur dino) {
        if(!dino.isJumping()) {
            dino.setJumping(true);
            dino.setForceUp(FORCE_UP);
        }
    }
}
