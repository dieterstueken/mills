package mills.score.opening;

import mills.bits.Clops;
import mills.bits.Player;
import mills.index.IndexProvider;
import mills.index.PosIndex;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 12.11.19
 * Time: 17:58
 */
class PlopLayer extends Plop {

    final IndexProvider indexes;

    Map<Clops, PlopSet> play = new ConcurrentHashMap<>();

    Map<Clops, PlopSet> closed = new ConcurrentHashMap<>();

    PlopLayer(IndexProvider indexes, int layer) {
        super(layer);
        this.indexes = indexes;
    }

    PlopSet plops(Clops clops) {
        PosIndex index = index(clops);
        return new PlopSet(this, index);
    }

    PosIndex index(Clops clops) {
        return indexes.build(clops);
    }

    public Player player() {
        return plop.sum()%2==0 ? Player.White : Player.Black;
    }

    PlopSet play(Clops clops) {
        return play.computeIfAbsent(clops, this::plops);
    }

    PlopSet closed(Clops clops) {
        return closed.computeIfAbsent(clops, this::plops);
    }

    void propagate(PlopLayer target) {

        for (PlopSet plops : closed.values()) {
            new PlopCloser(plops, this).run();
        }

        for (PlopSet plops : play.values()) {
            new PlopMover(plops, target).run();
        }
    }
}
