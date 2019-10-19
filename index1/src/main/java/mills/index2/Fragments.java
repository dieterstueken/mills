package mills.index2;

import mills.bits.PopCount;
import mills.ring.Entries;
import mills.ring.EntryTable;
import mills.ring.RingEntry;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 13.10.19
 * Time: 11:05
 */
public class Fragments {

    static final Fragments EMPTY = new Fragments() {
        @Override
        public String toString() {
            return "F[]";
        }
    };

    static final int CLOPS = PopCount.CLOSED.size();
    static final int RADS = Entries.RADIALS.size();

    private final Map<PopCount, Map<RingEntry, EntryTable>> fragments;

    private final Set<EntryTable> roots;

    private Fragments() {
        this.fragments = Collections.emptyMap();
        this.roots = EntryTable.EMPTY.singleton();
    }

    Fragments(Map<PopCount, Map<RingEntry, EntryTable>> fragments, Set<EntryTable> roots) {
        this.fragments = fragments;
        this.roots = roots;
    }

    public EntryTable get(PopCount clop, RingEntry rad) {
        Map<RingEntry, EntryTable> fragment = fragments.get(clop);
        return fragment==null ? EntryTable.EMPTY : fragment.get(rad);
    }

    public String toString() {
        return String.format("F[%d;%d]", fragments.size(), roots.size()-1);
    }
}
