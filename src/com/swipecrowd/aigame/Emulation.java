package com.swipecrowd.aigame;

import com.swipecrowd.aigame.ai.Population;
import lombok.Getter;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Stream;

import static com.swipecrowd.aigame.GamePanel.DINOSAUR_HEIGHT;
import static com.swipecrowd.aigame.GamePanel.DINOSAUR_WIDTH;
import static com.swipecrowd.aigame.GamePanel.DINOSAUR_X_POS;
import static com.swipecrowd.aigame.GamePanel.OBSTACLE_HEIGHT;
import static com.swipecrowd.aigame.GamePanel.OBSTACLE_WIDTH;
import static com.swipecrowd.aigame.GamePanel.OBSTACLE_Y_POS;

public class Emulation {
    private static final int POP_SIZE = 1000;
    private static final int FPS = 100;
    private static final double timeInBetween = 1000 / FPS;
    private static final double SPAWN_PROBABILITY = 0.02;
    private static final double GRAVITY = 1;
    public static double xSpeed = 10;
    private static final double FORCE_UP = GRAVITY * 20;
    static ArrayList<Integer> obstacleHistory = new ArrayList<Integer>();
    static ArrayList<Integer> randomAdditionHistory = new ArrayList<Integer>();

    private long lastDrawTime;

    @Getter
    private CopyOnWriteArrayList<Obstacle> obstacles = new CopyOnWriteArrayList<>();

    @Getter
    private Population population = new Population(POP_SIZE);

    @Getter
    private int emulationNo = 0;
    private int time;
    private int lastSpawnTime;

    public void start() {
        final Gui gui = new Gui();
        gui.setup(this);

        run(gui);
    }

    private void run(final Gui gui) {
        while (true) {
            resetEmulation();
            runGeneration(gui);
            population.naturalSelection();
            emulationNo++;
        }
    }

    private void resetEmulation() {
        obstacles.clear();
        lastDrawTime = 0;
        time = 0;
    }

    private void runGeneration(final Gui gui) {
        int aliveDinos = population.getPop().size();
        while(aliveDinos > 0) {
            waitTillNextFrame();

            updateState(gui.getPanel().getWidth());

            aliveDinos = countAliveDinos();

            gui.redraw(time, aliveDinos);
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

    private void updateState(final int frameWidth) {
        if(shouldAddObstacle()) {
            obstacles.add(new Obstacle(frameWidth, OBSTACLE_Y_POS, OBSTACLE_WIDTH, OBSTACLE_HEIGHT));
            lastSpawnTime = time;
        }

        updateDinosaurs();
        updateObstacles();
        incrementScore();

        markDeadDinos();
        time++;
        xSpeed *= 1.00001;
    }

    private void incrementScore() {
        getAlivePlayers().forEach(x -> x.incrementScore());
    }

    private void applyAction(final Action action, final Player dino) {
        if(action instanceof JumpAction) {
            jump(dino);
        }
    }

    private int countAliveDinos() {
        return (int) getAlivePlayers().count();
    }

    private Stream<Player> getAlivePlayers() {
        return population.getPop().stream().filter(x -> !x.isDead());
    }

    private void markDeadDinos() {
        population.getPop().forEach(dino -> {
            obstacles.forEach(obstacle -> {
                if(isColliding(dino, obstacle)) {
                    dino.setDead();
                }
            });
        });
    }

    private boolean isColliding(final Player dino, final Obstacle obstacle) {
        final Rectangle dinoBoundingBox = dinoBoundingBox(dino);
        final Rectangle obstacleBoundingBox = obstacleBoundingBox(obstacle);
        return dinoBoundingBox.intersects(obstacleBoundingBox);
    }

    private Rectangle obstacleBoundingBox(final Obstacle obstacle) {
        return new Rectangle((int) obstacle.getXPos(), (int) obstacle.getYPos(),
                (int) obstacle.getWidth(), (int) obstacle.getHeight());
    }

    private Rectangle dinoBoundingBox(final Player dino) {
        return new Rectangle(DINOSAUR_X_POS, (int) dino.getYPos(),
                DINOSAUR_WIDTH, DINOSAUR_HEIGHT);
    }

    private boolean shouldAddObstacle() {
        return Random2.random() < SPAWN_PROBABILITY && time - lastSpawnTime > 50;
    }

    private void updateObstacles() {
        obstacles.forEach(obstacle -> {
            obstacle.setXPos(obstacle.getXPos() - xSpeed);
        });
        obstacles.removeIf(obstacle -> obstacle.getXPos() < 0);
    }

    private void updateDinosaurs() {
        population.getPop().forEach(dino -> {
            final Action action = dino.getBrain().calculateAction(dino, this);
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

    public void jump(final Player dino) {
        if(!dino.isJumping()) {
            dino.setJumping(true);
            dino.setForceUp(FORCE_UP);
        }
    }
}
