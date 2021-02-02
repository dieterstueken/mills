package mills.ring;


import mills.util.ListSet;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.function.Predicate;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 15.10.11
 * Time: 22:41
 */

/**
 * An EntryTable is a List of RingEntries.
 * In addition it provides utility methods to get the RingEntry.index directly,
 * to find the table indexOf a given RingEntry and to generate filtered subsets of itself.
 */
public interface EntryTable extends ListSet<RingEntry> {

    // fast lookup of table index of a given ring index
    int findIndex(int ringIndex);

    int indexOf(RingEntry entry);

    /**
     * Return the index of the first element.index which is greater or equal than ringIndex.
     * The returned index is between [0, size]
     * @param ringIndex to find.
     * @return index of the first element which is greater than ringIndex.
     */
    int lowerBound(int ringIndex);

    /**
     * Return the index of the first element.index which is strictly greater than ringIndex.
     * The returned index is between [0, size]
     * @param ringIndex to find.
     * @return index of the first element which is greater than ringIndex.
     */
    int upperBound(int ringIndex);

    // shortcut
    default short ringIndex(int index) {
        return get(index).index;
    }

    //RingEntry getEntry(int index);

    @Override
    EntryTable subList(int fromIndex, int toIndex);

    EntryTable subSet(RingEntry fromElement, RingEntry toElement);

    @Override
    EntryTable headSet(RingEntry toElement);

    @Override
    EntryTable tailSet(RingEntry fromElement);

    EntryTable filter(Predicate<? super RingEntry> predicate);

    ////////////////////////////////////////////////////////////////////////////////////////////

    // an empty table template
    IndexedEntryTable EMPTY = EmptyTable.of();

    static EntryTable of(Iterator<? extends RingEntry> entries, int size) {

        if(size==0)
            return EMPTY;

        RingEntry e = entries.next();

        if(size==1)
            return SingleEntry.of(e.index);

        short[] index = new short[size];

        index [0] = e.index;
        boolean ordered = true;

        for(int i=1; i<size; i++) {
            RingEntry f = entries.next();
            index[i] = f.index;
            ordered &= e.index>f.index;
        }

        if(!ordered)
            Arrays.sort(index);

        return EntryArray.of(index);
    }

    static EntryTable of(Collection<? extends RingEntry> entries) {

        if(entries instanceof EntryTable) {
            return (EntryTable) entries;
        }

        final int size = entries.size();

        if(size==0)
            return EMPTY;

        return of(entries.iterator(), size);
    }
    
    static EntryTable of(int ... index) {
        short[] values = new short[index.length];
        int l=-1;

        for(int i=0; i<index.length; ++i) {
            int k = index[i];
            values[i] = (short) k;
            
            if(k<=l)
                throw new IllegalArgumentException("unordered ENtryTable");

            l = k;
        }

        return of(values, values.length);
    }

    static EntryTable of(short[] ringIndex, int size) {
        return of(ringIndex, 0, size);
    }

    static EntryTable of(short[] table, int fromIndex, int toIndex) {

        int size = toIndex - fromIndex;
        if(size==0)
            return EMPTY;

        if(size==1)
            return SingleEntry.of(table[fromIndex]);

        table = Arrays.copyOfRange(table, fromIndex, toIndex);
        return EntryArray.of(table);
    }
}
