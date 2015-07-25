package mills.index2.fragments;

import mills.bits.PopCount;
import mills.ring.EntryTable;
import mills.util.AbstractRandomList;

import java.util.List;

/**
 * version:     $Revision$
 * created by:  dst
 * created on:  23.07.2015 08:36
 * modified by: $Author$
 * modified on: $Date$
 */
public class Fragments {

    static final Fragments EMPTY = new Fragments(EntryTable.EMPTY, AbstractRandomList.virtual(25, i -> Fragment.EMPTY));

    final EntryTable root;

    final List<Fragment> fragments;

    Fragments(EntryTable root, List<Fragment> fragments) {
        this.root = root;
        this.fragments = fragments;
    }

    public Fragment get(PopCount clop) {
        return fragments.get(clop.index);
    }
}
