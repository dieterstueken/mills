package mills.index3.partitions;

import mills.ring.EntryTable;

import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

/**
 * version:     $Revision$
 * created by:  dst
 * created on:  24.07.2015 18:00
 * modified by: $Author$
 * modified on: $Date$
 */
public class ClopTable {

    final List<EntryTable> t0;

    ClopTable(List<EntryTable> t0) {
        this.t0 = t0;
    }

    public EntryTable get(int index) {
        return t0.get(index);
    }

    public boolean isEmpty() {
        return t0.isEmpty();
    }

    public int size() {
        return t0.size();
    }

    public void forEach(Consumer<? super EntryTable> action) {
        t0.forEach(action);
    }

    public static final ClopTable EMPTY = new ClopTable(Collections.emptyList()) {
        @Override
        public String toString() {
            return "empty";
        }
    };
}
