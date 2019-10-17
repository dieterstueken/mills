package mills.index2;

import mills.bits.PopCount;
import mills.ring.EntryTable;
import mills.ring.RingEntry;

import java.util.List;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 13.10.19
 * Time: 11:05
 */
public class Fragments {

    final List<List<EntryTable>> fragments;

    final Map<PopCount, List<EntryTable>> maps = Map.of(null, null);

    final List<EntryTable> roots;

    Fragments(List<List<EntryTable>> fragments, List<EntryTable> roots) {
        this.fragments = fragments;
        this.roots = roots;
    }

    public EntryTable get(PopCount clop, RingEntry rad) {
        return fragments.get(clop.index).get(rad.index);
    }
}
