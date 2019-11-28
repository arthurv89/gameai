package com.swipecrowd.aigame;

import com.swipecrowd.aigame.ai.Population;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import javax.swing.JPanel;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;

@RequiredArgsConstructor
public class GamePanel extends JPanel {
    public static final int DINOSAUR_X_POS = 0;
    public static final int DINOSAUR_WIDTH = 50;
    public static final int DINOSAUR_HEIGHT = 50;
    public static final double OBSTACLE_Y_POS = 0;
    public static final double OBSTACLE_WIDTH = 30;
    public static final double OBSTACLE_HEIGHT = 60;

    private final Emulation emulation;
    private static final Map<?, ?> renderingHints = createRenderingHints();
    @Setter
    private int time;
    private int aliveDinos;


    private void drawScreen(final Graphics2D g) {
        drawBackground(g);
        drawTime(g);
        drawIteration(g, emulation.getEmulationNo());
        drawDinoCount(g, aliveDinos);
        drawDinosaurs(g, emulation.getPopulation());
        drawObstacles(g);
    }

    private void drawDinoCount(final Graphics2D g, final int aliveDinos) {
        g.setColor(Color.BLACK);
        g.drawString(String.valueOf(aliveDinos), 0, 60);
    }

    private void drawIteration(final Graphics2D g, final int emulationNo) {
        g.setColor(Color.BLACK);
        g.drawString(String.valueOf(emulationNo), 0, 40);

    }

    private void drawObstacles(final Graphics g) {
        emulation.getObstacles().listIterator().forEachRemaining(obstacle -> {
            drawObstacle(g, obstacle);
        });
    }

    private void drawObstacle(final Graphics g, final Obstacle obstacle) {
        g.setColor(Color.RED);
        g.fillRect(
                (int) obstacle.getXPos(),
                bottomY((int) obstacle.getYPos()),
                (int) obstacle.getWidth(),
                (int) obstacle.getHeight());
    }

    private void drawDinosaurs(final Graphics g, final Population pop) {
        pop.getPop().iterator().forEachRemaining(x -> {
            if(!x.isDead()) {
                g.setColor(x.getColor());
                drawDinosaur(g, x.getYPos());
            }
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
        g.drawRect(DINOSAUR_X_POS,
                bottomY(yPos),
                DINOSAUR_WIDTH,
                DINOSAUR_HEIGHT);
    }

    private int bottomY(final double yPos) {
        return (int) (getHeight() - yPos - DINOSAUR_HEIGHT);
    }


    @Override
    public void paintComponent(Graphics g){
        super.paintComponent(g);

        final BufferedImage image = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_RGB);
        Graphics2D gr = image.createGraphics();
        gr.setRenderingHints(renderingHints);
        gr.setColor(Color.WHITE);
        gr.fillRect(0, 0, image.getWidth(), image.getHeight());

        drawScreen(gr);

        g.drawImage(image, 0, 0, null);
        gr.dispose();
    }

    private static Map<?, ?> createRenderingHints() {
        Map<RenderingHints.Key, Object> hintsMap = new HashMap<>();
        hintsMap.put(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        hintsMap.put(RenderingHints.KEY_DITHERING, RenderingHints.VALUE_DITHER_ENABLE);
        hintsMap.put(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        return new RenderingHints(hintsMap);
    }

    public void setAliveDinos(final int aliveDinos) {
        this.aliveDinos = aliveDinos;
    }
}
