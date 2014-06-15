package mills.scores.opening2;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.LongBuffer;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 04.01.14
 * Time: 17:01
 */
public class DirectBitMap implements BitMap {

    final int size;

    final LongBuffer buffer;

    protected DirectBitMap(int size) {
        this.size = size;
        int capacity = (size + 63) / 64;
        buffer = ByteBuffer.allocateDirect(8*capacity)
            .order(ByteOrder.LITTLE_ENDIAN)
            .asLongBuffer();
    }

    @Override
    public boolean set(int posIndex) {

        int wordIndex = posIndex/64;
        int bitIndex = posIndex%64;
        long bits = buffer.get(wordIndex);
        long bitx = bits | (1L << bitIndex);

        // was already set
        if(bitx==bits)
            return true;

        buffer.put(wordIndex, bitx);
        return false;
    }

    @Override
    public boolean get(int posIndex) {

        if(buffer==null)
            return false;

        int wordIndex = posIndex/64;
        int bitIndex = posIndex%64;
        long bits = buffer.get(wordIndex);
        bits &= (1L << bitIndex);

        return bits != 0L;
    }

    public int size() {
        return size;
    }
}
