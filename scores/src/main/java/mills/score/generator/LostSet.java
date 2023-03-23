package mills.score.generator;

import mills.bits.Player;
import mills.index.PosIndex;
import mills.score.Score;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 02.01.20
 * Time: 13:05
 */
public class LostSet extends ScoreSet {

    public LostSet(PosIndex index, Player player) {
        super(index, player);
    }

    @Override
    public int getScore(int index) {
        return Score.LOST.value;
    }

    @Override
    public String toString() {
        return "LostSet(" + super.toString() + ")";
    }
}
