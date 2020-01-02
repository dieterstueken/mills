package mills.score.generator;

import mills.bits.Player;
import mills.bits.PopCount;
import mills.index.IndexProvider;
import mills.index.PosIndex;

import java.util.function.Function;
import java.util.stream.Stream;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 27.12.19
 * Time: 18:55
 */
public class ClosingGroup extends MovingGroup<ScoreSlice> {

    @Override
    public boolean closing() {
        return true;
    }

    public ClosingGroup(PopCount pop, Player player, Stream<ScoreSet> slices) {
        super(pop, player, slices.map(scores -> Slices.generate(scores, scores::openSlice)));
    }

    public static ClosingGroup closed(PopCount pop, Player player, Function<PopCount, ? extends ScoreSet> generator) {

        // max reachable closed count plus a closed opponent mill
        PopCount mclop = pop.mclop()
                .min(PopCount.P99.sub(pop)
                    .add(player.pop).swap());

        Stream<ScoreSet> slices = PopCount.TABLE.parallelStream()
                .filter(clop -> clop.le(mclop))
                .filter(clop -> player.other().pop.le(clop)) // plus a closed opponent mill
                .map(generator);

        return new ClosingGroup(pop, player, slices);
    }

    public static ClosingGroup lost(IndexProvider indexes, Player player) {
        return closed(PopCount.get(3,3), player, clops-> {
            PosIndex index = indexes.build(clops);
            return new LostSet(index, player);
        });
    }
}
