package mills.score.generator;

import mills.bits.Player;
import mills.bits.PopCount;

import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 27.12.19
 * Time: 18:55
 */
public class ClosedGroup extends MovingGroup<ScoreSlice> {

    @Override
    public boolean closed() {
        return true;
    }

    public ClosedGroup(PopCount pop, Player player, List<Slices<ScoreSlice>> slices) {
        super(pop, player, slices);
    }
}
