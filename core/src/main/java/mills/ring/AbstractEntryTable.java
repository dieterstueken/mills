package mills.ring;


import mills.util.AbstractListSet;
import mills.util.Indexed;
import mills.util.Indexer;

import java.util.Comparator;
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
 */
abstract public class AbstractEntryTable extends AbstractListSet<RingEntry> implements EntryTable {

    @Override
    public Comparator<Indexed> comparator() {
        return Indexer.INDEXED;
    }

    public int indexOf(RingEntry entry) {
        return findIndex(entry.index);
    }

    @Override
    public boolean contains(Object obj) {
        return indexOf(obj) >= 0;
    }

    /**
     * Return the index of the first element.index which is greater or equal than ringIndex.
     * The returned index is between [0, size]
     * @param ringIndex to find.
     * @return index of the first element which is greater than ringIndex.
     */
    public int lowerBound(int ringIndex) {
        int index = findIndex(ringIndex);
        return index<0 ? -(index+1) : index;
    }

    /**
     * Return the index of the first element.index which is strictly greater than ringIndex.
     * The returned index is between [0, size]
     * @param ringIndex to find.
     * @return index of the first element which is greater than ringIndex.
     */
    public int upperBound(int ringIndex) {
        int index = findIndex(ringIndex);
        return index<0 ? -(index+1) : index+1;
    }

    @Override
    public int indexOf(Object obj) {

        if(obj instanceof RingEntry)
            return indexOf((RingEntry) obj);
        else
            return -1;
    }

    @Override
    public EntryTable subList(int fromIndex, int toIndex) {
        int size = checkRange(fromIndex, toIndex);

        if(size==0)
            return EntryTable.EMPTY;

        if(size==1)
            return get(fromIndex).singleton;

        if(size==this.size())
            return this;

        return partition(fromIndex, size);
    }

    public EntryTable partition(int fromIndex, int size) {
        return new SubTable(this, fromIndex, size);
    }

    @Override
    public EntryTable subSet(RingEntry fromElement, RingEntry toElement) {
        return subList(lowerBound(fromElement.index), lowerBound(toElement.index));
    }

    @Override
    public EntryTable headSet(RingEntry toElement) {
        return subList(0, lowerBound(toElement.index));
    }

    @Override
    public EntryTable tailSet(RingEntry fromElement) {
        return subList(lowerBound(fromElement.index), size());
    }

    public EntryTable filter(Predicate<? super RingEntry> predicate) {

        if(predicate == Entries.ALL)
            return this;

        if(predicate == Entries.NONE)
            return EntryTable.EMPTY;

        int i0; // first match

        // find start of sequence
        for(i0=0; i0<size(); ++i0) {
            final RingEntry e = get(i0);
            if(predicate.test(e))
                break;
        }

        if(i0==size())
            return EntryTable.EMPTY;

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
}
