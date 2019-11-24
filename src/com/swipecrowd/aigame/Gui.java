package com.swipecrowd.aigame;

import lombok.Getter;

import javax.swing.JFrame;

public class Gui {
    @Getter
    private JFrame frame;

    @Getter
    private DinoPanel panel;

    private int WIDTH = 500;
    private int HEIGHT = 500;

    public void setup(final Population pop, final Emulation emulation) {
        createElements(pop, emulation);
        setupProperties();
    }

    private void createElements(final Population pop, final Emulation emulation) {
        frame = new JFrame();
        panel = new DinoPanel(emulation);
    }

    private void setupProperties() {
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(WIDTH, HEIGHT);
        frame.getContentPane().add(panel);
        frame.setVisible(true);

        panel.setFocusable(true);
    }

    public void draw(final Population pop) {
        panel.repaint();
    }

    public void removeObstacles() {

    }
}
