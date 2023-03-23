package mills.score.generator;

import mills.bits.Player;
import mills.bits.PopCount;
import mills.index.PosIndex;

public class IndexLayer implements ClopLayer {

    public final PosIndex index;

    public final Player player;

    public IndexLayer(PosIndex index, Player player) {
        this.index = index;
        this.player = player;
    }

    public PosIndex index() {
        return index;
    }

    public PopCount pop() {
        return index().pop();
    }

    public PopCount clop() {
        return index().clop();
    }

    public Player player() {
        return player;
    }

    public String toString() {
        return String.format("%s%c%s", pop(), player().key(), clop());
    }
}
