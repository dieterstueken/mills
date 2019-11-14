package mills.score.opening;

import mills.bits.PopCount;
import mills.index.IndexProvider;
import mills.stones.Stones;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 12.11.19
 * Time: 22:04
 */
public class MovedLayer extends PlopLayer {

    final ClosedLayer closed;

    MovedLayer(IndexProvider indexes, int layer) {
        super(indexes, layer);
        closed = new ClosedLayer(this);
    }

    void elevate(PlopSets source) {
        System.out.format("elevate %s\n", source);

        source.process(closed::elevate);
        closed.process(this::elevate);
    }

    protected PlopMover elevator(PlopSet source) {

        // elevate closed positions and take a stone away
        PopCount next = source.pop().sub(source.player().pop);

        return new PlopMover(source, next, this) {

            @Override
            int move(int stay, int move) {
                int closed = Stones.closed(move);

                // don't take from closed
                move ^= closed;

                // except if there ar no others
                if(move==0)
                    move = closed;

                return move;
            }


            @Override
            public String toString() {
                return String.format("close %s^%s[%s] -> %s", source, source.pop(), source.clop(), next);
            }
        };
    }
}
