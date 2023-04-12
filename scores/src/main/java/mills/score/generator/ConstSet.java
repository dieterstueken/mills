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
public class ConstSet extends ScoreSet {

    final int score;

    public ConstSet(PosIndex index, Player player, int score) {
        super(index, player);
        this.score = score;
    }

    public ConstSet(PosIndex index, Player player, Score score) {
        this(index, player, score.value);
    }

    public static ConstSet lost(PosIndex index, Player player) {
        return new ConstSet(index, player, Score.LOST);
    }

    @Override
    public int getScore(int index) {
        return score;
    }

    @Override
    ScoreSlice<ConstSet> openSlice(int index) {
        return new ScoreSlice<>(this, index) {

            @Override
            public int max() {
                return score;
            }

            @Override
            public long marked(Score s) {
                return s.value==score ? 0xfffffff : 0;
            }

            @Override
            public ConstSet scores() {
                return ConstSet.this;
            }
        };
    }

    @Override
    public String toString() {
        return super.toString() + "!" + score;
    }
}
