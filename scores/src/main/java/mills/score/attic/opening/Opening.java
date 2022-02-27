package mills.score.attic.opening;

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


    final List<MovedLayer> layers = new ArrayList<>(Plop.COUNT);

    public void run() {
        try(IndexProvider indexes = IndexProvider.load()) {

            double start = System.currentTimeMillis();

            if (!layers.isEmpty())
                throw new IllegalStateException();

            MovedLayer prev = null;

            for(Plop plop:Plop.LIST) {
                MovedLayer layer = new MovedLayer(indexes, plop);

                if(prev == null) {
                    layer.plops(Clops.EMPTY).set(0);
                } else {
                    layer.elevate(prev);
                }

                double stop = System.currentTimeMillis();
                System.out.format("elevate %s %.3fs\n", layer, (stop - start) / 1000);
                prev = layer;
            }

            double stop = System.currentTimeMillis();
            System.out.format("\n%.3fs\n", (stop - start) / 1000);
        }
    }
}
