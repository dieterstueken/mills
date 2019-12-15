package mills.score.attic;

import mills.bits.Player;
import mills.index.IndexProcessor;
import mills.position.Position;
import mills.score.ScoreMap;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 07.10.13
 * Time: 18:02
 */
public class MapSlice {

    // either %= SIZE or & MAX_VALUE
    public static final int SIZE = Short.MAX_VALUE + 1;

    // a dirty block
    public static final int BLOCK = SIZE/Long.SIZE;

    public final ScoreMap map;

    public final int base;

    protected MapSlice(ScoreMap map, int index) {
        this.map = map;
        this.base = SIZE * index;
    }

    public String toString() {
        return String.format("%s@%d", map, sliceIndex());
    }

    public Player player() {
        return map.player();
    }

    public int size() {
        int size = map.size()-base;
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
        long i201 = map.i201(posIndex);

        //if(offset<0) // tag closed positions
        //    i201 |= Positions.CLOSED;

        return i201;
    }

    // debug
    public Position position(long i201) {
        return map.position(i201);
    }

    public Position position(short offset) {
        long i201 = i201(offset);
        return map.position(i201);
    }

    public int getScore(short offset) {
        int posIndex = posIndex(offset);

        return map.getScore(posIndex);
    }

    public void setScore(short offset, int score) {
        int posIndex = posIndex(offset);
        map.setScore(posIndex, score);
    }

    public void processAll(IndexProcessor processor) {
        map.process(processor, base, base + size());
    }

    public void close(final List<AtomicInteger> stat) {

        int size = size();

        final AtomicInteger stat0 = stat.get(0);

        for(int i=0; i<size; ++i) {

            short offset = (short) i;

            int score = getScore(offset);

            if(score<0) {
                int posIndex = posIndex(offset);
                map.setScore(posIndex, 0);
                stat0.incrementAndGet();
            } else
            if(score>0) {
                stat.get(score).incrementAndGet();
            }
        }
    }
}
