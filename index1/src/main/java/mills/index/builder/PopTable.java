package mills.index.builder;

import mills.bits.Player;
import mills.bits.PopCount;
import mills.ring.Entries;
import mills.ring.EntryTable;
import mills.util.AbstractRandomArray;
import mills.util.ArraySet;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.RecursiveTask;
import java.util.function.UnaryOperator;

import static mills.bits.PopCount.P88;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 02.10.11
 * Time: 17:57
 */

/**
 * A map of EntryTables filtered by pop count.
 * Each entry provides a filtered table of entries with equal or lesser population.
 * Since population is limited to 8 the tables are repeated for n>8.
 */
public class PopTable {

    /**
     * Build from a subset root.
     * @param root of entries distribute
     * @return LePopTable of distributed entries.
     */
    public static PopTable build(EntryTable root, UnaryOperator<EntryTable> normalize) {
        final List<EntryTable> table = createTable(root, normalize);
        return new PopTable(table);
    }

    public static ForkJoinTask<PopTable> fork(EntryTable root, UnaryOperator<EntryTable> normalize) {
        return ForkJoinTask.adapt(() -> PopTable.build(root, normalize)).fork();
    }

    // specialisation shortcut
    public static <T> Map<PopCount, T> mapOf(List<T> values, T defaultValue) {
        return ArraySet.mapOf(PopCount::get, values, defaultValue);
    }

    public static PopTable build(EntryTable root) {
        return build(root, UnaryOperator.identity());
    }

    public static PopTable build() {
        return build(Entries.TABLE);
    }

    private final Map<PopCount, EntryTable> tables;

    private PopTable(List<EntryTable> table) {
        this.tables = mapOf(table, EntryTable.EMPTY);
    }

    public int size() {
        return PopCount.TABLE.size();
    }

    public EntryTable get(PopCount pop) {
        return pop==null ? null : tables.get(pop);
    }

    private static List<EntryTable> createTable(EntryTable root, UnaryOperator<EntryTable> normalize) {
        List<PopCount> p88 = PopCount.TABLE.subList(0, P88.index);
        List<RecursiveTask<EntryTable>> tasks = new ArrayList<>(P88.index);

        // expanded virtual table for all pops(9,9)
        AbstractRandomArray<EntryTable> tables = AbstractRandomArray.virtual(PopCount.TABLE.size(), index -> {
            // clip down
            if(index>P88.index)
                index = PopCount.get(index).min(P88).index;

            // p88 itself was excluded and is virtual
            if(index==P88.index)
                return root;

            return tasks.get(index).join();
        });

        class Task extends RecursiveTask<EntryTable> {
            private final PopCount pop;

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
                return normalize.apply(result);
            }
        }

        // prepare tasks to execute
        p88.stream().map(Task::new).forEach(tasks::add);

        // start with bigger tables first
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
                final EntryTable t = get(pop);
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
