package mills.score.generator;

import mills.bits.Player;
import mills.bits.PopCount;
import mills.index.PosIndex;

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
}
