package mills.score.opening;

import mills.bits.Player;
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

        // none closed -> moved
        moved.plops(next, clop);
        
        int closeable = source.clops().closeables(player);

        if (closeable > 0) {
            // might close a mill
            clop = clop.add(player.pop);
            this.plops(next, clop);

            if (closeable > 1) {
                // might close two mills
                clop = clop.add(player.pop);
                this.plops(next, clop);
            }
        }
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
