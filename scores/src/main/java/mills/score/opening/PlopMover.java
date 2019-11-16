package mills.score.opening;

import mills.bits.Player;
import mills.bits.PopCount;
import mills.index.IndexProcessor;
import mills.position.Position;
import mills.position.Positions;
import mills.stones.Mover;
import mills.stones.Moves;
import mills.stones.Stones;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.function.LongConsumer;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 12.11.19
 * Time: 18:36
 */
abstract public class PlopMover implements IndexProcessor, AutoCloseable {

    final Function<PopCount, PlopSet> target;
    final Player player;
    final Mover mover;

    private final LongConsumer propagator = this::propagate;

    final Map<PopCount, PlopSet> targets = new HashMap<>();

    PlopMover(PlopSet source, Function<PopCount, PlopSet> target) {
        this.target = target;
        this.player = source.player();
        this.mover = Moves.TAKE.mover(player==Player.Black);
    }

    abstract int move(int stay, int move);

    public void process(int posIndex, long i201) {

        int stay = Stones.stones(i201, player.other());
        int move = Stones.stones(i201, player);
        int mask = move(stay, move);

        try {
            mover.move(stay, move, mask);
            mover.normalize();
            mover.analyze(propagator);
        } catch (Throwable error) {
            mover.move(stay, move, mask);
            mover.normalize();
            mover.analyze(this::debug);
            throw error;
        }
    }

    private void debug(long i201) {
        var p = Position.of(i201);
        System.out.println(p);
    }


    protected void propagate(long i201) {
        PopCount clop = Positions.clop(i201);
        //assert Positions.pop(i201).equals(next);
        PlopSet target = target(clop);
        target.setPos(i201);
    }

    protected PlopSet target(PopCount clop) {
        return targets.computeIfAbsent(clop, target);
    }

    @Override
    public void close() {
        for (PlopSet plops : targets.values()) {
            System.out.format("     -> %s[%s] %,d/%,d\n",
                    plops.pop(), plops.clop(),
                    plops.set.cardinality(), plops.index.range());
        }
        System.out.println();
    }
}
