package mills.score.opening;

import mills.bits.Clops;
import mills.bits.Player;
import mills.bits.PopCount;
import mills.score.generator.ClopLayer;

import java.util.function.Consumer;

/**
 * version:     $
 * created by:  d.stueken
 * created on:  04.03.2021 19:37
 * modified by: $
 * modified on: $
 */

/**
 * Class OpeningLayer represents a layer in the opening phase.
 * The opening has 18 turns with increasing population count.
 * In addition, the count of closed mills are relevant (extends ClopLayer)
 */
abstract public class OpeningLayer implements ClopLayer {

    public static final int MAX_TURN = 18;

    final int turn;

    public OpeningLayer(int turn) {
        this.turn = turn;

        if(turn<0 || turn>MAX_TURN)
            throw new IndexOutOfBoundsException("invalid turn: " + turn);
    }

    public static Clops clops(int turn) {
        return Clops.of(placed(turn), PopCount.EMPTY);
    }

    public String toString() {
        return String.format("O%d%c%d%dc%d%d", turn/2,
                    player().key(),
                    pop().nb, pop().nw,
                    clop().nb, clop().nw);
    }

    public int turn() {
        return turn;
    }

    public Player player() {
        return (turn & 1) == 0 ? Player.White : Player.Black;
    }

    public static PopCount placed(int turn) {
        return PopCount.of((turn) / 2, (turn + 1) / 2);
    }

    public PopCount placed() {
        return placed(turn);
    }

    @Override
    public boolean canJump() {
        return false;
    }

    public boolean isComplete() {
        return true;
    }

    public boolean isEmpty() {
        return false;
    }

    Clops nextLayer() {
        Player player = player();
        PopCount pop = pop().add(player.pop);
        PopCount clop = clop();
        return Clops.of(pop, clop);
    }

    /**
     * Precalculate possible clops of next step.
     * pop+player,
     * on close:
     *   pop-player - 0,1,2 broken mills
     *
     * @return stream of possible clops
     */
    public void nextLayers(Consumer<Clops> next) {
        if(turn==MAX_TURN)
            return;

        Clops nextLayer = nextLayer();

        next.accept(nextLayer);

        Player player = player();
        Player opponent = player.opponent();

        // next pop:
        PopCount xpop = nextLayer.pop();
        PopCount clop = clop();

        // any closed?
        int nc = PopCount.mclop(player.count(xpop));
        nc -= player.count(clop);

        if(nc==0)
            return;

        if(nc>2)
            nc = 2;

        // take an opponent stone
        xpop = xpop.sub(opponent.pop);

        // # of possible opponent mills
        int oc = PopCount.mclop(opponent.count(xpop));

        for(int i=0; i<nc; ++i) {
            clop = clop.add(player.pop);

            if(opponent.count(clop) <= oc) {
                Clops closed = Clops.of(xpop, clop);
                next.accept(closed);
            }

            PopCount clop1 = clop.sub(opponent.pop);

            // no opponent mill to break
            if (clop1 == null)
                continue;

            if(opponent.count(clop1) <= oc) {
                Clops closed = Clops.of(xpop, clop1);
                next.accept(closed);
            }

            // can we break two opponent mills?
            PopCount clop2 = clop1.sub(opponent.pop);
            if (clop2 != null)
                next.accept(Clops.of(xpop, clop2));
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        return o instanceof OpeningLayer that && turn == that.turn && isEqual(that);
    }

    @Override
    public int hashCode() {
        return 31*turn + Clops.index(this);
    }
}
