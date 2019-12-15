package mills.score.generator;

import mills.bits.Player;
import mills.index.IndexProcessor;
import mills.index.PosIndex;
import mills.position.Position;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 26.10.19
 * Time: 17:50
 */
abstract public class ScoreSet implements IndexLayer, AutoCloseable {

    final PosIndex index;

    final Player player;

    public ScoreSet(PosIndex index, Player player) {
        this.index = index;
        this.player = player;
    }

    public int size() {
        return index.range();
    }

    abstract public int getScore(int index);

    public void setScore(int posIndex, int score) {
        throw new UnsupportedOperationException("setScore");
    }

    public void process(IndexProcessor processor, int base, int end) {
        index.process(processor, base, end);
    }

    IndexProcessor filter(IndexProcessor processor, int score) {
        return (posIndex, i201) -> {
            if(getScore(posIndex)==score)
                processor.process(posIndex, i201);
        };
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
            Position pos = position(i201);
            index.posIndex(i201);
            throw new IllegalStateException("missing index on:" + pos.toString());
        }

        return posIndex;
    }

    public long i201(int posIndex) {
        return index.i201(posIndex);
    }

    public ScoredPosition position(long i201) {
        return new ScoredPosition(i201);
    }

    public void close() { }

    ScoreSlice openSlice(int index) {
        return ScoreSlice.of(this, index);
    }

    Slices<? extends ScoreSlice> slices() {
        return Slices.generate(this, this::openSlice);
    }

    private class ScoredPosition extends Position {

        final ScoredPosition normalized;

        final int posIndex;
        final int score;

        public ScoredPosition(long i201) {
            super(i201);

            posIndex = index.posIndex(i201);
            score = posIndex < 0 ? -1 : getScore(posIndex);

            if (super.normalized)
                normalized = this;
            else
                normalized = position(i201(posIndex));
        }

        public ScoredPosition position(long i201) {
            return new ScoredPosition(i201);
        }

        @Override
        public StringBuilder format(StringBuilder sb) {
            sb = super.format(sb);
            sb.insert(3, player().key());
            sb.append(" ").append(posIndex).append(" : ");
            sb.append(score);
            return sb;
        }
    }
}
