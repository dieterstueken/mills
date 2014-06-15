package mills.scores;

import mills.util.AbstractRandomArray;

import java.util.concurrent.RecursiveAction;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 25.05.13
 * Time: 15:41
 */
public class ScoreStat extends AbstractRandomArray<AtomicInteger> {

    final AtomicInteger stat[];

    public ScoreStat(int size) {
        super(size);

        stat = new AtomicInteger[size];
        for(int i=0; i<size; ++i)
            stat[i] = new AtomicInteger();
    }

    @Override
    public AtomicInteger get(int index) {
        return stat[index];
    }

    public void print() {

        for(int score=0; score<stat.length; score++) {
            int n = stat[score].get();
            if(n>0) {
                String w = Score.isLost(score)? "-" : "+";
                System.out.format("%d (%s) %d\n", score, w, n);
            }
        }
    }

    RecursiveAction closer(final ScoreSlice slice) {
        return new RecursiveAction() {
            @Override
            protected void compute() {
                slice.close(ScoreStat.this);
            }
        };
    }
}
