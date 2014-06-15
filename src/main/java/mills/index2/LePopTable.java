package mills.index2;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import mills.bits.Player;
import mills.bits.PopCount;
import mills.ring.EntryTable;
import mills.ring.RingEntry;
import mills.util.AbstractRandomList;

import java.util.ArrayList;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.RecursiveTask;
import java.util.function.Supplier;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 5/25/14
 * Time: 6:28 PM
 */
public class LePopTable extends AbstractRandomList<EntryTable> {

    public static LePopTable INSTANCE = new LePopTable();

    private final ArrayList<Supplier<EntryTable>> tables = new ArrayList<>(PopCount.SIZE);

    @Override
    public int size() {
        return PopCount.SIZE;
    }

    @Override
    public EntryTable get(int index) {
        return tables.get(index).get();
    }

    private LePopTable() {
        for (PopCount pop : PopCount.TABLE)
            tables.add(supplier(pop));
    }

    private void setup(int index, EntryTable table) {
        tables.set(index, () -> table);
    }

    private Supplier<EntryTable> supplier(PopCount pop) {

        if(pop.min()>=8) // not limited, both >= 8
            return ()->RingEntry.TABLE;

        if(pop.index==0)
            return ()->RingEntry.of(0).singleton;

        ForkJoinTask<EntryTable> task = task(pop.index);

        return task::join;
    }

    private ForkJoinTask<EntryTable> task(int index) {

        RecursiveTask<EntryTable> task = new RecursiveTask<EntryTable>() {
            @Override
            protected EntryTable compute() {
                PopCount pop = PopCount.get(index);
                Player player =  pop.nw<pop.nb && pop.nw<9 ? Player.White : Player.Black;
                PopCount pup = pop.add(player.pop);
                EntryTable tup = tables.get(pup.index).get();
                EntryTable result = tup.filter(ple(pop));
                setup(index, result);
                return result;
            }
        };

        return task.fork();
    }

    /**
     * Generate filter Predicate for elements with le pop count.
     * A RingEntry holds maximum 8 stones.
     *
     * @return a filter predicate.
     */
    public static Predicate<RingEntry> ple(PopCount pop) {

        if (pop.min() < 8)
            return e -> e != null && e.pop.le(pop);
        else
            return Predicates.alwaysTrue();
    }
}
