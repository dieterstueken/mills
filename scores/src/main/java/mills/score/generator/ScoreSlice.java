package mills.score.generator;

import mills.bits.Player;
import mills.index.IndexProcessor;
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

    public final int base;

    protected ScoreSlice(int index) {
        this.base = SIZE * index;
    }

    /**
     * Create a read only slice from scores.
     * @param scores to offer.
     * @param index of this slice.
     * @return a read only slice;
     */
    static ScoreSlice newSlice(ScoreSet scores, int index) {
        return new ScoreSlice(index) {
            @Override
            public ScoreSet scores() {
                return scores;
            }
        };
    }

    public String toString() {
        return String.format("%s@%d", scores(), sliceIndex());
    }

    abstract public ScoreSet scores();

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

        return scores().getScore(posIndex);
    }

    public void processAll(IndexProcessor processor) {
        scores().process(processor, base, base + size());
    }

    public void close() {

    }
}
