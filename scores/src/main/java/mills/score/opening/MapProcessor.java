package mills.score.opening;

import mills.bits.Clops;
import mills.bits.Player;
import mills.index.IndexProcessor;
import mills.position.Positions;
import mills.stones.Mover;
import mills.stones.Moves;
import mills.stones.Stones;

import java.util.function.LongConsumer;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 29.08.22
 * Time: 16:33
 */
public class MapProcessor implements IndexProcessor {

    final OpeningLayer layer;

    final Mover mover;

    final Mover closer;

    final LongConsumer target;

    public MapProcessor(OpeningLayer layer, LongConsumer target) {
        this.layer = layer;
        this.target = target;
        this.mover = Moves.TAKE.mover(layer.player() == Player.Black);
        this.closer = Moves.TAKE.mover(layer.player() != Player.Black);
    }

    public void process(int posIndex, long i201) {
        Player player = layer.player();
        int stay = Stones.stones(i201, player.other());
        int move = Stones.stones(i201, player);
        int mask = Stones.STONES ^ (stay | move);

        mover.move(stay, move, mask);
        mover.normalize();
        mover.analyze(this::propagate);
    }

    private void propagate(long i201) {
        Clops clops = Positions.clops(i201);

        if(!clops.clop().equals(layer.clop())) {
            // take an opponents stone
            Player player = layer.player();
            int self = Stones.stones(i201, player);
            int oppo = Stones.stones(i201, player.other());
            int closed = Stones.closed(oppo);

            // any non mill stones?
            if(closed!=oppo)
                closer.move(self, oppo, oppo^closed);
            else
                closer.move(self, oppo, closed);

            closer.normalize();
            closer.analyze(target);
        } else
            target.accept(i201);
    }
}
