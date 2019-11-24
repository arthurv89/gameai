package com.swipecrowd.aigame;

public class Emulation {
    private static final int POP_SIZE = 1;
    private static final int FPS = 100;

    private static final double GRAVITY = 1;
    private double forceUp;
    private static final double FORCE_UP = GRAVITY * 20;
    private boolean jumping = false;
    private Population pop = new Population(POP_SIZE);;
    private long lastDrawTime = 0;
    private final double timeInBetween = 1000 / FPS;

    public void start() {
        final Gui gui = new Gui();
        gui.setup(pop, this);

        run(pop, gui);
    }

    private void run(final Population pop, final Gui gui) {
        while (true) {
            runGeneration(pop, gui);

            pop.naturalSelection();
            gui.removeObstacles();
        }
    }

    private void runGeneration(final Population pop, final Gui gui) {
        while(!pop.done()) {
            waitTillNextDraw();
            updateDinosaurPositions(pop);

            gui.draw(pop);
            pop.updateAlive();
        }
    }

    private void waitTillNextDraw() {
        if (lastDrawTime == 0) {
            lastDrawTime = System.currentTimeMillis();
        } else {
            // Wait a bit until we're allowed to draw the frame
            final long currentTime = System.currentTimeMillis();
            if(currentTime - lastDrawTime < timeInBetween) {
                try {
                    Thread.sleep((long) (lastDrawTime + timeInBetween - currentTime));
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
            lastDrawTime += timeInBetween;
        }
    }

    private void updateDinosaurPositions(final Population pop) {
        pop.getDinosaurs().forEach(dino -> {
            System.out.printf("Force up %s, Y = %s %n", forceUp, dino.getYPos());
            dino.goUp(this.forceUp);

            if(dino.getYPos() < 0) {
                dino.setYPos(0);
                this.forceUp = 0;
            }
            if(dino.getYPos() == 0) {
                jumping = false;
            } else {
                this.forceUp -= GRAVITY;
            }
        });
    }

    public void jump() {
        if(!jumping) {
            jumping = true;
            forceUp = FORCE_UP;
        }
    }

    public Population getDinosaurs() {
        return pop;
    }
}
