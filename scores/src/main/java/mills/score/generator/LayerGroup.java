package mills.score.generator;

import mills.bits.Player;
import mills.bits.PopCount;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 02.11.19
 * Time: 09:32
 */
public class LayerGroup<T extends IndexLayer> extends Group<T> implements Layer {

    final PopCount pop;

    final Player player;

    public LayerGroup(PopCount pop, Player player) {
        this.pop = pop;
        this.player = player;
    }

    public LayerGroup(Layer layer) {
        this(layer.pop(), layer.player());
    }

    @Override
    public PopCount pop() {
        return pop;
    }

    @Override
    public Player player() {
        return player;
    }
}
