package mills.score.opening;

import mills.bits.Clops;
import mills.bits.Player;
import mills.bits.PopCount;
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
public class PlopCloser {

    final Mover mover = Moves.TAKE.mover();

    final PlopSet source;
    final PlopLayer target;

    final PopCount tpop;

    Map<PopCount, PlopSet> targets = new HashMap<>();

    public PlopCloser(PlopSet source, PlopLayer target) {
        this.source = source;
        this.target = target;
        this.tpop = source.pop().sub(source.player().pop);
        Clops clops = Clops.get(tpop, source.clop());
        targets.put(source.clop(), target.plops(clops));
    }

    void run() {
        source.process(this::process);
    }

    private void process(int posIndex, long i201) {
        Player player = source.player();
        int stay = Stones.stones(i201, player.other());
        int move = Stones.stones(i201, player);

        // take any opponents(= current player) stone
        int take = move;

        mover.move(stay, move, take).normalize();
        mover.analyze(this::propagate);
    }

    private void propagate(long i201) {
        PopCount clop = Positions.clop(i201);
        PlopSet target = target(clop);
        target.setPos(i201);
    }

    private PlopSet target(PopCount clop) {
        return targets.computeIfAbsent(clop, this::findTarget);
    }

    private PlopSet findTarget(PopCount clop) {
        Clops clops = Clops.get(tpop, clop);
        return target.plops(clops);
    }
}
