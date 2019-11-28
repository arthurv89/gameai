package com.swipecrowd.dinogame.game.ui;

import com.swipecrowd.dinogame.game.Emulation;
import com.swipecrowd.dinogame.game.action.Action;
import com.swipecrowd.dinogame.game.action.DuckingAction;
import com.swipecrowd.dinogame.game.action.JumpAction;
import com.swipecrowd.dinogame.game.action.NullAction;

import javax.swing.JFrame;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import static java.awt.event.KeyEvent.VK_DOWN;
import static java.awt.event.KeyEvent.VK_I;
import static java.awt.event.KeyEvent.VK_K;
import static java.awt.event.KeyEvent.VK_UP;

public class GameFrame extends JFrame implements KeyListener {
    private final Emulation emulation;

    GameFrame(final Emulation emulation) {
        this.emulation = emulation;
        this.addKeyListener(this);
    }

    @Override
    public void keyTyped(final KeyEvent e) { }

    @Override
    public void keyPressed(final KeyEvent e) {
        if(e.getKeyCode() == VK_UP) { // up key
            handleJump();
        } else if(e.getKeyCode() == VK_DOWN) { // down key
            handleDuck();
        } else if(e.getKeyCode() == VK_I) { // i key
            handleIncreaseSpeed();
        } else if(e.getKeyCode() == VK_K) { // k key
            handleDecreaseSpeed();
        }
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
