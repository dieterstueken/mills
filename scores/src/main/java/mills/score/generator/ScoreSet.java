package mills.score.generator;

import mills.bits.Player;
import mills.index.IndexProcessor;
import mills.index.PosIndex;
import mills.position.Position;
import mills.position.Positions;
import mills.util.Stat;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 26.10.19
 * Time: 17:50
 */

/**
 * Class ScoreSet represents a read only view of scores for a given index (pop:clop).
 * ScoreSets may be purely virtual if they refer to an IndexLayer which is completely lost.
 */
abstract public class ScoreSet implements IndexLayer, AutoCloseable {

    final PosIndex index;

    final Player player;

    public ScoreSet(PosIndex index, Player player) {
        this.index = index;
        this.player = player;
    }

    public String toString() {
        return String.format("%s%c%s", pop(), player().key(), clop());
    }

    public int size() {
        return index.range();
    }

    abstract public int getScore(int index);

    public void process(IndexProcessor processor, int base, int end) {
        index.process(processor, base, end);
    }

    class IndexCounter implements IndexProcessor {

        final IndexProcessor delegate;
        final int score;
        int count = 0;

        IndexCounter(IndexProcessor delegate, int score) {
            this.delegate = delegate;
            this.score = score;
        }

        @Override
        public void process(int posIndex, long i201) {
            if(getScore(posIndex)==score) {
                delegate.process(posIndex, i201);
                ++count;
            }
        }
    }

    public PosIndex index() {
        return index;
    }

    public Player player() {
        return player;
    }

    public int posIndex(long i201) {
        int posIndex = index.posIndex(i201);

        if(posIndex<0) {
            Position pos = position(i201, player());
            index.posIndex(i201);
            throw new IllegalStateException("missing index on:" + pos.toString());
        }

        return posIndex;
    }

    public long i201(int posIndex) {
        return index.i201(posIndex);
    }

    public void close() { }

    public Stat stat() {
        Stat stat = new Stat();
        for(int i=0; i<index.range();++i) {
            int score = getScore(i);
            stat.accept(score);
        }

        return stat;
    }

    ScoreSlice openSlice(int index) {
        return ScoreSlice.of(this, index);
    }

    public ScoredPosition position(long i201) {
        return position(i201, player);
    }

    public ScoredPosition position(long i201, Player player) {
        boolean inverted = player!=this.player;

        int posIndex = index.posIndex(inverted ? Positions.inverted(i201) : i201);
        int score = getScore(posIndex);

        // todo: max value

        return new ScoredPosition(i201, player, score);
    }
}
