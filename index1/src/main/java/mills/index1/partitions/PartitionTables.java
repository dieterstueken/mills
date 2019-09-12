package mills.index1.partitions;

import mills.bits.PopCount;
import mills.ring.Entry;
import mills.ring.EntryTable;
import mills.util.AbstractRandomArray;
import mills.util.Stat;

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
public class PartitionTables extends AbstractRandomArray<PartitionTable> {

    // pre calculated tables of entries for given PopCounts[00-99]
    protected final PartitionTable[] partitions;

    private PartitionTables(PartitionTable[] partitions) {
        super(PopCount.TABLE.size());
        this.partitions = partitions;
        assert partitions.length == size();
    }

    /**
     * Return MskTable for a given pop count.
     *
     * @param pop count of the requested MskTable.
     * @return a MskTable for a given pop count.
     */
    public PartitionTable get(int pop) {
        return partitions[pop];
    }

    public PartitionTable get(PopCount pop) {
        return partitions[pop.index()];
    }

    /////////////////////////////////////////////////////////////////////////////////////

    public static PartitionTables build() {
        return build(Entry.MINIMIZED);
    }

    public static PartitionTables build(EntryTable root) {
        PartitionTable[] partitions = new PartitionTable[PopCount.TABLE.size()];

        PopCount.TABLE.parallelStream().forEach(pop -> partitions[pop.index] = PartitionTable.build(root.filter(pop.eq)));
        return new PartitionTables(partitions);
    }

    /////////////////////////////////////////////////////////////////////////////////////

    private void dump() {

        System.out.println("partition tables");

        Stat stat = new Stat();

        int k = 0;

        for (int nb = 0; nb < 10; nb++) {
            for (int nw = 0; nw < 10; nw++) {
                final PopCount pop = PopCount.of(nb, nw);
                final PartitionTable t = get(pop.index());
                int n = t.tables.size();
                int l = t.get(0).size();
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
        PopCount.CLOSED.stream()
                .peek(pt -> System.out.format("\nCLOP: %s", pt))
                .map(clop -> Entry.MINIMIZED.filter(e->e.clop().equals(clop)))
                .peek(root->System.out.format(" [%s]\n", root.size()))
                .map(PartitionTables::build)
                .forEach(PartitionTables::dump);
    }
}
