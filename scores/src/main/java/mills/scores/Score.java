package mills.scores;

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
public class Score {

    final int score;

    public Score(int score) {
        this.score = score;
    }

    public static final int LOST = 1;
    public static final int WON = 2;

    public enum Result {drawn, lost, won}

    // flatten a score to its final result
    public static Result result(int score) {
        if(score==0)
            return Result.drawn;

        if((score & 1) == 1)
            return Result.lost;
        else
            return Result.won;
    }

    public static boolean isWon(int score) {
        return score>0 && ((score & 1) == 0);
    }

    public static boolean isLost(int score) {
        return (score & 1) != 0;
    }

    // turn lost positions into a negative value to get an ordering
    public static int weight(int score) {
        //return isLost(score) ? -score : score;
        return score==0 ? 0 : (Integer.MAX_VALUE-score) * (2*(score&1)-1);
    }

    public static int compare(int s1, int s2) {
        return Integer.compare(weight(s1), weight(s2));
    }

    // calculate shortest lost or longest win
    public static boolean betterThan(int s1, int s2) {
        if(Score.isLost(s1))
            return Score.isLost(s2) && s1 > s2;

        if(s1!=0) // s1 won
            return !Score.isWon(s2) || s1 < s2;

        return isLost(s2);
    }

    public static void main(String ... args) {

        int s[] = {1, 3, 0, 4, 2};

        System.out.print("  ");
        for(int i=0; i<s.length; i++)
            System.out.format("%3d", s[i]);
        System.out.println();

        for(int i=0; i<s.length; i++) {
            System.out.format("%3d", s[i]);

            for(int j=0; j<s.length; j++)
                //System.out.format("%3d", compare(s[i], s[j]));
                System.out.print(betterThan(s[i], s[j]) ? " + " : " - ");

            System.out.println();
        }
    }
}
