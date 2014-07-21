package mills.partitions;

import mills.bits.PopCount;
import mills.ring.RingEntry;
import mills.util.AbstractRandomList;

import java.util.List;
import java.util.function.Predicate;

/**
 * version:     $Revision$
 * created by:  dst
 * created on:  21.07.2014 11:29
 * modified by: $Author$
 * modified on: $Date$
 */
public class GroupFilter implements Predicate<RingEntry>, Comparable<GroupFilter> {

    final PopCount clop;
    final Radials radials;

    GroupFilter(int index) {
        this.clop = PopCount.TABLE.get(index/81);
        this.radials = Radials.of(index%81);
    }

    public int hashCode() {
        return 81*clop.index  + radials.hashCode();
    }

    public String toString() {
        return String.format("%s:%s", clop.toString(), radials.toString());
    }

    @Override
    public boolean test(RingEntry e) {
        return e.closed().add(radials.apply(e)).equals(clop);
    }

    static final List<GroupFilter> FILTERS = AbstractRandomList.generate(81*25, GroupFilter::new);

    @Override
    public int compareTo(GroupFilter o) {
        return Integer.compare(hashCode(), o.hashCode());
    }

    public static GroupFilter of(PopCount clop, int radials) {
        int index = 81*clop.index + radials;
        return FILTERS.get(index);
    }
}
