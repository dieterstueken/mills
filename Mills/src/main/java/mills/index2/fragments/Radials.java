package mills.index2.fragments;

import mills.ring.EntryTable;
import mills.ring.RingEntry;
import mills.util.AbstractRandomList;

import java.util.Collections;
import java.util.List;

/**
 * version:     $Revision$
 * created by:  dst
 * created on:  23.07.2015 08:36
 * modified by: $Author$
 * modified on: $Date$
 */
public class Radials {

    static final Radials EMPTY = new Radials(EntryTable.EMPTY, AbstractRandomList.virtual(81, i -> Fragments.EMPTY), Collections.emptyList());

    final EntryTable root;

    final List<Fragments> fragments;
    final List<Fragments> fragset;

    Radials(EntryTable root, List<Fragments> fragments, List<Fragments> fragset) {
        this.root = root;
        this.fragments = fragments;
        this.fragset = fragset;
    }

    public Fragments get(RingEntry radial) {
        return fragments.get(radial.radix());
    }
}
