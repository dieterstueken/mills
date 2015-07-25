package mills.index2.fragments;

import mills.ring.EntryTable;
import mills.ring.RingEntry;
import mills.util.AbstractRandomList;

import java.util.Collections;
import java.util.List;

/**
 * version:     $Revision$
 * created by:  dst
 * created on:  24.07.2015 18:00
 * modified by: $Author$
 * modified on: $Date$
 */
public class Fragment extends AbstractRandomList<EntryTable> {

    public static final Fragment EMPTY = new Fragment(Collections.emptyList(), Collections.emptyList());

    final List<EntryTable> fragments;
    final List<EntryTable> radials;

    public Fragment(List<EntryTable> fragments, List<EntryTable> radials) {
        this.fragments = fragments;
        this.radials = radials;
    }

    public EntryTable get(RingEntry radial) {
        return get(radial.radix());
    }

    @Override
    public EntryTable get(int index) {
        RingEntry rad = RingEntry.radix(index);
        for(int i=0; i<fragments.size(); ++i) {
            if(radials.get(i).contains(rad))
                return fragments.get(i);
        }

        return EntryTable.EMPTY;
    }

    @Override
    public int size() {
        return 81;
    }
}
