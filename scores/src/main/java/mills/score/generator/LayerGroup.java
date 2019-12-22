package mills.score.generator;

import mills.bits.Clops;
import mills.bits.Player;
import mills.bits.PopCount;

import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 02.11.19
 * Time: 09:32
 */
public class LayerGroup<T extends IndexLayer> implements Layer {

    final PopCount pop;

    final Player player;

    final Map<Clops, T> group;

    public LayerGroup(PopCount pop, Player player, Map<Clops, T> group) {
        this.group = group;
        this.pop = pop;
        this.player = player;
    }

    public LayerGroup(Layer layer, Map<Clops, T> group) {
        this(layer.pop(), layer.player(), group);
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
