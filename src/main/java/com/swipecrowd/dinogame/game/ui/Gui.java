package com.swipecrowd.dinogame.game.ui;

import com.swipecrowd.dinogame.game.Emulation;
import com.swipecrowd.dinogame.utils.Tick;
import lombok.Getter;

import javax.swing.JFrame;
import java.util.concurrent.atomic.AtomicLong;

public class Gui {
    @Getter
    private GameFrame frame;

    @Getter
    private GamePanel panel;

    private static final int WIDTH = 1280;
    private static final int HEIGHT = 800;
    private Emulation emulation;
    private AtomicLong lastDrawFrame = new AtomicLong(0);
    private double fps = 100;

    public void setup(Emulation emulation) {
        this.emulation = emulation;

        createElements();
        setupProperties();
        startGuiThread();
    }

    private void startGuiThread() {
        new Thread(() -> {
            while(true) {
                Tick.waitTillNextFrame(lastDrawFrame, fps);
                panel.repaint();
            }
        }).start();
    }

    private void createElements() {
        frame = new GameFrame(emulation);
        panel = new GamePanel(emulation);
    }

    private void setupProperties() {
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(WIDTH, HEIGHT);
        frame.getContentPane().add(panel);
        frame.setVisible(true);
        frame.setFocusable(true);
    }

    public void

    redraw(final int time,
           final int aliveDinos,
           final double spawnRate,
           final double speed,
           final double timeBetweenObstacles) {
        panel.setTime(time);
        panel.setAliveDinos(aliveDinos);
        panel.setSpawnRate(spawnRate);
        panel.setSpeed(speed);
        panel.setTimeBetweenObstacles(timeBetweenObstacles);
    }

}
