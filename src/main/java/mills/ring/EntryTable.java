package mills.ring;

import com.google.common.collect.Iterables;
import mills.util.AbstractRandomList;

import java.util.*;
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
public abstract class EntryTable extends AbstractRandomList<RingEntry> implements SortedSet<RingEntry>, Comparable<EntryTable> {

    // fast lookup of table index of a given ring index
    abstract public int findIndex(int ringIndex);

    public int indexOf(RingEntry entry) {
        return findIndex(entry.index);
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
        if(obj instanceof Number)
            return findIndex((((Number) obj).shortValue()));
        else
            return -1;
    }

    public boolean contains(Object obj) {
        return obj instanceof RingEntry && indexOf((RingEntry) obj) >= 0;
    }

    // shortcut
    public short ringIndex(int index) {
        return get(index).index;
    }

    public RingEntry[] toArray() {
        return Iterables.toArray(this, RingEntry.class);
    }

    @Override
    abstract public EntryTable subList(int fromIndex, int toIndex);

    @Override
    public Comparator<? super RingEntry> comparator() {
        return RingEntry.COMPARATOR;
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

    @Override
    public RingEntry first() {

        if(isEmpty())
            throw new NoSuchElementException();

        return get(0);
    }

    @Override
    public RingEntry last() {
        if(isEmpty())
            throw new NoSuchElementException();

        return get(size()-1);
    }

    @Override
    public Spliterator<RingEntry> spliterator() {
        return Spliterators.spliterator(this, Spliterator.DISTINCT | Spliterator.SORTED | Spliterator.ORDERED | Spliterator.IMMUTABLE);
    }

    public EntryTable filter(Predicate<? super RingEntry> predicate) {

        if(predicate == ALL)
            return this;

        if(predicate == NONE)
            return EMPTY;

        int i0;

        // find start of sequence
        for(i0=0; i0<size(); ++i0) {
            final RingEntry e = get(i0);
            if(predicate.test(e))
                break;
        }

        if(i0==size())
            return EMPTY;

        // find end of sequence
        int i1;
        for(i1=i0+1; i1<size(); ++i1) {
            final RingEntry e = get(i1);
            if(!predicate.test(e))
                break;
        }

        // full match
        if(i0==0 && i1==size())
            return this;

        // count filtered entries
        int count = i1-i0;
        int i2 = size();
        for(int i=i1+1; i<size(); ++i) {
            final RingEntry e = get(i);
            if(predicate.test(e)) {
                ++count;
                i2 = Math.min(i2, i);
            }
        }

        if(count==1)
            return get(i0).singleton;

        // have to generate a sub list

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

        return new IndexTable(indexes);
    }

    @Override
    public int compareTo(EntryTable o) {
        if(o==null)
            return 1;

        int result = Integer.compare(size(), o.size());

        if(result==0)
        for(int i=0; i<size(); ++i) {
            result = Integer.compare(ringIndex(i), o.ringIndex(i));
            if(result!=0)
                break;
        }

        return result;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////

    public static final Comparator<List<RingEntry>> COMPARATOR = new Comparator<List<RingEntry>>() {
        @Override
        public int compare(List<RingEntry> t1, List<RingEntry> t2) {

            if(t1==t2)
                return 0;

            if(t1==null)
                return -1;

            int result = Integer.compare(t1.size(), t2.size());

            for(int i=0; result==0 && i<t1.size(); ++i)
                result = RingEntry.COMPARATOR.compare(t1.get(i), t2.get(i));

            return result;
        }
    };

    public static final Predicate<RingEntry> ALL  = e -> true;
    public static final Predicate<RingEntry> NONE = e -> false;

    // an empty table template
    public static final EmptyTable EMPTY = new EmptyTable();

    public static EntryTable of(List<RingEntry> entries) {
        if(entries instanceof EntryTable)
            return (EntryTable) entries;

        final int size = entries.size();

        if(size==0)
            return RingTable.EMPTY;

        if(size==1)
            return SingleEntry.of(entries.get(0).index());

        short ringIndex[] = new short[size];

        for(int i=0; i<size; i++)
            ringIndex[i] = entries.get(i).index();

        return IndexTable.of(ringIndex);
    }

    public static EntryTable of(short[] ringIndex, int size) {
        return of(ringIndex, 0, size);
    }

    public static EntryTable of(int ... index) {
        short values[] = new short[index.length];
        for(int i=0; i<index.length; ++i)
            values[i] = (short) index[i];

        return of(values, values.length);
    }

    public static EntryTable of(short[] ringIndex, int fromIndex, int toIndex) {

        int size = toIndex - fromIndex;
        if(size==0)
            return EMPTY;

        if(size==1)
            return SingleEntry.of(ringIndex[fromIndex]);

        ringIndex = Arrays.copyOfRange(ringIndex, fromIndex, toIndex);

        return IndexTable.of(ringIndex);
    }

    public static void main(final String... args) {

        int stat[] = new int[256];

        for (final RingEntry e : RingEntry.TABLE) {
            System.out.println(e.toString());
            if(e.isMin()) {
                int m = e.pmeq();
                ++stat[m];
            }
        }

        for(int i=0; i<256; i++) {
            int n = stat[i];
            if(n>0)
                System.out.format("%02x %d\n", i, n);
        }
    }
}
