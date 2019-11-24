package com.swipecrowd.aigame;

import lombok.Getter;

import javax.swing.JFrame;

public class Gui {
    @Getter
    private GameFrame frame;

    @Getter
    private GamePanel panel;

    private static final int WIDTH = 500;
    private static final int HEIGHT = 500;
    private Emulation emulation;

    public void setup(Emulation emulation) {
        this.emulation = emulation;

        createElements();
        setupProperties();
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

    public void redraw(final int time) {
        panel.setTime(time);
        panel.repaint();
    }

}
