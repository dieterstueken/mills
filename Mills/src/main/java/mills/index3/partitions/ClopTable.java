package mills.index3.partitions;

import mills.ring.EntryTable;
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
public class ClopTable extends AbstractRandomList<EntryTable> {

    final List<EntryTable> t0;

    ClopTable(List<EntryTable> t0) {
        this.t0 = t0;
    }

    public EntryTable get(int index) {
        return t0.get(index);
    }

    public int size() {
        return t0.size();
    }

    public static final ClopTable EMPTY = new ClopTable(Collections.emptyList()) {
        @Override
        public String toString() {
            return "empty";
        }
    };
}
