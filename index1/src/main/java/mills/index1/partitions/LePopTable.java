package mills.index1.partitions;

import mills.bits.Player;
import mills.bits.PopCount;
import mills.ring.Entry;
import mills.ring.EntryTable;
import mills.util.AbstractRandomList;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.RecursiveTask;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 02.10.11
 * Time: 17:57
 */

/**
 * A virtual List of EntryTables.
 * Each entry is generated recursively on demand by calling get().
 * <p/>
 * Even if the list is immutable it should be copied in advance to speed up access.
 */
public class LePopTable extends AbstractRandomList<EntryTable> {

    public static LePopTable build() {
        final List<EntryTable> table = createTable();
        return new LePopTable(table);
    }

    private final List<EntryTable> table;

    public LePopTable(List<EntryTable> table) {
        this.table = table;
    }

    @Override
    public int size() {
        return PopCount.TABLE.size();
    }

    @Override
    public EntryTable get(int index) {
        // 8:8 and above: full table
        return index<table.size() ? table.get(index) : Entry.TABLE;
    }

    public EntryTable get(PopCount pop) {
        return pop==null ? null : get(pop.index);
    }

    private static List<EntryTable> createTable() {
        List<PopCount> p88 = PopCount.TABLE.subList(0, PopCount.P88.index);
        List<RecursiveTask<EntryTable>> tasks = new ArrayList<>(p88.size());

        p88.forEach(pop -> tasks.add(new RecursiveTask<>(){
            @Override
            protected EntryTable compute() {
                // increment the bigger one if < 9 else the other one
                Player p =  pop.nw<pop.nb && pop.nw<9 ? Player.White : Player.Black;
                int index = pop.add(p.pop).index;
                EntryTable upTable = index<tasks.size() ? tasks.get(index).join() : Entry.TABLE;
                EntryTable result = upTable.filter(e -> e.pop.le(pop));
                return result;
            }
        }));

        int i=tasks.size();
        while(i>0)
            tasks.get(--i).fork();

        return AbstractRandomList.transform(tasks, ForkJoinTask::join).copyOf();
    }

    ///////////////////////////////////////////////////////////////////////////

    private void dump() {

        System.out.println("leTable");

        for (int nb = 0; nb < 10; nb++) {
            for (int nw = 0; nw < 10; nw++) {
                final PopCount pop = PopCount.of(nb, nw);
                final EntryTable t = get(pop.index());
                System.out.format("%5d", t.size());
            }

            System.out.println();
        }
    }

    public static void main(String... args) throws InterruptedException, ExecutionException {
        build().dump();
    }

}
