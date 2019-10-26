package mills.score.generator;

import mills.bits.Player;
import mills.index.PosIndex;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 26.10.19
 * Time: 17:50
 */
abstract public class ScoreSet {

    private final PosIndex index;

    private final Player player;

    public ScoreSet(PosIndex index, Player player) {
        this.index = index;
        this.player = player;
    }

    public Player player() {
        return player;
    }

    abstract public int getScore(int index);
}
