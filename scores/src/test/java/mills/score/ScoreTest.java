package mills.score;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 26.10.19
 * Time: 17:58
 */
public class ScoreTest {

    @Test
    public void betterThan() {

        int[] s = {1, 3, 0, 4, 2};

        System.out.print("  ");
        for(int i=0; i<s.length; i++)
            System.out.format("%3d", s[i]);
        System.out.println();

        for(int i=0; i<s.length; i++) {
            System.out.format("%3d", s[i]);

            for(int j=0; j<s.length; j++)
                //System.out.format("%3d", compare(s[i], s[j]));
                System.out.print(Score.betterThan(s[i], s[j]) ? " + " : " - ");

            System.out.println();
        }
    }

    @Test
    public void weight() {

        List<Score> test = List.of(Score.of(0), Score.of(1), Score.of(2), Score.of(254), Score.of(255));

        List<Score> scores = new ArrayList<>(Score.SCORES);
        Collections.sort(scores);

        for (Score score : scores) {
            System.out.format("%3d: %s\n", score.score, score.result());

            compareTest(score, score);
            for (Score ts : test) {
                compareTest(score, ts);
                compareTest(ts, score);
            }

        }
    }

    private void compareTest(Score s1, Score s2) {
        int cmp = s1.compareTo(s2);
        boolean betterThan = Score.betterThan(s1.score, s2.score);

        assertEquals(betterThan, cmp>0);

        assertEquals(s1.compareTo(s2), -1 * s2.compareTo(s1));
    }
}