package com.swipecrowd.dinogame.nn;

import com.swipecrowd.dinogame.game.Emulation;
import com.swipecrowd.dinogame.game.Player;
import org.junit.Test;

import static com.swipecrowd.dinogame.nn.Genome.inputs;
import static org.assertj.core.api.Assertions.assertThat;

public class GenomeTest {
    @Test
    public void testVisionSize() {
        final Genome genome = new Genome();
        final Player dino = new Player();
        final Emulation emulation = new Emulation();
        final double[] vision = genome.getVision(dino, emulation);
        assertThat(vision).hasSize(inputs);
    }
}
