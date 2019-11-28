package com.swipecrowd.dinogame.game;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class CollidingTest {
    final Emulation emulation = new Emulation();

    @Test
    public void isCollidingLowBird() {
        final Obstacle bird = emulation.createLowBird(0);
        final Player player = new Player();
        assertThat(isColliding(bird, player)).isTrue();
    }

    @Test
    public void isNotCollidingMediumBird() {
        final Obstacle bird = emulation.createMediumBird(0);
        final Player player = new Player();
        assertThat(isColliding(bird, player)).isFalse();
    }

    @Test
    public void isNotCollidingLowBirdWhenDucking() {
        final Obstacle bird = emulation.createMediumBird(0);
        final Player player = new Player();
        player.setDucking(true);
        assertThat(isColliding(bird, player)).isFalse();
    }

    private boolean isColliding(final Obstacle bird, final Player player) {
        return emulation.isColliding(player, bird);
    }
}
