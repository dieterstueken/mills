package mills.index2.fragments;

import mills.ring.EntryTable;
import mills.util.AbstractRandomList;

import java.util.Collection;
import java.util.Collections;

/**
 * version:     $Revision$
 * created by:  dst
 * created on:  23.07.2015 08:34
 * modified by: $Author$
 * modified on: $Date$
 */
abstract public class PartitionTable<T> extends AbstractRandomList<T> {

    final EntryTable root;

    protected PartitionTable(EntryTable root) {
        this.root = root;
    }

    // list of different entries
    Collection<T> values() {
        return Collections.emptyList();
    }
}
