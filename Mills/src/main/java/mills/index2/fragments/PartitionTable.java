package mills.index2.fragments;

import mills.ring.EntryTable;

import java.util.Collection;
import java.util.Collections;

/**
 * version:     $Revision$
 * created by:  dst
 * created on:  23.07.2015 08:34
 * modified by: $Author$
 * modified on: $Date$
 */
abstract public class PartitionTable<T> {

    final EntryTable root;

    protected PartitionTable(EntryTable root) {
        this.root = root;
    }

    abstract public T get(int index);

    abstract public int size();

    // list of different entries
    public Collection<T> content() {
        return Collections.emptyList();
    }

    @Override
    public String toString() {
        return String.format("size = %d", content().size());
    }
}
