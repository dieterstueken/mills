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

    final List<MoveLayer> layers = new ArrayList<>(Plop.COUNT);

    public void run() {

        if(!layers.isEmpty())
            throw new IllegalStateException();

        MoveLayer layer = new MoveLayer(indexes, 0);
        layer.plops(Clops.EMPTY).set(0);

        while(layers.size()<Plop.COUNT) {
            layers.add(layer);
            MoveLayer next = new MoveLayer(indexes, layers.size());
            next.elevate(layer);
            layer = next;
        }
    }
}
