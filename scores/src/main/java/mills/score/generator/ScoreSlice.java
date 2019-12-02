package mills.score.generator;

import mills.bits.Player;
import mills.bits.PopCount;
import mills.index.IndexProcessor;
import mills.index.PosIndex;
import mills.position.Position;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 07.10.13
 * Time: 18:02
 */
abstract public class ScoreSlice {

    // either %= SIZE or & MAX_VALUE
    public static final int SIZE = Short.MAX_VALUE + 1;

    // a dirty block
    public static final int BLOCK = SIZE/Long.SIZE;


    public static int sliceCount(ScoreSet scores) {
        return (scores.size() + SIZE - 1) / SIZE;
    }

    public static int sliceCount(PosIndex index) {
        return (index.range() + SIZE - 1) / SIZE;
    }

    static ScoreSlice of(ScoreSet scores, int index) {
        return new ScoreSlice(index) {

            @Override
            public ScoreSet scores() {
                return scores;
            }
        };
    }

    /////////////////////////////////////////////////

    public final int base;

    // max score occurred
    private int max = 0;

    // any positions set
    private final long[] todo = new long[256];

    public int max() {
        return max;
    }

    public boolean any(int score) {
        return todo[score]!=0;
    }

    public long todo(int score) {
        long dirty = todo[score];
        todo[score] = 0;
        return dirty;
    }

    public void mark(short offset, int score) {

        // scores < 0 just pass by
        if (score > max)
            max = score;

        if(score>0)
            todo[score] |= mask(offset);
        else if(score<0)
            todo[0] |= mask(offset);
    }

    protected ScoreSlice(int index) {
        this.base = SIZE * index;
    }

    /**
     * Create a read only slice from scores.
     * @param index of this slice.
     * @return a read only slice;
     */
    ScoreSlice newSlice(int index) {
        ScoreSet scores = scores();
        return new ScoreSlice(index) {
            @Override
            public ScoreSet scores() {
                return scores;
            }
        };
    }

    @Override
    public String toString() {
            return String.format("%s[%d]@%d", scores(), sliceIndex(), max);
        }

    abstract public ScoreSet scores();

    public PopCount pop() {
        return scores().pop();
    }

    public PopCount clop() {
        return scores().clop();
    }

    public Player player() {
        return scores().player();
    }

    public int size() {
        int size = scores().size()-base;
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
        long i201 = scores().i201(posIndex);

        //if(offset<0) // tag closed positions
        //    i201 |= Positions.CLOSED;

        return i201;
    }

    // debug
    public Position position(long i201) {
        return scores().position(i201);
    }

    public Position position(short offset) {
        long i201 = i201(offset);
        return scores().position(i201);
    }

    public int getScore(short offset) {
        int posIndex = posIndex(offset);
        return getScore(posIndex);
    }

    public int getScore(int posIndex) {

        assert posIndex>=base;
        assert posIndex<base+SIZE;

        int score = scores().getScore(posIndex);

        if(max>0 && score>max) // should not happen if map.max was updated properly
            score -= 256;

        return score;
    }

    /**
     * Process any dirty blocks of score.
     * @param processor to process
     * @param score to analyze
     * @return previous dirty flags.
     */
    public void processScores(IndexProcessor processor, int score) {

        final long dirty = todo(score);

        if(dirty==0)
            return;

        processor = scores().filter(processor, score);

        if(dirty==-1) {
            // process all
            process(processor);
            return;
        }

        int start = base;
        final int next = base+size();
        long todo = dirty;

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

            scores().process(processor, start, end);
            start += len*BLOCK;
        }
    }

    public void process(IndexProcessor processor) {
        scores().process(processor, base, base + size());
    }

    public void close() {

    }
}