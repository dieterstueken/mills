package mills.score.opening;

import mills.bits.PopCount;
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

    @Override
    PlopSet plops(PopCount pop, PopCount clop) {
        return super.plops(pop, clop);
    }

    protected PlopMover elevator(PlopSet source) {

        // elevate moved or closed positions
        PopCount next = source.pop().add(source.player().pop);

        // verify if any mill was closed
        // non closing positions are passed to move
        PopCount sclop = source.clop();

        return new PlopMover(source, next, this) {

            @Override
            int move(int stay, int move) {
                return Stones.free(stay|move);
            }
            
            transient PlopSet moved = null;

            @Override
            protected PlopSet target(PopCount clop) {
                if(!clop.equals(sclop))
                    return super.target(clop);

                // not closed, just moved
                if(moved==null)
                    moved = ClosedLayer.this.moved.plops(next, clop);

                return moved;
            }

            @Override
            public String toString() {
                return String.format("move  %s^%s[%s] -> %s", source, source.pop(), source.clop(), next);
            }
        };
    }

}
