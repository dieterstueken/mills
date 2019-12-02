package mills.score.generator;

import mills.bits.Player;
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
public class MapSlice extends ScoreSlice {

    public static MapSlice of(ScoreMap scores, int index) {
        return new MapSlice(scores, index);
    }

    //////////////////////////////////////

    final ScoreMap scores;

    final QueueActor<MapSlice> work = new QueueActor<>(this);

    // current score to analyze
    int scoring = 0;

    // to calculate pending moves
    final Mover mover;

    public ScoreMap scores() {
        return scores;
    }

    public void close() {
        super.close();
        work.finish();
    }

    protected MapSlice(ScoreMap scores, int index) {
        super(index);

        this.scores = scores;

        mover = Moves.moves(scores().jumps()).mover(player()==Player.Black);
    }

    void setScore(short offset, int score) {
        mark(offset, score);

        int posIndex = posIndex(offset);
        scores.setScore(posIndex, score);
    }

    private boolean resolved(int current, int newScore) {

        // is not a longer win path
        if(Score.isWon(current))
            return Score.isWon(newScore) && current <= newScore;

        // is not a shorter loss path
        if(Score.isLost(current))
            return Score.isLost(newScore) && current >= newScore;

        // either still 0 or counting: to be resolved
        return false;
    }

    /**
     * Propagate incoming score.
     * All smaller scores have already been processed.
     * @param index of target position
     * @param i201 target position
     * @param newScore incoming new score (incremented)
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

        if(current>0) {
            // !resolved
            setScore(offset, newScore);
            return;
        }

        if(current<0) {
            // count down
            ++current;
            if(current==0)
                setScore(offset, newScore);
            else
                setScore(offset, current);
            return;
        }

        // current == 0

        if(scoring==0) {
            // during elevation setup L directly
            setScore(offset, newScore);
            return;
        }

        // count remaining

        int unresolved = unresolved(i201);

        // must be at least 1 since we just propagate a position.
        assert unresolved > 1;

        if(unresolved==1)
            setScore(offset, newScore);
        else
            setScore(offset, 1-unresolved);
    }


    int unresolved(long i201) {
        Player player = player();
        int stay = Stones.stones(i201, player.other());
        int move = Stones.stones(i201, player);
        int closed = Stones.closed(move);

        mover.move(stay, move, move^closed);
        return mover.normalize().size();
    }

}