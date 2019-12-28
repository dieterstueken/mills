package mills.score.generator;

import mills.bits.Player;
import mills.bits.PopCount;

import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 27.12.19
 * Time: 21:18
 */
public class MovedGroup extends MovingGroup<MapSlice> {
    
    final ClosedGroup closed;
    
    public MovedGroup(PopCount pop, Player player, ClosedGroup closed, List<Slices<MapSlice>> slices) {
        super(pop, player, slices);
        this.closed = closed;
    }
    
    ScoredPosition position(MovingGroup<? extends ScoreSlice> target, long i201) {
        return target.position(i201);
    }
}
