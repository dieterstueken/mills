package mills.score.generator;

import mills.bits.Player;
import mills.bits.PopCount;
import mills.index.IndexProcessor;
import mills.index.PosIndex;
import mills.position.Position;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 26.10.19
 * Time: 17:50
 */
abstract public class ScoreSet implements AutoCloseable {

    final PosIndex index;

    private final Player player;

    public ScoreSet(PosIndex index, Player player) {
        this.index = index;
        this.player = player;
    }

    public int size() {
        return index.range();
    }

    public Player player() {
        return player;
    }

    abstract public int getScore(int index);

    public void setScore(int posIndex, int score) {
        throw new UnsupportedOperationException("setScore");
    }

    public void process(IndexProcessor processor, int base, int i) {
        index.process(processor, base, i);
    }

    public PopCount pop() {
        return index.pop();
    }

    public PopCount clop() {
        return index.clop();
    }

    public int posIndex(long i201) {
        return index.posIndex(i201);
    }

    public long i201(int posIndex) {
        return index.i201(posIndex);
    }

    public ScoredPosition position(long i201) {
        return new ScoredPosition(i201);
    }

    public void close() { }

    public class ScoredPosition extends Position {

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
            sb.insert(3, player().name().charAt(0));
            sb.append(" ").append(posIndex).append(" : ");
            sb.append(score);
            return sb;
        }
    }
}
