package mills.score.opening;

import mills.bits.PopCount;
import mills.stones.Stones;

import java.util.function.Function;

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

        Function<PopCount, PlopSet> target = clop -> (clop.equals(source.clop()) ? moved : this).plops(next, clop);

        return new PlopMover(source, target) {

            @Override
            int move(int stay, int move) {
                return Stones.free(stay|move);
            }

            @Override
            public String toString() {
                return String.format("set  %s[%s] -> %s", source.pop(), source.clop(), next);
            }

            @Override
            public void close() {
                super.close();
                // remove pass thru target again
                targets.remove(source.clop());
            }
        };
    }

}
