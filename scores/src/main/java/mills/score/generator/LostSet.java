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
    ScoreSlice openSlice(int index) {
        ScoreSlice slice = super.openSlice(index);
        slice.dirty[Score.LOST.value] = -1;
        slice.max = Score.LOST.value;
        return slice;
    }

    @Override
    public String toString() {
        return super.toString() + " lost";
    }
}
