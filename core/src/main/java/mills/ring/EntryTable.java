package mills.ring;


import mills.util.listset.IndexedListSet;

import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 15.10.11
 * Time: 22:41
 */

/**
 * An EntryTable is a List of RingEntries.
 * In addition, it provides utility methods to get the RingEntry.index directly,
 * to find the table indexOf a given RingEntry and to generate filtered subsets of itself.
 * An EntryTable must always be sorted and immutable.
 */
public interface EntryTable extends IndexedListSet<RingEntry> {

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

    default RingEntry max() {
        int n = size();
        return n>0 ? get(n-1) : null;
    }

    @Override
    EntryTable subList(int fromIndex, int toIndex);

    EntryTable subSet(RingEntry fromElement, RingEntry toElement);

    @Override
    EntryTable headSet(RingEntry toElement);

    @Override
    default EntryTable headSet(int size) {
        return subList(0, size);
    }

    @Override
    EntryTable tailSet(RingEntry fromElement);

    EntryTable filter(Predicate<? super RingEntry> predicate);

    ////////////////////////////////////////////////////////////////////////////////////////////

    static EmptyTable empty() {
        return EmptyTable.EMPTY;
    }

    static EntryTable of(List<? extends RingEntry> entries) {

        if(entries instanceof EntryTable) {
            return (EntryTable) entries;
        }

        return AbstractEntryTable.of(entries);
    }
    
    static AbstractEntryTable of(int ... index) {
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

    static AbstractEntryTable of(short[] ringIndex, int size) {
        return of(ringIndex, 0, size);
    }

    static AbstractEntryTable of(short[] table, int fromIndex, int toIndex) {

        int size = toIndex - fromIndex;
        if(size==0)
            return EntryTable.empty();

        if(size==1)
            return SingletonTable.of(table[fromIndex]);

        table = Arrays.copyOfRange(table, fromIndex, toIndex);
        return EntryArray.of(table);
    }
}
