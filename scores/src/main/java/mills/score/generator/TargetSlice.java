package mills.score.generator;

import mills.score.Score;
import mills.util.QueueActor;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 24.03.23
 * Time: 14:08
 */
public class TargetSlice extends MapSlice<ScoreTarget> {

    final QueueActor<TargetSlice> work = QueueActor.of(this);

    int pending = 0;

    int pending() {
        return pending;
    }

    TargetSlice(ScoreTarget scores, int index) {
        super(scores, index);
    }

    void init() {
        process(this::setup);
    }

    private void setup(int posIndex, long i201) {
        if(!any(i201)) {
            short offset = offset(posIndex);
            setScore(offset, Score.LOST.value);
        }
    }

    /**
     * Return pending scores with negative values.
     *
     * @param posIndex of score
     * @return signed score value.
     */
    public int getScore(int posIndex) {

        int score = super.getScore(posIndex);
        if(score+pending > 255) {
            score -= 256;
        }

        return score;
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
            super.mark(offset, score);
        }

        if(max+pending>=255)
            throw new IllegalStateException(String.format("score overflow: %d %d", max, pending));
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
            work.submit(slice -> setupScore(offset, i201, newScore));
        }
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
            if(unresolved == 0) {
                unresolved(i201);
                throw new IllegalStateException("update resolved position");
            }

            if (unresolved == 1)
                setScore(offset, newScore);
            else
                setScore(offset, 1 - unresolved);
        }
    }

    /**
     * Finalize this slice by resetting all pending entries to drawn.
     */
    public void close() {

        // wait for all work to finish.
        work.close();

        for(int offset=0; offset<size(); ++offset) {
            int posIndex = base+offset;
            int value = getScore(posIndex);
            if(value<0) {
                scores.setScore(posIndex, Score.DRAWN.value);
            }
        }
    }
}
