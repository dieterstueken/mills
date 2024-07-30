package mills.ring;


import mills.util.listset.AbstractIndexedSet;

import java.util.List;
import java.util.function.Predicate;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 15.10.11
 * Time: 22:41
 */

/**
 * Class AbstractEntryTable is the base of all stable EntryTable implementations.
 * The internal array to form the list must never be modified.
 */
abstract public class AbstractEntryTable extends AbstractIndexedSet<RingEntry> implements EntryTable {

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
            return EntryTable.of();

        if(size==1)
            return get(fromIndex).singleton;

        if(size==this.size())
            return this;

        return subSet(fromIndex, size);
    }

    @Override
    public EntryTable subSet(int fromIndex, int size) {
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
    public EntryTable headSet(int toIndex) {
        return subList(0, toIndex);
    }

    @Override
    public EntryTable tailSet(RingEntry fromElement) {
        return subList(lowerBound(fromElement.index), size());
    }

    public EntryTable filter(Predicate<? super RingEntry> predicate) {

        if(predicate == Entries.ALL)
            return this;

        if(predicate == Entries.NONE)
            return EntryTable.of();

        int i0; // first match

        // find start of sequence
        for(i0=0; i0<size(); ++i0) {
            final RingEntry e = get(i0);
            if(predicate.test(e))
                break;
        }

        if(i0==size())
            return EntryTable.of();

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
        short[] indexes = new short[count];
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

    /**
     * Transform a List of entries into a stable EntryTable.
     * @param list of entries.
     * @return a stable AbstractEntryTable.
     */
    static AbstractEntryTable of(List<? extends RingEntry> list) {

        if(list instanceof AbstractEntryTable)
            return (AbstractEntryTable) list;

        int size = list.size();

        if(size==0)
            return EmptyTable.EMPTY;

        RingEntry e = list.getFirst();
        if(size==1)
            return e.singleton;

        short[] index = new short[size];
        index [0] = e.index;
        boolean ordered = true;

        for(int i=1; i<size; i++) {
            RingEntry f = list.get(i);
            index[i] = f.index;
            ordered &= e.index<f.index;
            e=f;
        }

        if(!ordered) {
            throw new IllegalArgumentException("entries not sorted");
            // not sufficient: may have duplicates.
            //Arrays.sort(index);
        }

        return EntryArray.of(index);
    }
}
