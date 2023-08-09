package mills.score.opening;

import mills.bits.Clops;
import mills.bits.Player;
import mills.bits.PopCount;
import mills.score.generator.ClopLayer;

import java.util.stream.Stream;

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
public class OpeningLayer implements ClopLayer {

    public static final int MAX_TURN = 18;

    final int turn;

    final Clops clops;

    public OpeningLayer(int turn, Clops clops) {
        this.turn = turn;
        this.clops = clops;

        if(turn<0 || turn>MAX_TURN)
            throw new IndexOutOfBoundsException("invalid turn: " + turn);

        /*
         * The number of closed mills must not exceed the number of vanished stones.
         */

        PopCount vanished = placed(turn).sub(clops.pop());
        if(vanished==null)
            throw new IllegalArgumentException("PopCount exceeds expected: " + this.clops);

        // does not hold: double close takes one, mills may be destroyed again
        //if(vanished.sub(clops.clop().swap())== null)
        //    throw new IllegalArgumentException("ClopCount exceeds expected: " + this.clops);
    }

    public String toString() {
        return String.format("O%d%c%d%dc%d%d", turn,
                    player().key(),
                    pop().nb, pop().nw,
                    clop().nb, clop().nw);
    }

    public static OpeningLayer of(int turn, Clops clops) {
        return new OpeningLayer(turn, clops);
    }

    public Clops clops() {
        return clops;
    }

    @Override
    public PopCount clop() {
        return clops.clop();
    }

    @Override
    public PopCount pop() {
        return clops.pop();
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

    public Clops nextClops() {
        return Clops.of(pop().add(player().pop), clop());
    }

    /**
     * Precalculate possible clops of next step.
     * pop+player,
     * on close:
     *   pop-opponent - 1,2 closed/stroke mills
     *
     * @return stream of possible clops
     */
    public Stream<Clops> clopsStream() {
        if(turn>=MAX_TURN)
            return Stream.of();

        Player player = player();

        // player adds a stone without closing a mill.
        Clops next = nextClops();
        Stream<Clops> layers = Stream.of(next);

        // # of additional mills that may be closed
        int mc = player.mclop(next.pop()) - player.count(next.clop());
        if(mc>0) {
            // some mill(s) can be closed
            PopCount clop1 = clop().add(player.pop);
            Stream<PopCount> strokes = strokes(clop1);

            if(mc>1) {
                // closing of two mills possible.
                PopCount clop2 = clop1.add(player.pop);
                strokes = Stream.concat(strokes, strokes(clop2));
            }

            // stroke an opponents stone
            PopCount stroke = next.pop().sub(player().opponent().pop);

            layers = Stream.concat(layers,
                    strokes.map(clop -> Clops.of(stroke, clop)));
        }

        return layers;
    }

    private Stream<PopCount> strokes(PopCount clop) {
        Player opponent = player().opponent();

        // # of opponent mills that can be destroyed.
        int md = opponent.count(clop);

        if(md==0)
            return Stream.of(clop);

        // stroke a single opponent mill
        PopCount clop1 = clop.sub(opponent.pop);

        if(md==1)
            return Stream.of(clop, clop1);

        // may stroke two connected mills
        PopCount clop2 = clop1.sub(opponent.pop);

        return Stream.of(clop, clop1, clop2);
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
