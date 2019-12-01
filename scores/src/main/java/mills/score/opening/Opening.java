package mills.score.opening;

import mills.bits.Clops;
import mills.index.IndexProvider;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 09.11.19
 * Time: 17:41
 */
public class Opening {

    final IndexProvider indexes = IndexProvider.load().lazy();

    final List<MovedLayer> layers = new ArrayList<>(Plop.COUNT);

    public void run() {
        double start = System.currentTimeMillis();

        if(!layers.isEmpty())
            throw new IllegalStateException();

        MovedLayer prev = new MovedLayer(indexes, 0);
        prev.plops(Clops.EMPTY).set(0);

        for(int l = 1; l<Plop.COUNT; ++l) {
            MovedLayer layer = new MovedLayer(indexes, l);
            layer.elevate(prev);
            prev = layer;

            double stop = System.currentTimeMillis();
            System.out.format("elevate %s %.3fs\n", layer, (stop - start) / 1000);
        }

        double stop = System.currentTimeMillis();
        System.out.format("\n%.3fs\n", (stop - start) / 1000);
    }
}
