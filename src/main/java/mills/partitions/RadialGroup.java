package mills.partitions;

import mills.ring.EntryTable;
import mills.ring.RingEntry;

import java.util.List;
import java.util.function.Predicate;
import java.util.function.ToIntFunction;

/**
 * version:     $Revision$
 * created by:  dst
 * created on:  22.07.2014 11:29
 * modified by: $Author$
 * modified on: $Date$
 */
interface RadialGroup {

    public int getKey(int radials);

    static RadialGroup of(short keys[]) {
        return keys==null ? null : radials -> keys[radials];
    }

    static RadialGroup build(EntryTable root, List<? extends Predicate<RingEntry>> filters, ToIntFunction<EntryTable> index) {

        short keys[] = null;

        for(int i=0; i<filters.size(); ++i) {
            EntryTable t = root.filter(filters.get(i));
            int key = index.applyAsInt(t);
            if(key!=0) {
                if(keys==null)
                    keys = new short[81];
                keys[i] = (short) key;
            }
        }

        return of(keys);
    }
}
