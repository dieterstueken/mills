package mills.ring;

import java.util.Arrays;

public class RingArray extends AbstractEntryTable implements DirectTable {

    protected final RingEntry[] entries;

    final int size;
    private int hashCode = 0;

    @Override
    public int hashCode() {
        if(hashCode==0) {
            hashCode = Arrays.hashCode(entries);
            if(hashCode==0)
                throw new IllegalStateException("unexpected hash code");
        }
        return hashCode;
    }

    protected RingArray(RingEntry[] entries) {
        this(entries, entries.length);
    }

    protected RingArray(RingEntry[] entries, int size) {
        this.entries = entries;
        this.size = size;
        if(entries.length < size)
            throw new IndexOutOfBoundsException("array too small");
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public RingEntry get(int index) {
        return entries[index];
    }

    @Override
    public EmptyEntry getFirst() {
        return (EmptyEntry) this.get(0);
    }

    @Override
    public DirectTable headSet(final RingEntry toElement) {
        return headSet(Math.min(toElement.index, size));
    }

    @Override
    public DirectTable headSet(final int size) {
        if(size==size())
            return this;

        if(size==0)
            return EntryTable.empty();

        if(size==1)
            return getFirst().singleton();

        return new RingArray(entries, size);
    }

    @Override
    public int findIndex(final int ringIndex) {
        if(ringIndex<0)
            return -1;

        if(ringIndex>=size) // insert position is
            return -size-1;

        return ringIndex;
    }

    @Override
    public int indexOf(final RingEntry entry) {
        return findIndex(entry.index);
    }

    @Override
    public short ringIndex(final int index) {
        return super.ringIndex(index);
    }

    @Override
    public EntryTable subSet(final int fromIndex, final int size) {
        if(fromIndex==0)
            return headSet(size);
        else
            return super.subSet(fromIndex, size);
    }
}
