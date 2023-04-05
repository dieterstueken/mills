package mills.score.generator;

import mills.bits.Player;
import mills.index.PosIndex;
import mills.score.Score;

import java.nio.ByteBuffer;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 25.03.23
 * Time: 15:16
 */
public class ScoreTarget extends ScoreMap {

    static final Logger LOGGER = Logger.getLogger(ScoreTarget.class.getName());

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

    public void stat(Level level) {
        if(!LOGGER.isLoggable(level))
            return;

        int won = 0;
        int lost = 0;
        int dawn = 0;
        int max = 0;

        for(int posIndex=0; posIndex<size(); ++posIndex) {
            int score = getScore(posIndex);
            if(Score.isWon(score))
                ++won;
            else if(Score.isLost(score))
                ++lost;
            else
                ++dawn;
            if(score>max)
                max = score;
        }

        LOGGER.log(level, String.format("stat %s D:%,d W:%,d L:%,d M:%d",
                this, dawn, won, lost, max));
    }
}
