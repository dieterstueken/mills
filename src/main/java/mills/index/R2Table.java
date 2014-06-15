package mills.index;

import com.google.common.collect.ImmutableList;
import mills.bits.PopCount;
import mills.position.Positions;
import mills.ring.RingEntry;
import mills.util.Indexer;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 07.07.12
 * Time: 15:01
 */
public class R2Table implements PosIndex {

    final PopCount pop;

    // full table
    final List<R2Entry> table;

    // partial table
    final List<R2Entry> entries;

    protected R2Table(final PopCount pop, final List<R2Entry> table, final List<R2Entry> entries) {
        this.pop = pop;
        this.table = table;
        this.entries = entries;
    }

    public final PopCount pop() {
        return pop;
    }

    public List<R2Entry> entries() {
        return entries;
    }

    public int size() {
        if(entries.isEmpty())
            return 0;

        int tail = entries.size()-1;
        return entries.get(tail).size();
    }

    private boolean verify(long i201) {
        PopCount p = Positions.pop(i201);
        if(p==pop)
            return true;

        return false;
    }

    public int posIndex(long i201) {

        assert verify(i201) : Positions.position(i201);

        final long n201 = Positions.normalize(i201);
        final short i2 = Positions.i2(n201);
        final R2Entry entry = table.get(i2);
        return entry.posIndex(n201);
    }

    @Deprecated // use posIndex instead
    public int findIndex(long i201) {
        final long n201 = Positions.normalize(i201);

        final short i2 = Positions.i2(n201);
        int pos = R2.binarySearchKey(entries, i2);

        if (pos < 0) {
            // if missing return lower bound by negative index
            return -entries.get(-2 - pos).index;
        }

        return entries.get(pos).posIndex(i201);
    }

    public long i201(int posIndex) {

        final int pos = INDEX.lowerBound(entries, posIndex);

        // may throw IndexOutOfBoundsException
        final R2Entry entry = entries.get(pos);

        return entry.i201(posIndex);
    }

    public IndexProcessor process(IndexProcessor receiver) {
        return process(receiver, 0, Integer.MAX_VALUE);
    }

    public IndexProcessor process(IndexProcessor processor, int start, int end) {
        final int i0 = start>0 ? INDEX.lowerBound(entries, start) : 0;

        for(int i=i0; i<entries.size(); ++i) {
            final R2Entry entry = entries.get(i);
            if(!entry.process(processor, start, end))
                break;
        }

        return processor;
    }

    static final Indexer<R2Entry> INDEX = new Indexer<R2Entry>() {

        @Override
        public int index(R2Entry element) {
            return element.index;
        }
    };

    static final Indexer<R2Entry> R2 = new Indexer<R2Entry>() {

        @Override
        public int index(R2Entry element) {
            return element.i2;
        }
    };

    public static R2Table of(final PopCount pop, final List<R0Table> list) {

        assert list.size()==RingEntry.MAX_INDEX;

        int index = 0;
        List<R2Entry> table = new ArrayList<>(RingEntry.MAX_INDEX);
        List<R2Entry> entries = new ArrayList<>(RingEntry.MAX_INDEX);

        for (short i2 = 0; i2 < RingEntry.MAX_INDEX; i2++) {
            R0Table r0t = list.get(i2);
            final R2Entry entry = new R2Entry(index, i2, r0t);
            int l = r0t.size();
            index += l;
            if(l>0)
                entries.add(entry);
            table.add(entry);
        }

        table = ImmutableList.copyOf(table);
        entries = entries.size()==table.size() ?  table  : ImmutableList.copyOf(entries);

        /*
        if(pop.nb<=3 && pop.nw()<=3)
            return new R2Table(pop, table, entries) {

                @Override
                public long normalize(long i201) {
                    return Positions.normalize3(i201);
                }
            };
        */

        return new R2Table(pop, table, entries);
    }
}
