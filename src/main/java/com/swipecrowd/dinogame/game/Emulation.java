package com.swipecrowd.dinogame.game;

import com.swipecrowd.dinogame.game.action.Action;
import com.swipecrowd.dinogame.game.action.DuckingAction;
import com.swipecrowd.dinogame.game.action.JumpAction;
import com.swipecrowd.dinogame.game.action.NullAction;
import com.swipecrowd.dinogame.game.ui.GamePanel;
import com.swipecrowd.dinogame.game.ui.Gui;
import com.swipecrowd.dinogame.nn.Population;
import com.swipecrowd.dinogame.utils.Random2;
import com.swipecrowd.dinogame.utils.Tick;
import lombok.Getter;

import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Stream;

import static com.swipecrowd.dinogame.game.ui.GamePanel.CACTUS_Y_POS;
import static com.swipecrowd.dinogame.game.ui.GamePanel.HIGH_BIRD_Y_POS;
import static com.swipecrowd.dinogame.game.ui.GamePanel.LOW_BIRD_Y_POS;
import static com.swipecrowd.dinogame.game.ui.GamePanel.MEDIUM_BIRD_Y_POS;
import static com.swipecrowd.dinogame.game.ui.Images.birdImage0;
import static com.swipecrowd.dinogame.game.ui.Images.birdImage1;
import static com.swipecrowd.dinogame.game.ui.Images.cactusBigImage;
import static com.swipecrowd.dinogame.game.ui.Images.cactusSmallImage;
import static com.swipecrowd.dinogame.game.ui.Images.cactusSmallManyImage;

public class Emulation {
    private static final int POP_SIZE = 1000;
    private static double spawnProbability = 0.02;
    private static final double GRAVITY = 1;
    public static final double X_SPEED = 10;
    private static final double FORCE_UP = GRAVITY * 20;
    static ArrayList<Integer> obstacleHistory = new ArrayList<>();
    static ArrayList<Integer> randomAdditionHistory = new ArrayList<>();
    private static final double SPEEDUP = 1.0001;
    private int timeBetweenObstacles = 50;

    @Getter
    private CopyOnWriteArrayList<Obstacle> obstacles = new CopyOnWriteArrayList<>();

    @Getter
    private Population population = new Population(POP_SIZE);

    @Getter
    private int emulationNo = 0;
    private int time;
    private int lastSpawnTime;
    private AtomicLong lastDrawTime = new AtomicLong(0);
    private double fps = 100;

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
        lastDrawTime.set(0);
        time = 0;
        lastSpawnTime = -timeBetweenObstacles;
    }

    private void runGeneration(final Gui gui) {
        int aliveDinos = population.getPop().size();
        while(aliveDinos > 0) {
            Tick.waitTillNextFrame(lastDrawTime, fps);

            updateState(gui.getPanel().getWidth());

            aliveDinos = countAliveDinos();

            gui.redraw(time, aliveDinos, spawnProbability, fps, timeBetweenObstacles);
        }
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
//        fps *= SPEEDUP;
    }

    private Obstacle createObstacle(final int xPos, final double random) {
        if(random < 0.15) {
            return createBigCactus(xPos);
        } else if(random < 0.3) {
            return createSmallManyCacti(xPos);
        } else if(random < 0.45) {
            return createSmallCactus(xPos);
        } else if(random < 0.6) {
            return createHighBird(xPos);
        } else if(random < 0.75) {
            return createMediumBird(xPos);
        } else {
            return createLowBird(xPos);
        }
    }

    Obstacle createMediumBird(final int xPos) {
        return new Obstacle(xPos, MEDIUM_BIRD_Y_POS, Arrays.asList(birdImage0, birdImage1));
    }

    Obstacle createHighBird(final int xPos) {
        return new Obstacle(xPos, HIGH_BIRD_Y_POS, Arrays.asList(birdImage0, birdImage1));
    }

    Obstacle createLowBird(final int xPos) {
        return new Obstacle(xPos, LOW_BIRD_Y_POS, Arrays.asList(birdImage0, birdImage1));
    }

    Obstacle createBigCactus(final int xPos) {
        return new Obstacle(xPos, CACTUS_Y_POS, cactusBigImage);
    }

    Obstacle createSmallCactus(final int xPos) {
        return new Obstacle(xPos, CACTUS_Y_POS, cactusSmallImage);
    }

    Obstacle createSmallManyCacti(final int xPos) {
        return new Obstacle(xPos, CACTUS_Y_POS, cactusSmallManyImage);
    }

    private void incrementScore() {
        getAlivePlayers().forEach(x -> x.incrementScore());
    }

    public void applyAction(final Action action, final Player dino) {
        if(action instanceof JumpAction) {
            jump(dino);
        } else if(action instanceof NullAction) {
            unduck(dino);
            // Do nothing
        } else if(action instanceof DuckingAction) {
            duck(dino);
        } else {
            throw new RuntimeException("Don't recognise this action: " + action.getClass().getName());
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

    boolean isColliding(final Player dino, final Obstacle obstacle) {
        final Rectangle dinoBoundingBox = dinoBoundingBox(dino);
        final Rectangle obstacleBoundingBox = obstacleBoundingBox(obstacle);
        return dinoBoundingBox.intersects(obstacleBoundingBox);
    }

    private Rectangle obstacleBoundingBox(final Obstacle obstacle) {
        return new Rectangle((int) obstacle.getXPos(), (int) obstacle.getYPos(),
                obstacle.getWidth(), obstacle.getHeight());
    }

    private Rectangle dinoBoundingBox(final Player dino) {
        final BufferedImage currentImage = animated(dino.getCurrentImages());
        return new Rectangle(GamePanel.DINOSAUR_X_POS, (int) dino.getYPos(),
                currentImage.getWidth(), currentImage.getHeight());
    }

    public BufferedImage animated(final List<BufferedImage> images) {
        return images.get(time % images.size());
    }

    private boolean shouldAddObstacle() {
        final double rand = Random2.random();
        return rand < spawnProbability && time - lastSpawnTime > timeBetweenObstacles;
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
        if(!dino.isJumping() && !dino.isDucking()) {
            dino.setJumping(true);
            dino.setForceUp(FORCE_UP);
        }
    }

    public void increaseSpeed() {
        fps *= 1.3;
    }

    public void decreaseSpeed() {
        final double newFps = fps / 1.3;
        if(newFps > 0) {
            fps = newFps;
        }
    }

    public void duck(final Player dino) {
        if(!dino.isJumping() && !dino.isDucking()) {
            dino.setDucking(true);
        }
    }

    public void unduck(final Player dino) {
        if(dino.isDucking()) {
            dino.setDucking(false);
        }
    }

    public void increaseSpawn() {
        spawnProbability *= 1.1;
    }

    public void decreaseSpawn() {
        spawnProbability /= 1.1;
    }

    public void decreaseTimeInBetweenObstacles() {
        timeBetweenObstacles /= 1.1;
    }

    public void increaseTimeInBetweenObstacles() {
        timeBetweenObstacles *= 1.1;
    }
}
