package mills.ring;

import mills.util.IndexedSet;

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
 * In addition it provides utility methods to get the RingEntry.index directly,
 * to find the table indexOf a given RingEntry and to generate filtered subsets of itself.
 */
public interface EntryTable extends IndexedSet<RingEntry> {

    // fast lookup of table index of a given ring index
    int findIndex(int ringIndex);

    default int indexOf(RingEntry entry) {
        return findIndex(entry.index);
    }

    /**
     * Return the index of the first element.index which is greater or equal than ringIndex.
     * The returned index is between [0, size]
     * @param ringIndex to find.
     * @return index of the first element which is greater than ringIndex.
     */
    default int lowerBound(int ringIndex) {
        int index = findIndex(ringIndex);
        return index<0 ? -(index+1) : index;
    }

    /**
     * Return the index of the first element.index which is strictly greater than ringIndex.
     * The returned index is between [0, size]
     * @param ringIndex to find.
     * @return index of the first element which is greater than ringIndex.
     */
    default int upperBound(int ringIndex) {
        int index = findIndex(ringIndex);
        return index<0 ? -(index+1) : index+1;
    }

    @Override
    default int indexOf(Object obj) {

        if(obj instanceof RingEntry)
            return indexOf((RingEntry) obj);
        else
            return -1;
    }

    @Override
    default boolean contains(Object obj) {
        return indexOf(obj) >= 0;
    }

    // shortcut
    default short ringIndex(int index) {
        return get(index).index;
    }

    RingEntry getEntry(int index);

    @Override
    default EntryTable subList(int fromIndex, int toIndex) {
        return new SubTable(this, fromIndex, toIndex);
    }

    @Override
    default EntryTable subSet(RingEntry fromElement, RingEntry toElement) {
        return subList(lowerBound(fromElement.index), lowerBound(toElement.index));
    }

    @Override
    default EntryTable headSet(RingEntry toElement) {
        return subList(0, lowerBound(toElement.index));
    }

    @Override
    default EntryTable tailSet(RingEntry fromElement) {
        return subList(lowerBound(fromElement.index), size());
    }

    default EntryTable filter(Predicate<? super RingEntry> predicate) {

        if(predicate == Entries.ALL)
            return this;

        if(predicate == Entries.NONE)
            return EMPTY;

        int i0; // first match

        // find start of sequence
        for(i0=0; i0<size(); ++i0) {
            final RingEntry e = get(i0);
            if(predicate.test(e))
                break;
        }

        if(i0==size())
            return EMPTY;

        int i1; // first miss

        // find end of sequence
        for(i1=i0+1; i1<size(); ++i1) {
            final RingEntry e = get(i1);
            if(!predicate.test(e))
                break;
        }

        // full match
        if(i0==0 && i1==size())
            return this;

        // count filtered tables
        int count = i1-i0;
        int i2 = size(); // last match (if any)
        for(int i=i1+1; i<size(); ++i) {
            final RingEntry e = get(i);
            if(predicate.test(e)) {
                ++count;
                i2 = Math.min(i2, i);
            }
        }

        if(count==1)
            return get(i0).singleton;

        // may be a sub list
        if(i1-i0 == count)
            return subList(i0, i1);

        // have to generate a separate list
        short indexes[] = new short[count];
        count = 0;
        for(int i=i0; i<i1; ++i) {
            final RingEntry e = get(i);
            indexes[count++] = e.index;
        }

        for(int i=i2; i<size(); ++i) {
            final RingEntry e = get(i);
            if(predicate.test(e))
                indexes[count++] = e.index;
        }

        assert count == indexes.length : "filter mismatch";

        return EntryArray.of(indexes);
    }

    ////////////////////////////////////////////////////////////////////////////////////////////

    // an empty table template
    EmptyTable EMPTY = EmptyTable.of();

    static EntryTable of(List<? extends RingEntry> entries) {

        if(entries instanceof EntryTable) {
            return (EntryTable) entries;
        }
        
        final int size = entries.size();

        if(size==0)
            return EntryTable.EMPTY;

        RingEntry e = entries.get(0);

        if(size==1)
            return SingleEntry.of(e.index);

        short index[] = new short[size];

        index [0] = e.index;
        boolean ordered = true;

        for(int i=1; i<size; i++) {
            RingEntry f = entries.get(i);
            index[i] = f.index;
            ordered &= e.index>f.index;
        }

        if(!ordered)
            Arrays.sort(index);

        return EntryArray.of(index);
    }

    static EntryTable of(int ... index) {
        short[] values = new short[index.length];

        for(int i=0; i<index.length; ++i)
            values[i] = (short) index[i];

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
