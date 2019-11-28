package com.swipecrowd.dinogame.game.ui;

import com.swipecrowd.dinogame.game.Emulation;
import com.swipecrowd.dinogame.game.Obstacle;
import com.swipecrowd.dinogame.game.Player;
import com.swipecrowd.dinogame.nn.Population;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import javax.swing.JPanel;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
public class GamePanel extends JPanel {
    public static final int DINOSAUR_X_POS = 0;
    public static final double CACTUS_Y_POS = 0;
    public static final double HIGH_BIRD_Y_POS = 200;
    public static final double MEDIUM_BIRD_Y_POS = 130;
    public static final double LOW_BIRD_Y_POS = 70;

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
        final BufferedImage image = emulation.animated(obstacle.getImages());
        final int height = image.getHeight();
        g.drawImage(image,
                (int) obstacle.getXPos(),
                bottomY(obstacle.getYPos(), height),
                null);
    }

    private void drawDinosaurs(final Graphics g, final Population pop) {
        pop.getPop().iterator().forEachRemaining(x -> {
            if(!x.isDead()) {
                g.setColor(x.getColor());
                drawDinosaur(g, x);
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

    private void drawDinosaur(final Graphics g, final Player player) {
        final List<BufferedImage> images = player.getCurrentImages();
        final BufferedImage image = emulation.animated(images);
        drawDinoImage(g, image, player);
    }

    private void drawDinoImage(final Graphics g, final BufferedImage image, final Player player) {
        g.drawImage(image, DINOSAUR_X_POS, bottomY(player.getYPos(), image.getHeight()), null);
    }

    private int bottomY(final double yPos, final int height) {
        return (int) (getHeight() - yPos - height);
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