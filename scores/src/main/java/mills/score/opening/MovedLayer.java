package mills.score.opening;

import mills.bits.Player;
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

    @Override
    void elevate(PlopSets source) {
        closed.elevate(source);
        super.elevate(closed);
    }

    /**
     * Callback from each plop set of elevating layer (closed)
     * @param source to elevate
     */
    protected void elevate(PlopSet source) {
        Player player = source.player();

        // take stone
        PopCount next = source.pop().sub(player.pop);

        // check for completely closed mills?
        int closed = source.clops().closed(player);
        PopCount clop = source.clop();

        if(closed == 0) {
            plops(next, clop);
        } else {
            // destroy other mill
            Player other = player.other();
            clop = clop.sub(other.pop);
            plops(next, clop);

            if(closed>1) {
                // may even destroy double mill
                clop = clop.sub(other.pop);
                plops(next, clop);
            }
        }
    }

    protected PlopMover elevator(PlopSet source) {

        // elevate closed positions and take a stone away
        PopCount next = source.pop().sub(source.player().pop);

        return new PlopMover(source, clop -> plops(next, clop)) {

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
                return String.format("take %s[%s] -> %s", source.pop(), source.clop(), next);
            }
        };
    }
}
