package mills.score.generator;

import mills.bits.Clops;
import mills.bits.Player;
import mills.bits.PopCount;

import java.util.HashMap;
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
public class LayerGroup<T extends IndexLayer> implements Layer {

    final PopCount pop;

    final Player player;

    final Map<Clops, T> group;

    public LayerGroup(PopCount pop, Player player, Map<Clops, T> group) {
        this.group = group;
        this.pop = pop;
        this.player = player;
    }

    public LayerGroup(PopCount pop, Player player, Stream<? extends T> layers) {
          this.group = layers.collect(Collectors.toMap(Clops::of, Function.identity()));
          this.pop = pop;
          this.player = player;
      }

    public <R extends IndexLayer> LayerGroup<R> map(Function<? super T,? extends R> map) {
        return of(pop, player, group.values().stream().map(map));
    }

    @Override
    public PopCount pop() {
        return pop;
    }

    @Override
    public Player player() {
        return player;
    }

    public int range() {
        int range=0;

        for (T layer : group.values()) {
            range += layer.index().range();
        }

        return range;
    }

    public Stream<? extends T> stream() {
        return group.values().stream();
    }

    @Override
    public String toString() {
        return String.format("%s%s[%d]", pop, player.key(), group.size());
    }

    //////////////////////////////////////////

    public static <T extends IndexLayer> LayerGroup<T> of(PopCount pop, Player player, Map<Clops, T> group) {
        return new LayerGroup<>(pop, player, group);
    }

    public static <T extends IndexLayer> LayerGroup<T> of(PopCount pop, Player player) {
        return of(pop, player, new HashMap<>());
    }

    public static <T extends IndexLayer> LayerGroup<T> of(PopCount pop, Player player, Stream<? extends T> values) {
        return new LayerGroup<>(pop, player, values);
    }

}
