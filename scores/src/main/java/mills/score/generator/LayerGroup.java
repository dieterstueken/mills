package mills.score.generator;

import mills.bits.Clops;
import mills.bits.Player;
import mills.bits.PopCount;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Stream;

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

    public <R extends IndexLayer> LayerGroup<R> map(Function<? super T,? extends R> map) {
        return of(pop, player, group.values().stream().map(map));
    }

    public void add(T layer) {
        group.put(Clops.of(layer), layer);
    }

    @Override
    public PopCount pop() {
        return pop;
    }

    @Override
    public Player player() {
        return player;
    }

    public Stream<T> stream() {
        return group.values().stream();
    }

    //////////////////////////////////////////

    public static <T extends IndexLayer> LayerGroup<T> of(PopCount pop, Player player, Map<Clops, T> group) {
        return new LayerGroup<>(pop, player, group);
    }

    public static <T extends IndexLayer> LayerGroup<T> of(PopCount pop, Player player) {
        return of(pop, player, new HashMap<>());
    }

    public static <T extends IndexLayer> LayerGroup<T> of(PopCount pop, Player player, Stream<? extends T> values) {
        LayerGroup<T> group = of(pop, player, new HashMap<>());
        values.forEach(group::add);
        return group;
    }

}
