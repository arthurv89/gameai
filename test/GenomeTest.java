import com.swipecrowd.aigame.ai.Genome;
import org.junit.Test;

public class GenomeTest {
    @Test
    public void testNetwork() {
        final Genome genome = new Genome();
        System.out.println(genome.getNodes());
    }
}
