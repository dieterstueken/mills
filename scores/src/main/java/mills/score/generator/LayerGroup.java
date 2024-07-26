package mills.score.generator;

import mills.bits.Player;
import mills.bits.PopCount;
import mills.util.AbstractRandomList;
import mills.util.listset.ListSet;
import mills.util.listset.PopMap;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ForkJoinTask;
import java.util.function.Function;
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

    final Map<PopCount, ? extends T> group;

    LayerGroup(PopCount pop, Player player, Map<PopCount, ? extends T> group) {
        this.group = group;
        this.pop = pop;
        this.player = player;
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

    @Override
    public String toString() {
        return String.format("%s%s[%d]", pop, player.key(), group.size());
    }

    /**
     * Generate a group map from a stream of clops and generator function.
     * The group is created in parallel.
     * @param clops stream of clops.
     * @param generator function.
     * @return a clop map.
     * @param <T> target objects to generate.
     */
    static <T extends ClopLayer> Map<PopCount, T>
    group(Stream<PopCount> clops, Function<PopCount, T> generator) {
        ListSet<PopCount> clist = ListSet.of(clops.toArray(PopCount[]::new));
        Function<PopCount, ForkJoinTask<T>> task = clop -> ForkJoinTask.adapt(() -> generator.apply(clop));
        List<ForkJoinTask<T>> tasks = AbstractRandomList.map(clist, task);
        ForkJoinTask.invokeAll(tasks);
        List<T> values = AbstractRandomList.map(tasks, ForkJoinTask::join);
        return PopMap.of(clist, values);
    }
}
