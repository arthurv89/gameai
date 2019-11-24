package com.swipecrowd.aigame;

import javax.swing.JFrame;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

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
        emulation.jump();
    }

    @Override
    public void keyReleased(final KeyEvent e) { }
}
