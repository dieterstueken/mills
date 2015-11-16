package mills.index4;

import mills.bits.PopCount;
import mills.ring.RingEntry;
import mills.util.AbstractRandomList;

import java.util.Comparator;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 11/15/15
 * Time: 6:20 PM
 */

/**
 * Class RdClop serves as an index combined of a mask of positions radials and a count of closed mills.
 */
public class RdClop {

    public static Comparator<RdClop> CMP = Comparator.comparingInt(RdClop::index);

    public static final List<RdClop> TABLE = AbstractRandomList.generate(81*25, RdClop::new);

    public static int index(RingEntry radials, PopCount clop) {
        return  81 * clop.index + radials.radix();
    }

    public static RdClop get(RingEntry radials, PopCount clop)  {
        return TABLE.get(index(radials, clop));
    }

    public static RdClop of(RingEntry entry) {
        return get(entry.radials(), entry.clop());
    }

    public final RingEntry radials;
    public final PopCount clop;

    private RdClop(int index) {
        radials = RingEntry.RADIALS.get((index%81));
        clop = PopCount.TABLE.get(index/81);
    }

    public int index() {
        return index(radials, clop);
    }

    public int hashCode() {
        return index();
    }
}
