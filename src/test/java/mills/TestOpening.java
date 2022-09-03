package mills;

import mills.bits.Clops;
import mills.index.IndexProvider;
import mills.score.attic.opening.MovedLayer;
import mills.score.attic.opening.Plop;
import org.junit.jupiter.api.Test;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 12.11.19
 * Time: 22:39
 */
public class TestOpening {

    @Test
    public void testOpening() {
        try(IndexProvider indexes = IndexProvider.load()) {

            double start = System.currentTimeMillis();

            MovedLayer layer = new MovedLayer(indexes, Plop.EMPTY);
            layer.plops(Clops.EMPTY).set(0);

            while(layer!=null) {
                double stop = System.currentTimeMillis();
                System.out.format("elevate %s %.3fs\n", layer, (stop - start) / 1000);
                layer = layer.next();
            }

            double stop = System.currentTimeMillis();
            System.out.format("\n%.3fs\n", (stop - start) / 1000);
        }
    }
}
