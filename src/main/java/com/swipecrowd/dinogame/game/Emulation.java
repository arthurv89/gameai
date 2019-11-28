package com.swipecrowd.dinogame.game;

import com.swipecrowd.dinogame.game.action.Action;
import com.swipecrowd.dinogame.game.action.JumpAction;
import com.swipecrowd.dinogame.nn.Population;
import com.swipecrowd.dinogame.nn.Random2;
import com.swipecrowd.dinogame.ui.GamePanel;
import com.swipecrowd.dinogame.ui.Gui;
import com.swipecrowd.dinogame.ui.Images;
import lombok.Getter;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Stream;

public class Emulation {
    private static final int POP_SIZE = 100;
    private static int fps = 100;
    private static final double SPAWN_PROBABILITY = 0.02;
    private static final double GRAVITY = 1;
    public static final double X_SPEED = 10;
    private static final double FORCE_UP = GRAVITY * 20;
    static ArrayList<Integer> obstacleHistory = new ArrayList<>();
    static ArrayList<Integer> randomAdditionHistory = new ArrayList<>();
    private static final double SPEEDUP = 1.0001;

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
        lastSpawnTime = 0;
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
            if(currentTime - lastDrawTime < getTimeInBetween()) {
                try {
                    Thread.sleep((long) (lastDrawTime + getTimeInBetween() - currentTime));
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
            lastDrawTime += getTimeInBetween();
        }
    }

    private double getTimeInBetween() {
        return 1000 / fps;
    }

    private void updateState(final int frameWidth) {
        if(shouldAddObstacle()) {
            final double random = Random2.random();
            final Obstacle obstacle = createObstacle(frameWidth, random);
            obstacles.add(obstacle);
            lastSpawnTime = time;
        }

        updateDinosaurs();
        updateObstacles();
        incrementScore();

        markDeadDinos();
        time++;
        fps *= SPEEDUP;
    }

    private Obstacle createObstacle(final int frameWidth, final double random) {
        if(random < 0.3) {
            return createBigObstacle(frameWidth);
        } else if(random < 0.6) {
            return createSmallObstacle(frameWidth);
        } else {
            return createSmallManyObstacle(frameWidth);
        }
    }

    private Obstacle createBigObstacle(final int frameWidth) {
        return new Obstacle(frameWidth, GamePanel.OBSTACLE_Y_POS, Images.cactusBigImage);
    }

    private Obstacle createSmallObstacle(final int frameWidth) {
        return new Obstacle(frameWidth, GamePanel.OBSTACLE_Y_POS, Images.cactusSmallImage);
    }

    private Obstacle createSmallManyObstacle(final int frameWidth) {
        return new Obstacle(frameWidth, GamePanel.OBSTACLE_Y_POS, Images.cactusSmallManyImage);
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
                obstacle.getImage().getWidth(), obstacle.getImage().getHeight());
    }

    private Rectangle dinoBoundingBox(final Player dino) {
        return new Rectangle(GamePanel.DINOSAUR_X_POS, (int) dino.getYPos(),
                GamePanel.DINOSAUR_WIDTH, GamePanel.DINOSAUR_HEIGHT);
    }

    private boolean shouldAddObstacle() {
        final double rand = Random2.random();
        return rand < SPAWN_PROBABILITY && time - lastSpawnTime > 50;
    }

    private void updateObstacles() {
        obstacles.forEach(obstacle -> {
            obstacle.setXPos(obstacle.getXPos() - X_SPEED);
        });
        obstacles.removeIf(obstacle -> obstacle.getXPos() + obstacle.getWidth() < 0);
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
