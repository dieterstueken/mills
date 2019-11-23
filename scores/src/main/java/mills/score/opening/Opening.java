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

        MovedLayer layer = new MovedLayer(indexes, 0);
        layer.plops(Clops.EMPTY).set(0);

        while(layers.size()<Plop.COUNT) {
            layers.add(layer);
            MovedLayer next = new MovedLayer(indexes, layers.size());
            double stop = System.currentTimeMillis();
            System.out.format("elevate %s %.3fs\n", layer, (stop - start) / 1000);
            next.elevate(layer);
            layer = next;
        }

        double stop = System.currentTimeMillis();
        System.out.format("\n%.3fs\n", (stop - start) / 1000);
    }
}
