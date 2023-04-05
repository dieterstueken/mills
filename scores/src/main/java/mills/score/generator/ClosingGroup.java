package mills.score.generator;

import mills.bits.Player;
import mills.bits.PopCount;
import mills.index.IndexProvider;
import mills.index.PosIndex;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Stream;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 27.12.19
 * Time: 18:55
 */
public class ClosingGroup<Slices extends ScoreSlices> extends MovingGroup<Slices> {

    @Override
    public boolean closing() {
        return true;
    }

    public ClosingGroup(PopCount pop, Player player, Stream<Slices> slices) {
        super(pop, player, slices);
    }

    public static Set<PopCount> clops(PopCount pop, Player player) {
        PopCount mclop = pop.mclop()
                .min(PopCount.P99.sub(pop)
                        .add(player.pop).swap());

        Set<PopCount> clops = new HashSet<>();

        for (PopCount clop : PopCount.TABLE) {
            if(clop.le(mclop) && player.opponent().pop.le(clop))
                clops.add(clop);
        }

        return clops;
    }

    public static ClosingGroup<ScoreSlices> closed(PopCount pop, Player player, Function<PopCount, ? extends ScoreSet> generator) {

        Stream<ScoreSlices> slices = clops(pop, player).parallelStream()
                .map(generator)
                .map(ScoreSlices::of);

        return new ClosingGroup<>(pop, player, slices);
    }

    public static ClosingGroup<? extends ScoreSlices> lost(IndexProvider indexes, PopCount pop, Player player) {
        return closed(pop, player, clop-> {
            PosIndex index = indexes.build(pop, clop);
            return new LostSet(index, player);
        });
    }

    public static ClosingGroup<? extends ScoreSlices> build(LayerGroup<IndexLayer> layers, LayerGroup<ScoreMap> target) {

        ClosingGroup<TargetSlices> slices = new ClosingGroup<>(layers.pop, layers.player,
                layers.stream().map(ScoreTarget::allocate)
                .map(TargetSlices::of));
        
        GroupElevator elevator = new GroupElevator(target);

        elevator.elevate(slices);

        slices.group.values().forEach(TargetSlices::stat);

        return slices;
    }
}
