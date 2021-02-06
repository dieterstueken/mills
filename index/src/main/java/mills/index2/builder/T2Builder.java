package mills.index2.builder;

import mills.ring.IndexedEntryTable;
import mills.ring.RingEntry;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 04.02.21
 * Time: 18:23
 */
public class T2Builder {

    final List<RingEntry> r2Table = new ArrayList<>();
    final List<IndexedEntryTable> t0Table = new ArrayList<>();

    void add(RingEntry r2, IndexedEntryTable t0) {
        r2Table.add(r2);
        t0Table.add(t0);
    }

}
