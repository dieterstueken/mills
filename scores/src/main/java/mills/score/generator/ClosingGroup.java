package mills.score.generator;

import mills.bits.Player;
import mills.bits.PopCount;
import mills.index.PosIndex;

import java.util.Map;
import java.util.function.Function;
import java.util.stream.Stream;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 27.12.19
 * Time: 18:55
 */
public class ClosingGroup extends MovingGroup<ScoreSlices> {

    @Override
    public boolean closing() {
        return true;
    }

    public ClosingGroup(PopCount pop, Player player, Map<PopCount, ? extends ScoreSlices> group) {
        super(pop, player, group);
    }

    /**
     * Stream of clops for a given player after closing a mill by opponent.
     * @param pop count of layer.
     * @param opponent who closed the mill.
     * @return stream of clops with at least one closed mill of opponent.
     */
    public static Stream<PopCount> clops(PopCount pop, Player opponent) {
        return MovingGroup.clops(pop).filter(opponent.pop::ge);
    }

    public static ClosingGroup closed(PopCount pop, Player player, Function<PopCount, ? extends ScoreSlices> slice) {
        Map<PopCount, ? extends ScoreSlices> slices = LayerGroup.group(clops(pop, player.opponent()), slice);
        return new ClosingGroup(pop, player, slices);
    }
}
