package mills.score.generator;

import mills.bits.Player;
import mills.score.Score;
import mills.stones.Mover;
import mills.stones.Moves;
import mills.stones.Stones;

/**
 * Created by IntelliJ IDEA.
 * User:
 * stueken
 * Date: 17.11.12
 * Time: 19:43
 */
public class MapSlice extends ScoreSlice {

    final ScoreMap scores;

    // to calculate pending moves
    final Mover mover;

    // max score occurred
    protected int max = 0;

    // any positions set
    protected final long[] dirty = new long[256];

    public ScoreMap scores() {
        return scores();
    }

    public int max() {
        return max;
    }

    public long marked(Score score) {
        return this.dirty[score.value];
    }


    protected MapSlice(ScoreMap scores, int index) {
        super(index);
        this.scores = scores;
        this.mover = Moves.moves(scores.canJump()).mover(true);;
    }

    int unresolved(long i201) {
        Player player = player();
        int stay = Stones.stones(i201, player.opponent());
        int move = Stones.stones(i201, player);
        //int closed = Stones.closed(move);
        // include closed, too

        mover.move(stay, move, move);
        return mover.normalize().size();
    }

    // set max and pending thresholds.
    protected void mark(short offset, int score) {
        if (score > max)
            max = score;
        dirty[score] |= mask(offset);
    }

    int init() {
        int count = 0;

        for(short offset=0; offset<size(); ++offset) {
            int score = getScore(offset);
            mark(offset, score);
            if(score!=0)
                ++count;
        }

        return count;
    }
}
