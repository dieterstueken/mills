package mills.index3.partitions;

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

    @Override
    public boolean equals(Object o) {
        return o==this || (o instanceof PartitionTable) && content().equals(((PartitionTable)o).content());
    }

    @Override
    public int hashCode() {
        return content().hashCode();
    }
}
