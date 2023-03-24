package mills.score.generator;

import mills.bits.Player;
import mills.index.IndexProcessor;
import mills.score.Score;
import mills.stones.Mover;
import mills.stones.Moves;
import mills.stones.Stones;
import mills.util.QueueActor;

/**
 * Created by IntelliJ IDEA.
 * User:
 * stueken
 * Date: 17.11.12
 * Time: 19:43
 */

/**
 * Class MapSlice is a writeable ScoreSlice.
 */
public class MapSlice extends ScoreSlice {

    final QueueActor<MapSlice> work = QueueActor.of(this);

    // to calculate pending moves
    final Mover mover;

    int pending = 0;

    public void close() {
        work.close();

        for(int offset=0; offset<size(); ++offset) {
            int posIndex = base+offset;
            int value = scores.getScore(posIndex);
            if(value>max)
                scores.setScore(posIndex,0);
        }
    }

    MapSlice(ScoreMap scores, int index, Mover mover) {
        super(scores, index);

        this.mover = mover;
    }

    static MapSlice of(ScoreMap scores, int index) {

        Mover mover = Moves.moves(scores.canJump()).mover(true);

        return new MapSlice(scores, index, mover);
    }

    void setScore(short offset, int score) {
        mark(offset, score);

        int posIndex = posIndex(offset);
        scores.setScore(posIndex, score);
    }

    // set max and pending thresholds.
    public void mark(short offset, int score) {
        if(score<0) {
            if(-score>pending)
                pending = -score;
            dirty[0] |= mask(offset);
        } else {
            if (score > max)
                max = score;

            dirty[score] |= mask(offset);
        }

        if(max+pending>=255)
            throw new IllegalStateException("score overflow");
    }

    /**
     * Return if new score is not better than current score
     *
     * current score: 0 L W count
     * newScore W:    W W < W
     * newScore L:    C > W --count
     *
     * @param current score
     * @param newScore score
     * @return true if new score can be ignored.
     */
    private boolean resolved(int current, int newScore) {

        assert newScore > 0;

        // is not a longer win path
        if (Score.isWon(newScore)) {
            if (Score.isWon(current)) {
                // ignore longer win paths
                return newScore >= current;
            } else
                // everything else propagates won
                return false;
        } else if (Score.isLost(newScore)) {
            if (Score.isLost(current)) {
                // ignore longer win paths
                return newScore <= current;
            } else
                return Score.isWon(current);
        }

        throw new IllegalStateException("invalid score");
    }

    /**
     * Propagate incoming score.
     * All smaller scores have already been processed.
     * @param index of target position
     * @param i201 target position
     * @param newScore incoming new score (incremented)
     * @return new current score
     */
    public void propagate(int index, long i201, int newScore) {
        short offset = offset(index);
        int score = getScore(offset);

        if(!resolved(score, newScore)) {
            work.submit(slice -> slice.setupScore(offset, i201, newScore));
        }
    }

    private void setupScore(short offset, long i201, int newScore) {

        int current = getScore(offset);
        if(resolved(current, newScore))
            return;

        if(Score.isWon(newScore)) {
            // no further checks necessary
            setScore(offset, newScore);
        } else // propagate loss
        if(current<0) {
            // count down
            ++current;
            if(current==0)
                setScore(offset, newScore);
            else
                setScore(offset, current);
        } else {

            // count remaining
            int unresolved = unresolved(i201);

            // must be at least 1 since we just propagate a position.
            assert unresolved > 1;

            if (unresolved == 1)
                setScore(offset, newScore);
            else
                setScore(offset, 1 - unresolved);
        }
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

    class Initializer implements IndexProcessor {

        int count = 0;

        @Override
        public void process(int posIndex, long i201) {
            int unresolved = unresolved(i201);
            if(unresolved==0) {
                short offset = offset(posIndex);
                setScore(offset, Score.LOST.value);
                ++count;
            }
        }

        int initialize() {
            MapSlice.this.process(this);
            return count;
        }
    }

    @Override
    int init() {
        return new Initializer().initialize();
    }
}
