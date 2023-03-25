package mills.score.generator;

import mills.bits.Player;
import mills.index.PosIndex;

import java.nio.ByteBuffer;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 25.03.23
 * Time: 15:16
 */
public class ScoreTarget extends ScoreMap {

    public ScoreTarget(PosIndex index, Player player, ByteBuffer scores) {
        super(index, player, scores);
    }

    public void setScore(int posIndex, int score) {
        byte value = (byte) (score&0xff);
        scores.put(posIndex, value);
    }

    @Override
    public TargetSlice openSlice(int index) {
        return new TargetSlice(this, index);
    }

    public static ScoreTarget allocate(IndexLayer layer) {
        return allocate(layer.index(), layer.player());
    }

    public static ScoreTarget allocate(PosIndex index, Player player) {
        ByteBuffer buffer = ByteBuffer.allocateDirect(index.range());
        return new ScoreTarget(index, player, buffer);
    }
}
