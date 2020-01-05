package mills.score.generator;

import mills.bits.Player;
import mills.index.PosIndex;

import java.nio.ByteBuffer;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 26.10.19
 * Time: 17:52
 */
public class ScoreMap extends ScoreSet {

    protected final ByteBuffer scores;

    public ScoreMap(PosIndex index, Player player, ByteBuffer scores) {
        super(index, player);
        this.scores = scores;
    }

    @Override
    public int getScore(int index) {
        int value = scores.get(index);
        value &= 0xff;  // clip off sign bit

        return value;
    }

    public void setScore(int posIndex, int score) {
        byte value = (byte) (score&0xff);
        scores.put(posIndex, value);
    }

    MapSlice openSlice(int index) {
        return MapSlice.of(this, index);
    }
}
