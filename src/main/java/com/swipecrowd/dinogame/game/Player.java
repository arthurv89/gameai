package com.swipecrowd.dinogame.game;

import com.swipecrowd.dinogame.nn.Genome;
import com.swipecrowd.dinogame.utils.Random2;
import lombok.Getter;
import lombok.Setter;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.swipecrowd.dinogame.game.ui.Images.dinoDuck0;
import static com.swipecrowd.dinogame.game.ui.Images.dinoDuck1;
import static com.swipecrowd.dinogame.game.ui.Images.dinoJumpingImage;
import static com.swipecrowd.dinogame.game.ui.Images.dinoRunningImage0;
import static com.swipecrowd.dinogame.game.ui.Images.dinoRunningImage1;

public class Player {
    public double fitness;
    @Getter
    public Genome brain = new Genome();

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

    @Getter
    @Setter
    private boolean ducking = false;

    @Getter
    private Color color = Color.getHSBColor((float) Random2.random(), 1, 1);

    public int gen = 0;
    int bestScore =0;//stores the score achieved used for replay
    public int score;
    boolean replay = false;
    ArrayList<Integer> localObstacleHistory = new ArrayList<Integer>();
    ArrayList<Integer> localRandomAdditionHistory = new ArrayList<Integer>();

    public void goUp(final double yDiff) {
        yPos += yDiff;
    }

    public void setYPos(final double yPos) {
        this.yPos = yPos;
    }

    public void setDead() {
        this.dead = true;
    }

    //returns a clone of this player with the same brian
    public Player clone() {
        Player clone = new Player();
        clone.brain = brain.clone();
        clone.fitness = fitness;
        clone.brain.generateNetwork();
        clone.gen = gen;
        clone.color = color;
        clone.bestScore = score;
        return clone;
    }

    public Player crossover(Player parent2) {
        Player child = new Player();
        child.brain = brain.crossover(parent2.brain);
        child.brain.generateNetwork();
        return child;
    }

    public Player cloneForReplay() {
        Player clone = new Player();
        clone.brain = brain.clone();
        clone.color = color;
        clone.fitness = fitness;
        clone.brain.generateNetwork();
        clone.gen = gen;
        clone.bestScore = score;
        clone.replay = true;
        if (replay) {
            clone.localObstacleHistory = (ArrayList)localObstacleHistory.clone();
            clone.localRandomAdditionHistory = (ArrayList)localRandomAdditionHistory.clone();
        } else {
            clone.localObstacleHistory = (ArrayList)Emulation.obstacleHistory.clone();
            clone.localRandomAdditionHistory = (ArrayList<Integer>) Emulation.randomAdditionHistory.clone();
        }

        return clone;
    }

    public void calculateFitness() {
        fitness = score*score;
    }

    public void incrementScore() {
        score++;
    }

    public List<BufferedImage> getCurrentImages() {
        if(isJumping()) {
            return Arrays.asList(dinoJumpingImage);
        } else if(isDucking()) {
            return Arrays.asList(dinoDuck0, dinoDuck1);
        } else {
            return Arrays.asList(dinoRunningImage0, dinoRunningImage1);
        }
    }
}
