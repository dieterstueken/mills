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

    Map<Clops, PlopSet> plops = new ConcurrentHashMap<>();

    PlopLayer(IndexProvider indexes, int layer) {
        super(layer);
        this.indexes = indexes;
    }

    protected PlopLayer(PlopLayer parent) {
        super(parent);
        this.indexes = parent.indexes;
    }

    private PlopSet _plops(Clops clops) {
        PosIndex index = index(clops);
        return new PlopSet(this, index);
    }

    PosIndex index(Clops clops) {
        return indexes.build(clops);
    }

    public Player player() {
        return plop.sum()%2==0 ? Player.White : Player.Black;
    }

    PlopSet plops(Clops clops) {
        return plops.computeIfAbsent(clops, this::_plops);
    }
}
