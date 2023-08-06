package mills.score.generator;

import mills.bits.Player;
import mills.bits.PopCount;
import mills.index.PosIndex;

import java.util.Map;
import java.util.function.Function;

/**
 * version:     $
 * created by:  d.stueken
 * created on:  07.04.2023 17:48
 * modified by: $
 * modified on: $
 */
public class TargetGroup extends MovingGroup<TargetSlices> {

    public TargetGroup(PopCount pop, Player player, Map<PopCount, ? extends TargetSlices> group) {
        super(pop, player, group);
    }

    public static TargetGroup create(PosIndex groups, Player player) {
        Function<PopCount, ScoreTarget> newTarget = clop -> ScoreTarget.allocate(groups.getIndex(clop), player);
        return create(groups.pop(), player, newTarget.andThen(TargetGroup::newSlices));
    }

    public static TargetGroup create(PopCount pop, Player player, Function<PopCount, ? extends TargetSlices> slices) {
        Map<PopCount, ? extends TargetSlices> group = LayerGroup.group(clops(pop), slices);
        return new TargetGroup(pop, player, group);
    }

    static TargetSlices newSlices(ScoreTarget scores) {
        TargetSlices slices = TargetSlices.of(scores);
        slices.slices().parallelStream().forEach(TargetSlice::init);
        return slices;
    }
}
