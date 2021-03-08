package mills.score.opening;

import mills.bits.Clops;
import mills.bits.Player;
import mills.bits.PopCount;
import mills.score.generator.ClopLayer;

import java.util.Comparator;

/**
 * version:     $
 * created by:  d.stueken
 * created on:  04.03.2021 19:37
 * modified by: $
 * modified on: $
 */
public class OpeningLayer implements ClopLayer {

    public static final int MAX_TURN = 18;

    public static final Comparator<OpeningLayer> COMPARATOR = Comparator
            .comparingInt(OpeningLayer::turn)
            .thenComparingInt(OpeningLayer::getIndex);
    
    final Clops clops;
    
    final int turn;

    public static final OpeningLayer START = new OpeningLayer(0, Clops.EMPTY);

    public OpeningLayer(int turn, Clops clops) {
        this.turn = turn;
        this.clops = Clops.of(clops);

        if(turn<0 || turn>MAX_TURN)
            throw new IndexOutOfBoundsException("invalid turn: " + turn);

        assert placed(turn).sub(clops.pop())!=null;
    }

    public OpeningLayer(int turn) {
        this(turn,Clops.of(placed(turn), null));
    }

    public String toString() {
        //if(pop().equals(placed()))
        //    return String.format("O%d%c%d%d", turn/2,
        //            player().key(),
        //            pop().nb, pop().nw);
        //else

        return String.format("O%d%c%d%dc%d%d", turn/2,
                    player().key(),
                    pop().nb, pop().nw,
                    clop().nb, clop().nw);
    }

    public OpeningLayer play(int close) {
        if(turn>=MAX_TURN)
            return null;

        PopCount pop = pop().add(player().pop);
        if(close>0)
            pop = pop.sub(player().other().pop);
        PopCount clop = clop();

        for(; close>0; --close)
            clop = clop.add(player().pop);

        return new OpeningLayer(turn+1, Clops.of(pop, clop));
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

    public PopCount pop() {
        return clops.pop();
    }

    @Override
    public PopCount clop() {
        return clops.clop();
    }

    @Override
    public boolean canJump() {
        return false;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OpeningLayer that = (OpeningLayer) o;
        return turn == that.turn && clops.equals(that.clops);
    }

    @Override
    public int hashCode() {
        return 31*turn + clops.hashCode();
    }
}
