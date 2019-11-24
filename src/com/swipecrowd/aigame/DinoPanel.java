package com.swipecrowd.aigame;

import javax.swing.JPanel;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;

public class DinoPanel extends JPanel implements KeyListener {
    private int time = 0;
    private final int DINOSAUR_HEIGHT = 50;

    private Emulation emulation;
    private Map<?, ?> renderingHints = createRenderingHints();

    private Map<?, ?> createRenderingHints() {
        Map<RenderingHints.Key, Object> hintsMap = new HashMap<>();
        hintsMap.put(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        hintsMap.put(RenderingHints.KEY_DITHERING, RenderingHints.VALUE_DITHER_ENABLE);
        hintsMap.put(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        renderingHints = new RenderingHints(hintsMap);
        return hintsMap;
    }

    DinoPanel(final Emulation emulation) {
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

    @Override
    public void paintComponent(Graphics g){
        super.paintComponent(g);

        final BufferedImage image = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_RGB);
        Graphics2D gr = image.createGraphics();
        gr.setRenderingHints(renderingHints);
        gr.setColor(Color.WHITE);
        gr.fillRect(0, 0, image.getWidth(), image.getHeight());

        drawBackground(gr);
        drawTime(gr);
        drawDinosaurs(gr, emulation.getPopulation());
        drawObstacles(gr);

        g.drawImage(image, 0, 0, null);
        gr.dispose();

        time++;
    }

    private void drawObstacles(final Graphics g) {
        emulation.getObstacles().forEach(obstacle -> {
            drawObstacle(g, obstacle);
        });
    }

    private void drawObstacle(final Graphics g, final Obstacle obstacle) {
        g.setColor(Color.RED);
        g.fillRect(
                (int) obstacle.getXPos(),
                bottomY((int) obstacle.getYPos()),
                20,
                20);
    }

    private void drawDinosaurs(final Graphics g, final Population pop) {
        pop.getDinosaurs().forEach(x -> {
            drawDinosaur(g, x.getYPos());
        });
    }

    private void drawTime(final Graphics g) {
        g.setColor(Color.BLACK);
        g.drawString(String.valueOf(time), 0, 20);
    }

    private void drawBackground(final Graphics g) {
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, getWidth(), getHeight());
    }

    private void drawDinosaur(final Graphics g, final double yPos) {
        g.setColor(Color.BLACK);
        g.fillRect(0,
                bottomY(yPos),
                20,
                DINOSAUR_HEIGHT);
    }

    private int bottomY(final double yPos) {
        return (int) (getHeight() - yPos - DINOSAUR_HEIGHT);
    }

    public void removeObstacles() {

    }
}
