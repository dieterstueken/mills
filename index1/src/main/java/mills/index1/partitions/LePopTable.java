package mills.index1.partitions;

import mills.bits.Player;
import mills.bits.PopCount;
import mills.ring.Entries;
import mills.ring.EntryTable;
import mills.util.AbstractRandomArray;
import mills.util.PopMap;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.RecursiveTask;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 02.10.11
 * Time: 17:57
 */

/**
 * A map of EntryTables filtered by pop count.
 */
public class LePopTable extends PopMap<EntryTable> {

    public static LePopTable build(EntryTable root) {
        final List<EntryTable> table = createTable(root);
        return new LePopTable(table);
    }

    public static LePopTable build() {
        return build(Entries.TABLE);
    }

    private LePopTable(List<EntryTable> table) {
        super(table);
    }

    @Override
    public int size() {
        return PopCount.TABLE.size();
    }

    public EntryTable get(PopCount pop) {
        return pop==null ? null : get(pop.index);
    }

    private static List<EntryTable> createTable(EntryTable root) {
        List<PopCount> p88 = PopCount.TABLE.subList(0, PopCount.P88.index);
        List<RecursiveTask<EntryTable>> tasks = new ArrayList<>(p88.size());

        // expanded virtual table for all pops
        AbstractRandomArray<EntryTable> tables = AbstractRandomArray.virtual(PopCount.TABLE.size(),
                pop -> pop<tasks.size() ? tasks.get(pop).join() : root);

        class Task extends RecursiveTask<EntryTable> {
            final PopCount pop;

            Task(PopCount pop) {
                this.pop = pop;
            }

            @Override
            protected EntryTable compute() {
                // increment the bigger one if < 9 else the other one
                Player p =  pop.nw<pop.nb && pop.nw<9 ? Player.White : Player.Black;
                int index = pop.add(p.pop).index;
                EntryTable upTable = tables.get(index);
                EntryTable result = upTable.filter(e -> e.pop.le(pop));
                return result;
            }
        }

        // prepare tasks to execute
        p88.stream().map(Task::new).forEach(tasks::add);

        // start bigger tables first
        int i=tasks.size();
        while(i>0)
            tasks.get(--i).fork();

        // materialize virtual table to ensure all tasks are joined
        return tables.copyOf();
    }

    ///////////////////////////////////////////////////////////////////////////

    private void dump() {

        System.out.println("leTable");

        for (int nb = 0; nb < 10; nb++) {
            for (int nw = 0; nw < 10; nw++) {
                final PopCount pop = PopCount.of(nb, nw);
                final EntryTable t = get(pop.getIndex());
                System.out.format("%5d", t.size());
            }

            System.out.println();
        }
    }

    public static void main(String... args) {
        build().dump();

        System.out.println();

        build(Entries.MINIMIZED).dump();
    }

}
