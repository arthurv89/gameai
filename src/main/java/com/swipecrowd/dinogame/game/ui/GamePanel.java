package com.swipecrowd.dinogame.game.ui;

import com.swipecrowd.dinogame.game.Emulation;
import com.swipecrowd.dinogame.game.Obstacle;
import com.swipecrowd.dinogame.game.Player;
import com.swipecrowd.dinogame.nn.Population;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import javax.swing.JPanel;
import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicInteger;

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
    @Setter
    private int aliveDinos;
    @Setter
    private double spawnRate;
    @Setter
    private double speed;
    @Setter
    private double timeBetweenObstacles;

    private void drawScreen(final Graphics2D g) {
        drawBackground(g);
        drawText(g, createDebugMap());
        drawDinosaurs(g, emulation.getPopulation());
        drawObstacles(g);
    }

    private Map<String, String> createDebugMap() {
        final Map<String, String> map = new LinkedHashMap<>();
        map.put("Time", String.valueOf(time));
        map.put("Iteration", String.valueOf(emulation.getEmulationNo()));
        map.put("Dino count", String.valueOf(aliveDinos));
        map.put("Speed", String.valueOf(speed));
        map.put("Spawn rate", String.valueOf(spawnRate));
        map.put("Time between obstacles", String.valueOf(timeBetweenObstacles));
        return map;
    }

    private void drawText(final Graphics2D g, final Map<String, String> map) {
        g.setColor(Color.BLACK);
        final AtomicInteger i = new AtomicInteger(1);
        map.forEach((k, v) -> {
            final int j = i.getAndIncrement();
            g.drawString(k + ": " + v, 0, j * 20);
        });
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
        Map<String, Callable<?>> map = new HashMap<>();
        pop.getPop().iterator().forEachRemaining(x -> {
            if(!x.isDead()) {
                final double y = x.getYPos();
                final int d = x.isDucking() ? 1 : 0;
                final int j = x.isJumping() ? 1 : 0;
                map.put(String.format("%s,%s,%s", y, d, j), () -> {
                    drawDinosaur(g, x);
                    return null;
                });
            }
        });
        map.forEach((x, y) -> {
            try {
                y.call();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    private void drawBackground(final Graphics g) {
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, getWidth(), getHeight());
    }

    private void drawDinosaur(final Graphics g, final Player player) {
        final List<BufferedImage> images = player.getCurrentImages();
        final BufferedImage image = emulation.animated(images);

        BufferedImage transparentImage = new BufferedImage(image.getWidth(), image.getHeight(),
                BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = (Graphics2D) transparentImage.getGraphics();
        g2d.setComposite(AlphaComposite.SrcOver.derive(0.5f));
        // set the transparency level in range 0.0f - 1.0f
        g2d.drawImage(image, 0, 0, null);

        drawDinoImage(g, transparentImage, player);
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
}
