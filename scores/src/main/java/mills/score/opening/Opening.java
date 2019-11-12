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

    final List<PlopLayer> layers = new ArrayList<>(Plop.COUNT);

    public Opening() {
        for (int i=0; i<Plop.COUNT; ++i) {
            layers.add(new PlopLayer(indexes, i));
        }
    }

    public void run() {
        layers.get(0).play(Clops.EMPTY).set(0);

        for(int i=1; i<Plop.COUNT; ++i) {
            layers.get(i-1).propagate(layers.get(i));
        }
    }


}
