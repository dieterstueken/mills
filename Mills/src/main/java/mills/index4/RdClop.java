package mills.index4;

import mills.bits.PopCount;
import mills.ring.EntryTable;
import mills.ring.RingEntry;
import mills.util.AbstractRandomArray;
import mills.util.AbstractRandomList;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

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

    public static final Comparator<RdClop> CMP = Comparator.comparingInt(RdClop::index);

    public static final int COUNT = 81*25;
    public static final List<RdClop> TABLE = AbstractRandomList.generate(COUNT, RdClop::new);

    final List<EntryTable> SUBSETS = AbstractRandomArray.map(RingEntry.RADIALS,
            rad->RingEntry.RADIALS.filter(rad::contains)
    );

    public static int index(RingEntry radials, PopCount clop) {
        return  25*radials.radix() + clop.index;
    }

    // lookup given radials and clop, or null if clop>4
    public static RdClop get(RingEntry radials, PopCount clop)  {
        return clop==null || clop.max()>4? null : TABLE.get(index(radials, clop));
    }

    public static RdClop of(RingEntry entry) {
        return get(entry.radials(), entry.clop());
    }

    public final RdClop radials(RingEntry new_radials) {
        return get(new_radials, this.clop);
    }

    public final RingEntry radials;
    public final PopCount clop;

    /**
     * Treat all radials as closed and add them to the closed count.
     * If any count of closed mills is >4 return null.
     * @return a total RdClop adding all radial positions.
     */
    public RdClop closed() {
        return get(radials, radials.pop().add(clop));
    }

    public Stream<RdClop> subsets() {
        return SUBSETS.get(radials.radix()).stream().map(this::radials);
    }

    private RdClop(int index) {
        radials = RingEntry.RADIALS.get((index/25));
        clop = PopCount.TABLE.get(index%25);
    }

    public int index() {
        return index(radials, clop);
    }

    public int hashCode() {
        return index();
    }

    // Object.equal() is sufficient

    public String toString() {
        return String.format("%sx%02d", clop, radials.radix());
    }
}
