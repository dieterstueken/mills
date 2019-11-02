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

    final boolean opening;

    public LayerGroup(PopCount pop, Player player, boolean opening) {
        this.pop = pop;
        this.player = player;
        this.opening = opening;
    }

    public LayerGroup(Layer layer) {
        this(layer.pop(), layer.player(), layer.opening());
    }

    @Override
    public PopCount pop() {
        return pop;
    }

    @Override
    public Player player() {
        return player;
    }

    @Override
    public boolean opening() {
        return opening;
    }
}
