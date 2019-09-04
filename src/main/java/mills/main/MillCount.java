package mills.main;

import mills.bits.Player;
import mills.bits.PopCount;
import mills.index.IndexProcessor;
import mills.index.IndexProvider;
import mills.index.PosIndex;
import mills.stones.Stones;

import java.util.concurrent.RecursiveAction;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 14.10.12
 * Time: 18:51
 */
public class MillCount extends RecursiveAction implements IndexProcessor {

    protected final IndexProvider indexes = IndexProvider.load();

    int count = 0;

    @Override
    public void process(int posIndex, long i201) {
        int black = Stones.stones(i201, Player.Black);
        int closed = Stones.closed(black);
        if(closed!=0)
            ++count;
    }

    public static void main(String ... args) {
        new MillCount().invoke();
    }

    public void compute() {

        for(int i=3; i<10; ++i) {
            final PopCount pop = PopCount.of(i, 0);
            final PosIndex pi = indexes.get(pop);
            final MillCount mc = new MillCount();

            pi.process(mc);

            double db = 10*Math.log10(pi.range()/(double)mc.count);

            System.out.format("%d: %6d %6d %4.1f\n", i, pi.range(), mc.count, db);
        }
    }
}
