package mills.index2.fragments;

import mills.ring.EntryTable;

import java.util.List;

/**
 * version:     $Revision$
 * created by:  dst
 * created on:  23.07.2015 08:34
 * modified by: $Author$
 * modified on: $Date$
 */
public class Partition {

    final EntryTable root;

    final List<Fragments> fragments;  // 128
    final List<Fragments> fragset;

    Partition(EntryTable root, List<Fragments> fragments, List<Fragments> fragset) {
        this.root = root;
        this.fragments = fragments;
        this.fragset = fragset;
    }
}
