package mills.score.generator;

import mills.bits.Player;
import mills.bits.PopCount;
import mills.index.PosIndex;
import mills.util.AbstractRandomList;
import mills.util.PopMap;

import java.util.List;

public interface IndexLayer extends ClopLayer {

    PosIndex index();

    default PopCount pop() {
        return index().pop();
    }

    default PopCount clop() {
        return index().clop();
    }

    Player player();

    static IndexLayer of(PosIndex index, Player player) {
        return new IndexLayer() {

            @Override
            public PosIndex index() {
                return index;
            }

            @Override
            public Player player() {
                return player;
            }
        };
    }

    static PopMap<IndexLayer> group(PosIndex index, Player player) {
        PopMap<? extends PosIndex> group = index.group();
        List<IndexLayer> values = AbstractRandomList.transform(group.values(), pix -> of(pix, player)).copyOf();
        return PopMap.of(group.keySet(), values);
    }
}
