package mills.index2;

import mills.bits.PopCount;
import mills.ring.Entries;
import mills.ring.EntryTable;
import mills.ring.RingEntry;
import mills.util.AbstractRandomList;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

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

    final List<List<EntryTable>> fragments;

    final Map<PopCount, List<EntryTable>> maps;

    final List<EntryTable> roots;

    private Fragments() {
        this.fragments = AbstractRandomList.virtual(CLOPS, null);
        this.roots = EntryTable.EMPTY.singleton();
        this.maps = Collections.emptyMap();
    }

    Fragments(List<List<EntryTable>> fragments, List<EntryTable> roots) {
        this.fragments = fragments;
        this.roots = roots;

        maps = new TreeMap<>();
        for (PopCount clop : PopCount.CLOSED) {
            List<EntryTable> fragment = fragments.get(clop.index);
            if(fragment!=null)
                maps.put(clop, fragment);
        }
    }

    public EntryTable get(PopCount clop, RingEntry rad) {
        List<EntryTable> fragment = fragments.get(clop.index);
        return fragment==null ? EntryTable.EMPTY : fragment.get(rad.index);
    }

    public String toString() {
        return String.format("F[%d;%d]", maps.size(), roots.size()-1);
    }

    public EntryTable root() {
        return roots.get(0);
    }
}
