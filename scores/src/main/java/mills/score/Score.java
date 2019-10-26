package mills.score;

import mills.util.AbstractRandomList;

import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 01.01.12
 * Time: 17:36
 */

/*   score (weight)
 *   0 (0)  indifferent
 *   1 (-1) immediate loss
 *   2 (2)  win
 *   3 (-3) loss
 *   4 (4)  win
 *   ...
 * 255 loss (worst)
 */
public class Score implements Comparable<Score> {

    public enum Result {LOST, DRAWN, WON}

    public final int score;

    private Score(int score) {
        this.score = score;
    }

    public String toString() {
        return String.format("%s(%d)", result(), score);
    }

    public Result result() {
        if(score==0)
            return Result.DRAWN;

        if((score&1)==1)
            return Result.LOST;

        return Result.WON;
    }

    public boolean is(Result result) {
        return result() == result;
    }

    public int score() {
        return score;
    }

    @Override
    public int compareTo(Score other) {

        if(is(Result.WON)) {
            if(other.is(Result.WON))    // shorter path is better
                return Integer.compare(other.score, score);
            else // or better than everything else.
                return +1;
        }

        if(is(Result.LOST)) {
            if(other.is(Result.LOST))    // longer path is better
                return Integer.compare(score, other.score);
            else // or worse than everything else.
                return -1;
        }

        // DRAWN
        assert result()!=Result.DRAWN;
        
        return Result.DRAWN.compareTo(other.result());
    }

    ////////////////////////////////////////////////////////////

    public static final int LOST = 1;
    public static final int WON = 2;

    public static Score of(int score) {
        return SCORES.get(score);
    }

    public static final List<Score> SCORES = AbstractRandomList.generate(256, Score::new);

    public static boolean isWon(int score) {
        return score>0 && ((score & 1) == 0);
    }

    public static boolean isLost(int score) {
        return (score & 1) != 0;
    }

    // calculate shortest lost or longest win
    public static boolean betterThan(int s1, int s2) {
        if(Score.isLost(s1))
            return Score.isLost(s2) && s1 > s2;

        if(s1!=0) // s1 won
            return !Score.isWon(s2) || s1 < s2;

        return isLost(s2);
    }
}
