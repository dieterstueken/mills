package mills.index1.partitions;

import mills.bits.PopCount;
import mills.ring.Entries;
import mills.ring.EntryTable;
import mills.util.ListSet;
import mills.util.Stat;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.function.UnaryOperator;

/**
 * version:     $Revision$
 * created by:  dst
 * created on:  05.07.12 13:54
 * modified by: $Author$
 * modified on: $Date$
 */

/**
 * Class PartitionTables provides a list of 100 PartitionTables for each PopCount.
 */
public class PartitionTables<T> extends AbstractMap<PopCount, PartitionTable<T>> {

    // pre calculated tables of entries for given PopCounts[00-99]
    private final List<PartitionTable<T>> tables;

    private PartitionTables(List<PartitionTable<T>> tables) {
        this.tables = tables;
        assert tables.size() == PopCount.TABLE.size();
    }

    private Entry<PopCount, PartitionTable<T>> entry(PopCount pop) {
        return new SimpleImmutableEntry<>(pop, tables.get(pop.index));
    }

    @Override
    public ListSet<Entry<PopCount, PartitionTable<T>>> entrySet() {
        return PopCount.TABLE.transform(this::entry);
    }

    @Override
    public Set<PopCount> keySet() {
        return PopCount.TABLE;
    }

    @Override
    public List<PartitionTable<T>> values() {
        return tables;
    }

    @Override
    public PartitionTable<T> get(Object key) {
        return key instanceof PopCount ? get((PopCount) key) : null;
    }

    /**
     * Return MskTable for a given pop count.
     *
     * @param pop count of the requested MskTable.
     * @return a MskTable for a given pop count.
     */
    public PartitionTable<T> get(int pop) {
        return tables.get(pop);
    }

    public PartitionTable<T> get(PopCount pop) {
        return tables.get(pop.getIndex());
    }

    /////////////////////////////////////////////////////////////////////////////////////

    public static PartitionTables<EntryTable> build() {
        return build(UnaryOperator.identity());
    }

    public static <T> PartitionTables<T> build(Function<EntryTable, T> generate) {
        return build(Entries.MINIMIZED, generate);
    }

    public static <T> PartitionTables<T> build(EntryTable root, Function<EntryTable, T> generate) {
        PartitionTable<T> empty = PartitionTable.empty(generate.apply(EntryTable.EMPTY));
        List<PartitionTable<T>> partitions = new ArrayList<>(PopCount.TABLE.size());
        for(int i=0; i<PopCount.TABLE.size(); ++i)
            partitions.add(empty);

        PopCount.TABLE.parallelStream()
                .filter(PopCount::singleRing)
                .forEach(pop -> partitions.set(pop.index, PartitionTable.build(root.filter(pop.eq), generate)));

        return new PartitionTables<>(partitions);
    }

    /////////////////////////////////////////////////////////////////////////////////////

    private static void dump(PartitionTables<EntryTable> pt) {

        System.out.println("partition tables");

        Stat stat = new Stat();

        int k = 0;

        for (int nb = 0; nb < 10; nb++) {
            for (int nw = 0; nw < 10; nw++) {
                final PopCount pop = PopCount.of(nb, nw);
                final PartitionTable<EntryTable> t = pt.get(pop.getIndex());
                int n = t.tables.size();
                int l = t.get(0).size();

                if(l>0)
                    System.out.format("%5d:%2d", l,n);

                k += t.tables.stream().filter(_t->_t.size()>1).count();
                stat.process(t.tables.stream());
            }
            System.out.println();
        }

        System.out.format("relevant tables: %d\n", k);

        stat.dump("total");
    }
    
    public static void main(String... args) {
        dump(PartitionTables.build());
    }
}
