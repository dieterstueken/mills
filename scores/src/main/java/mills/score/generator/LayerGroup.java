package mills.score.generator;

import mills.bits.Player;
import mills.bits.PopCount;

import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 02.11.19
 * Time: 09:32
 */
public class LayerGroup<T extends ClopLayer> implements Layer {

    final PopCount pop;

    final Player player;

    final Map<PopCount, T> group;

    LayerGroup(PopCount pop, Player player, Map<PopCount, T> group) {
        this.group = group;
        this.pop = pop;
        this.player = player;
    }

    LayerGroup(PopCount pop, Player player, Stream<? extends T> layers) {
        this(pop, player, layers.collect(Collectors.toMap(ClopLayer::clop, Function.identity())));
    }

    @Override
    public PopCount pop() {
        return pop;
    }

    @Override
    public Player player() {
        return player;
    }

    public Stream<? extends T> stream() {
        return group.values().stream();
    }

    public Stream<? extends T> parallelStream() {
        return group.values().parallelStream();
    }

    @Override
    public String toString() {
        return String.format("%s%s[%d]", pop, player.key(), group.size());
    }
}
