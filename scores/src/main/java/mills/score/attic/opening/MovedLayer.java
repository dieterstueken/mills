package mills.score.attic.opening;

import mills.bits.Player;
import mills.bits.PopCount;
import mills.index.IndexProcessor;
import mills.index.IndexProvider;
import mills.stones.Moves;
import mills.stones.Stones;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 12.11.19
 * Time: 22:04
 */
public class MovedLayer extends PlopLayer {

    final ClosedLayer closed;

    public MovedLayer(IndexProvider indexes, Plop plop) {
        super(indexes, plop);
        closed = new ClosedLayer(this);
    }

    public MovedLayer next() {
        Plop next = plop.next();
        return next == null ? null : new MovedLayer(indexes, next).elevate(this);
    }

    MovedLayer elevate(MovedLayer src) {

        // prepare target plop sets of both layers
        src.forEach(this::elevate);
        src.forEach(closed::elevate);

        closed.forEach(this::elevateClosed);

        closed.forEach(tgt -> closed.trace(src, tgt));
        this.forEach(tgt -> trace(src, tgt));

        closed.show();
        show();

        return this;
    }

    /**
     * Regular propagation.
     * @param src to elevate.
     */
    protected void elevate(PlopSet src) {
        PopCount next = src.pop().add(src.player().pop);
        PopCount clop = src.clop();
        plops(next, clop);
    }

    /**
     * Callback from each plop set of elevating layer (closed)
     * @param src to elevate.
     */
    protected void elevateClosed(PlopSet src) {
        Player player = src.player();

        // take stone
        PopCount next = src.pop().sub(player.pop);

        // check for completely closed mills?
        int closed = src.clops().closed(player);
        PopCount clop = src.clop();

        if(closed == 0) {
            plops(next, clop);
        } else {
            // destroy a mill
            clop = clop.sub(player.pop);
            plops(next, clop);

            if(closed>1) {
                // may even destroy double mill
                clop = clop.sub(player.pop);
                plops(next, clop);
            }
        }
    }

    /**
     * Trace moved target positions back to src or this.closed layer.
     *
     * Ordinary moves from src layer must not have closed a mill.
     *
     * Moves coming from from this.closed layer had closed a mill (or two)
     * and will loose an opponent stone from either non closed or all closed positions.
     *
     * @param src of ordinary moves.
     */
    
    @Override
    protected void trace(MovedLayer src, PlopSet tgt) {

        if(tgt.clop().equals(PopCount.EMPTY)) {
            // simply setup all
            tgt.set.set(0, tgt.index.range());
            return;
        }

        IndexProcessor processor = new IndexProcessor() {

            // Reset a non closed stone put before.
            boolean traceMove(long i201) {
                Player player = src.player();
                int stay = Stones.stones(i201, player.opponent());
                int move = Stones.stones(i201, player);

                // must not have closed a mill
                int mask = move ^ Stones.closed(move);

                int at = Moves.TAKE.move(stay, move, mask, src);
                return at<0;
            }

            // replace lost opponents stone
            boolean traceClose (long i201) {
                Player player = closed.player();

                int stay = Stones.stones(i201, player.opponent());
                if(Stones.closed(stay)==0)
                    return false;
                
                int move = Stones.stones(i201, player);

                // reset any void position
                int mask = Stones.STONES ^ (stay|move);

                int at = Moves.TAKE.move(stay, move, mask, closed);
                return at<0;
            }

            @Override
            public void process(int posIndex, long i201) {
                if (traceClose(i201) || traceMove(i201))
                    tgt.setPos(i201);
            }
        };

        tgt.processParallel(processor);
    }
}
