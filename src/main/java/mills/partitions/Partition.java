package mills.partitions;

import mills.bits.PopCount;
import mills.ring.EntryTable;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 7/21/14
 * Time: 9:14 AM
 */
public class Partition {

    final List<EntryTable> tables = new ArrayList<>();

    public short getKey(int msk, PopCount clop, int radials) {
        return -1;
    }
}
