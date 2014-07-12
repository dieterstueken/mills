package mills.index;

import mills.bits.PopCount;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 16.09.2010
 * Time: 12:17:35
 */

public interface PosIndex {

    PopCount pop();

    int range();

    int posIndex(long i201);

    long i201(int posIndex);

    // run processor for a given range
    IndexProcessor process(IndexProcessor processor, int start, int end);

    default IndexProcessor process(IndexProcessor receiver) {
        return process(receiver, 0, Integer.MAX_VALUE);
    }
}
