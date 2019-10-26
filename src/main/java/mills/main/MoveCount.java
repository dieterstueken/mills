package mills.main;

import mills.bits.Player;
import mills.bits.PopCount;
import mills.index.IndexProcessor;
import mills.index.IndexProvider;
import mills.index.PosIndex;
import mills.stones.Mover;
import mills.stones.Moves;
import mills.stones.Stones;

import java.util.concurrent.RecursiveAction;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 18.11.12
 * Time: 11:52
 */
public class MoveCount extends RecursiveAction {

    public final IndexProvider indexes = IndexProvider.load();

    public static void main(String ... args) {
        new MoveCount().invoke();
    }

    @Override
    public void compute() {

        for(int i=3; i<10; i++) {

            m0 = 0;
            m1 = 0;

            PosIndex pi = indexes.build(PopCount.of(3, i));

            pi.process(processor(pi));

            System.out.format("%d %9d %9d %3d\n", i, pi.range(), m0, m1);
        }
    }

    int m0 = 0;
    int m1 = 0;

    final

    IndexProcessor processor(PosIndex pi) {

        Mover mover = Moves.JUMP.mover();

        return (posIndex, i201) -> {

            int white = Stones.stones(i201, Player.White);
            int closed = Stones.closed(white);
            if (closed == 0)
                return;

            int black = Stones.stones(i201, Player.Black);

            mover.move(black, white, closed).unique();

            int n = mover.size();

            if (n > m1)
                m1 = n;

            m0++;
        };
    }
}
