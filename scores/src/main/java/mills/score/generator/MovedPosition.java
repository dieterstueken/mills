package mills.score.generator;

import mills.bits.Player;
import mills.util.AbstractRandomList;

import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 30.12.19
 * Time: 17:52
 */
public class MovedPosition extends ScoredPosition {

    final List<? extends ScoredPosition> moved;
    final List<? extends ScoredPosition> closed;

    public MovedPosition(long i201, Player player, int score,
                         List<? extends ScoredPosition> moved,
                         List<? extends ScoredPosition> closed) {
        super(i201, player, score);
        this.moved = moved;
        this.closed = closed;
    }

    @Override
    public ScoredPosition inverted() {

        List<? extends ScoredPosition> xmoved = AbstractRandomList.map(this.moved, ScoredPosition::inverted);
        List<? extends ScoredPosition> xclosed = AbstractRandomList.map(this.closed, ScoredPosition::inverted);

        return new MovedPosition(i201, player.other(), score, xmoved, xclosed) {
            @Override
            public MovedPosition inverted() {
                return MovedPosition.this;
            }
        };
    }

    protected MovedPosition position(long i201, Player player, int score,
                                     List<? extends ScoredPosition> moved,
                                     List<? extends ScoredPosition> closed) {

        return new MovedPosition(i201, player, score, moved, closed);
    }
}
