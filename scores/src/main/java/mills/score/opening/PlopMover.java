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

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 12.11.19
 * Time: 18:36
 */
abstract public class PlopMover implements IndexProcessor, AutoCloseable {

    final Player player;
    final PopCount next;

    final Mover mover;

    final PlopLayer target;
    final Map<PopCount, PlopSet> targets = new HashMap<>();

    PlopMover(PlopSet source, PopCount next, PlopLayer target) {
        this.target = target;
        this.player = source.player();
        this.next = next;

        mover = Moves.TAKE.mover(player==Player.Black);

        //targets.put(source.clop(), init.plops(next, source.clop()));
    }

    abstract int move(int stay, int move);

    public void process(int posIndex, long i201) {

        int stay = Stones.stones(i201, player.other());
        int move = Stones.stones(i201, player);
        int mask = move(stay, move);

        try {
            mover.move(stay, move, mask);
            mover.normalize();
            mover.analyze(this::propagate);
        } catch (Throwable error) {
            mover.move(stay, move, mask);
            mover.normalize();
            mover.analyze(this::debug);
        }
    }

    private void debug(long i201) {
        var p = Position.of(i201);
        p.toString();
    }

    private void propagate(long i201) {
        PopCount clop = Positions.clop(i201);
        assert Positions.pop(i201).equals(next);
        PlopSet target = target(clop);
        target.setPos(i201);
    }

    protected PlopSet target(PopCount clop) {
        return targets.computeIfAbsent(clop, this::findTarget);
    }

    private PlopSet findTarget(PopCount clop) {
        return target.plops(next, clop);
    }

    @Override
    public void close() {
        System.out.format("%s : ", toString());
        for (PlopSet plops : targets.values()) {
            System.out.format("%s[%s] ", plops.pop(), plops.clop());
        }
        System.out.println();
    }
}
