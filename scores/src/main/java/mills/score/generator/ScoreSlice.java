package mills.score.generator;

import mills.bits.Player;
import mills.bits.PopCount;
import mills.index.IndexProcessor;
import mills.position.Position;
import mills.score.Score;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 07.10.13
 * Time: 18:02
 */

/**
 * Class ScoreSlice provides a slice of 32k scores.
 * Thus, a short may be used as local index.
 * A lookup bitmap for each score tags those parts of a slice containing that score.
 */
abstract public class ScoreSlice<Scores extends ScoreSet> {

    // either %= SIZE or & MAX_VALUE
    public static final int SIZE = Short.MAX_VALUE + 1;

    // a dirty block
    public static final int BLOCK = SIZE/Long.SIZE;

    public static int sliceCount(ScoreSet scores) {
        return (scores.size() + SIZE - 1) / SIZE;
    }

    /////////////////////////////////////////////////

    protected final Scores scores;

    protected final int base;

    public Scores scores() {
        return scores;
    }

    abstract public int max();

    public boolean any(Score score) {
        //return marked(score)!=0;
        return score.value<=max();
    }

    abstract public long marked(Score score);

    protected ScoreSlice(Scores scores, int index) {
        this.scores = scores;
        this.base = SIZE * index;
    }

    @Override
    public String toString() {
        return String.format("%s[%d]@%d", scores(), sliceIndex(), max());
    }

    public PopCount pop() {
        return scores.pop();
    }

    public PopCount clop() {
        return scores.clop();
    }

    public Player player() {
        return scores.player();
    }

    public int size() {
        int size = scores.size()-base;
        return Math.min(size, SIZE);
    }

    public static long mask(short offset) {
        return 1L<<(offset/BLOCK);
    }

    public int sliceIndex() {
        return base / SIZE;
    }

    public short offset(int posIndex) {
        posIndex -= base;
        //assert posIndex >= 0 && posIndex < SIZE;
        // SIZE maps to -1
        return (short) (posIndex&0xffff);
    }

    public int posIndex(short offset) {
        int index = offset;
        // strip off sign bit
        index &= Short.MAX_VALUE;
        return base + index;
    }

    public long i201(short offset) {
        if(offset<0)
            throw new IllegalArgumentException("negative offset");

        int posIndex = posIndex(offset);
        long i201 = scores.i201(posIndex);

        //if(offset<0) // tag closed positions
        //    i201 |= Positions.CLOSED;

        return i201;
    }

    // debug
    public Position position(long i201) {
        return scores.position(i201);
    }

    public Position position(short offset) {
        long i201 = i201(offset);
        return scores.position(i201);
    }

    public int getScore(short offset) {
        int posIndex = posIndex(offset);
        return getScore(posIndex);
    }

    public int getScore(int posIndex) {

        assert posIndex>=base;
        assert posIndex<base+SIZE;

        return scores().getScore(posIndex);
    }

    /**
     * Process any dirty blocks of score.
     * @param processor to process
     * @param score to analyze
     * @return number of analyzed scores.
     */
    public int processScores(IndexProcessor processor, Score score) {

        final long marked = marked(score);

        if(marked==0)
            return 0;

        ScoreSet.IndexCounter counter = scores.new IndexCounter(processor, score.value);

        if(marked==-1) {
            // process all
            process(counter);
            return counter.count;
        }

        int start = base;
        final int next = base+size();
        long todo = marked;

        while(todo!=0) {
            final int skip = Long.numberOfTrailingZeros(todo);

            if(skip>0) {
                start += skip*BLOCK;
                todo >>>= skip;
            }

            final int len = Long.numberOfTrailingZeros(~todo);
            assert len<64;
            todo >>>= len;

            final int end = Math.min(start + len*BLOCK, next);

            assert end>start : "empty range";

            scores.process(counter, start, end);
            start += len*BLOCK;
        }

        return counter.count;
    }

    public void process(IndexProcessor processor) {
        scores.process(processor, base, base + size());
    }
}