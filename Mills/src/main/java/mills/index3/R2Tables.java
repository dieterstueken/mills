package mills.index3;

import mills.bits.PopCount;
import mills.index3.partitions.ClopTable;
import mills.ring.EntryTable;

import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 7/29/15
 * Time: 6:15 PM
 */
public class R2Tables {

    final PopCount pop;

    final EntryTable t2;

    final short i2[];

    final short[] t0;

    final List<ClopTable> r1;

    final int[] index;

    public R2Tables(PopCount pop, EntryTable t2, short[] i2, short[] t0, List<ClopTable> r1, int[] index) {
        this.pop = pop;
        this.t2 = t2;
        this.i2 = i2;
        this.t0 = t0;
        this.r1 = r1;
        this.index = index;
    }
}
