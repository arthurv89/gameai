package com.swipecrowd.dinogame.game.ui;

import com.google.common.collect.ImmutableMap;
import com.swipecrowd.dinogame.game.Emulation;
import com.swipecrowd.dinogame.game.action.Action;
import com.swipecrowd.dinogame.game.action.DuckingAction;
import com.swipecrowd.dinogame.game.action.JumpAction;
import com.swipecrowd.dinogame.game.action.NullAction;

import javax.swing.JFrame;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.Map;

import static java.awt.event.KeyEvent.VK_D;
import static java.awt.event.KeyEvent.VK_DOWN;
import static java.awt.event.KeyEvent.VK_E;
import static java.awt.event.KeyEvent.VK_I;
import static java.awt.event.KeyEvent.VK_K;
import static java.awt.event.KeyEvent.VK_S;
import static java.awt.event.KeyEvent.VK_UP;
import static java.awt.event.KeyEvent.VK_W;

public class GameFrame extends JFrame implements KeyListener {
    private final Emulation emulation;
    private Map<Integer, Runnable> map = ImmutableMap.<Integer, Runnable>builder()
        .put(VK_UP,   () -> handleJump())
        .put(VK_DOWN, () -> handleDuck())
        .put(VK_I,    () -> handleIncreaseSpeed())
        .put(VK_K,    () -> handleDecreaseSpeed())
        .put(VK_W,    () -> handleIncreaseSpawnRate())
        .put(VK_S,    () -> handleDecreaseSpawnRate())
        .put(VK_E,    () -> handleIncreaseTimeInBetweenObstacles())
        .put(VK_D,    () -> handleDecreaseTimeInBetweenObstacles())
        .build();

    private void handleDecreaseTimeInBetweenObstacles() {
        emulation.decreaseTimeInBetweenObstacles();
    }

    private void handleIncreaseTimeInBetweenObstacles() {
        emulation.increaseTimeInBetweenObstacles();
    }

    GameFrame(final Emulation emulation) {
        this.emulation = emulation;
        this.addKeyListener(this);
    }

    @Override
    public void keyTyped(final KeyEvent e) { }

    @Override
    public void keyPressed(final KeyEvent e) {
        map.get(e.getKeyCode()).run();
    }

    private void handleDecreaseSpawnRate() {
        emulation.decreaseSpawn();
    }

    private void handleIncreaseSpawnRate() {
        emulation.increaseSpawn();
    }

    private void handleDuck() {
        performAction(new DuckingAction());
    }

    private void handleIncreaseSpeed() {
        emulation.increaseSpeed();
    }

    private void handleDecreaseSpeed() {
        emulation.decreaseSpeed();
    }

    private void handleJump() {
        performAction(new JumpAction());
    }

    @Override
    public void keyReleased(final KeyEvent e) {
        final NullAction action = new NullAction();
        performAction(action);
    }

    private void performAction(final Action action) {
        emulation.getPopulation().getPop().forEach(dino -> {
            emulation.applyAction(action, dino);
        });
    }
}
