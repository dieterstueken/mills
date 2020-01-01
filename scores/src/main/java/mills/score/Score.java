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

    public final int value;

    private Score(int score) {
        this.value = score;
    }

    public String toString() {
        return String.format("%s(%d)", result(), value);
    }

    public Result result() {
        if(value ==0)
            return Result.DRAWN;

        if((value &1)==1)
            return Result.LOST;

        return Result.WON;
    }

    public boolean is(Result result) {
        return result() == result;
    }

    public int value() {
        return value;
    }

    public Score next() {
        return Score.of(value+1);
    }

    @Override
    public int compareTo(Score other) {

        if(is(Result.WON)) {
            if(other.is(Result.WON))    // shorter path is better
                return Integer.compare(other.value, value);
            else // or better than everything else.
                return +1;
        }

        if(is(Result.LOST)) {
            if(other.is(Result.LOST))    // longer path is better
                return Integer.compare(value, other.value);
            else // or worse than everything else.
                return -1;
        }

        // DRAWN
        assert result()!=Result.DRAWN;
        
        return Result.DRAWN.compareTo(other.result());
    }

    ////////////////////////////////////////////////////////////

    public static final List<Score> SCORES = AbstractRandomList.generate(256, Score::new);

    public static final Score DRAWN = Score.of(0);
    public static final Score LOST = Score.of(1);
    public static final Score WON = Score.of(2);

    public static Score of(int score) {
        return SCORES.get(score);
    }
    
    public static boolean isWon(int score) {
        return score>0 && ((score & 1) == 0);
    }

    public static boolean isLost(int score) {
        return score>0 && (score & 1) != 0;
    }

    // calculate shortest win or longest lost
    public static boolean betterThan(int s1, int s2) {

        assert s2>=0;

        // shorter win path
        if(Score.isWon(s1))
            return Score.isWon(s2) && s1 < s2;

        // longer loss path
        if(Score.isLost(s1))
            return Score.isLost(s2) && s1 > s2;

        assert s1==0;

        return isLost(s2);
    }
}
