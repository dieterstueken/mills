package mills.score.generator;

import mills.bits.Player;
import mills.index.IndexProcessor;
import mills.index.PosIndex;
import mills.position.Position;
import mills.position.Positions;
import mills.stones.Mover;
import mills.stones.Moves;

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

    Mover mover(Player player) {
        return Moves.moves(jumps()).mover(player!=this.player());
    }

    abstract public int getScore(int index);

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

    ScoreSlice openSlice(int index) {
        return ScoreSlice.of(this, index);
    }

    Slices<? extends ScoreSlice> slices() {
        return Slices.generate(this, this::openSlice);
    }

    public ScoredPosition position(long i201) {
        return position(i201, player);
    }

    public ScoredPosition position(long i201, Player player) {
        boolean inverted = player==this.player;

        int posIndex = index.posIndex(inverted ? Positions.inverted(i201) : i201);
        int score = getScore(posIndex);

        // todo: max value

        return new ScoredPosition(i201, player, score);
    }
}
