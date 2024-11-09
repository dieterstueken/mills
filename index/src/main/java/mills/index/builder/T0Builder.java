package mills.index.builder;

import mills.bits.PopCount;
import mills.index.tables.R0Table;
import mills.position.Positions;
import mills.ring.Entries;
import mills.ring.EntryTable;
import mills.ring.IndexedEntryTable;
import mills.ring.RingEntry;
import mills.util.AbstractRandomArray;
import mills.util.AbstractRandomList;
import mills.util.ConcurrentCompleter;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CountedCompleter;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 03.09.22
 * Time: 15:55
 */
class T0Builder extends ConcurrentCompleter {

    // M < 2^31 / 25 / 6561 = 13092
    static final int M = 12 * 1024;

    final GroupBuilder group;

    int[] table;

    int size = 0;

    T0Builder(CountedCompleter<?> parent, AtomicInteger todo, GroupBuilder group) {
        super(parent, todo);
        this.group = group;
    }

    T0Builder(GroupBuilder group) {
        this(null, new AtomicInteger(group.t2.size()), group);
    }

    @Override
    protected ConcurrentCompleter newSubtask() {
        return new T0Builder(this, todo, group);
    }

    @Override
    public void onCompletion(final CountedCompleter<?> caller) {
        super.onCompletion(caller);
    }

    protected void compute(int i) {
        build(group.t2.get(i));
    }

    short getKey(int index) {
        int pattern = table[index];
        return (short) (pattern % M);
    }

    short getEntryIndex(int index) {
        int pattern = table[index];
        int key = (pattern / M) % RingEntry.MAX_INDEX;
        return (short) key;
    }

    RingEntry getEntry(int index) {
        int i1 = getEntryIndex(index);
        return Entries.entry(i1);
    }

    int getClopIndex(int index) {
        int pattern = table[index];
        if (pattern < 0)
            return -1;
        return (pattern / M) / RingEntry.MAX_INDEX;
    }

    void setup(int n0) {
        size = 0;
        //if(table==null) {
        //    PopCount mclop = pop.mclop();
        //    n0 *= mclop.nb * mclop.nw + 1;
        //    n0 = Math.max(n0, 1024);
        //    table = new int[n0];
        //}
    }

    void addIndex(int index) {
        if (table == null)
            table = new int[1024];
        else if (size >= table.length)
            table = Arrays.copyOf(table, table.length * 2);

        table[size++] = index;
    }

    /**
     * Build a compact index from the given values.
     * @param clop pop count of closings
     * @param r0 Ring Entry
     * @param i1 index of
     */
    int buildIndex(PopCount clop, RingEntry r0, int i1) {
        if (i1 >= M)
            throw new IndexOutOfBoundsException("IndexedEntryTable to big");

        i1 += (r0.index + RingEntry.MAX_INDEX * clop.index) * M;
        if (i1 < 0)
            throw new IndexOutOfBoundsException("IndexedEntryTable negative index");

        return i1;
    }

    void build(RingEntry r2) {

        PopCount pop0 = group.pop.sub(r2.pop);

        if (pop0.sum() > 16)
            return; // won't fit into two rings

        EntryTable t0 = group.partitions.lePops.get(pop0).tailSet(r2);

        if (t0.isEmpty())
            return;

        setup(t0.size());

        for (RingEntry r0 : t0) {

            // debug(r0, r2);

            PopCount pop1 = pop0.sub(r0.pop);
            if(pop1.sum()>8)
                continue;

            final Partition partition = group.partitions.get(pop1);

            if (partition.isEmpty())
                continue;

            int meq2 = Positions.meq(r2, r0);
            if (meq2 == 0)
                continue;

            RingEntry rad = r2.radials().and(r0);
            PopCount clop20 = r2.clop().add(r0.clop());

            final List<IndexedEntryTable> t1tables = partition.getFragments(meq2).get(rad);
            for (IndexedEntryTable t1 : t1tables) {
                PopCount clop = t1.getFirst().clop(rad).add(clop20);

                // to eliminate r201 permutations when jumping the whole array it limited.
                RingEntry limit = group.limit(r2, r0);
                if(limit!=null) {
                    EntryTable l1 = t1.tailSet(limit);
                    if(l1.isEmpty())
                        continue;
                    // truncate the table, possibly by allocating a new IndexedEntryTable.
                    t1 = partition.tables.getTable(l1);
                }

                // collect all fragments of different clop and r0
                int index = buildIndex(clop, r0, t1.getIndex());
                addIndex(index);
            }
        }

        if (size == 0)
            return;

        if (size > 1)
            Arrays.sort(table, 0, size);

        addIndex(-1); // terminal dummy index

        // group indexes by increasing clop count starting with first index.
        int offt = 0;
        int clop = getClopIndex(0);

        for (int i = 1; i < size; ++i) {
            int iclop = getClopIndex(i);
            if (iclop != clop) {
                // entering next group of clops, submit current group to the associated builder.
                C2Builder c2Builder = group.builders.getOf(clop);
                if (c2Builder != null) {
                    R0Table r0t = build(pop0, offt, i - offt);
                    c2Builder.put(r2, r0t);
                }
                offt = i;
                clop = iclop;
            }
        }
    }

    private R0Table<IndexedEntryTable> build(final PopCount pop0, final int offt, final int size) {

        if (size == 0)
            return R0Table.emptyTable();

        if (size == 1) {
            RingEntry r0 = getEntry(offt);
            int key = getKey(offt);
            PopCount pop1 = pop0.sub(r0.pop);
            IndexedEntryTable t1 = group.partitions.get(pop1).tables.get(key);
            return R0Table.of(r0.singleton(), List.of(t1));
        }

        List<RingEntry> tmp = AbstractRandomList.virtual(size, i -> getEntry(i+offt));
        EntryTable t0 = EntryTable.of(tmp);

        short[] s1 = new short[size];
        for (int index = 0; index < size; ++index) {
            s1[index] = getKey(index + offt);
         }

         List<IndexedEntryTable> t1 = new AbstractRandomArray<>(size) {
             @Override
             public IndexedEntryTable get(int index) {
                 RingEntry r0 = t0.get(index);
                 PopCount pop1 = pop0.sub(r0.pop);
                 int key = s1[index];
                 return group.partitions.get(pop1).tables.get(key);
             }
         };

        // materialize small lists
        if(size<16) {
            t1 = List.copyOf(t1);
        }

        return R0Table.of(t0, t1);
    }
}
