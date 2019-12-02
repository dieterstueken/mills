package mills.score.opening;

import mills.bits.Player;
import mills.bits.PopCount;
import mills.index.IndexProcessor;
import mills.stones.Moves;
import mills.stones.Stones;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 12.11.19
 * Time: 22:05
 */
public class ClosedLayer extends PlopLayer {

    final MovedLayer moved;

    ClosedLayer(MovedLayer moved) {
        super(moved);
        this.moved = moved;
    }

    /**
     * Callback from each plop set of elevating layer
     *
     * @param source to elevate
     */
    protected void elevate(PlopSet source) {
        Player player = source.player();

        // put stone
        PopCount next = source.pop().add(player.pop);
        PopCount clop = source.clop();

        // same clop (no closings) passed to ordinary moved
        moved.plops(next, clop);

        // # of mills that may be closed
        int closeable = source.clops().closeables(player);

        if (closeable > 0) {
            // opponent might have close a mill
            clop = clop.add(player.pop);
            this.plops(next, clop);

            if (closeable > 1) {
                // might close two mills
                clop = clop.add(player.pop);
                this.plops(next, clop);
            }
        }
    }

    /**
     * Trace closed target positions back to src:
     * Break a closed mill and lookup position @src.
     * @param src source of back trace.
     */
    @Override
    protected void trace(MovedLayer src, PlopSet tgt) {

        // target plop set of closed positions to analyze

        IndexProcessor processor = (posIndex, i201) -> {
            Player player = src.player();
            int stay = Stones.stones(i201, player.other());
            int move = Stones.stones(i201, player);

            // break a closed mill
            int mask = Stones.closed(move);

            int at = Moves.TAKE.move(stay, move, mask, src);
            if(at<0)
                tgt.setPos(i201);
        };

        tgt.processParallel(processor);
    }

    @Override
    public boolean process(int stay, int move, int mask) {
        int moved = move^mask;

        // broke a mill?
        int closing = Stones.closed(moved);

        // breaking closed mills is permitted only if all positions are closed too.
        if((closing&mask)==0 || closing==moved)
            return super.process(stay, move, mask);
        else
            return false;
    }
}
