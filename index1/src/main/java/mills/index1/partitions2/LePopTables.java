package mills.index1.partitions2;

import mills.bits.Player;
import mills.bits.PopCount;
import mills.ring.Entry;
import mills.ring.EntryTable;
import mills.ring.RingEntry;
import mills.util.AbstractRandomList;

import java.util.*;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.RecursiveAction;
import java.util.concurrent.RecursiveTask;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * version:     $Revision$
 * created by:  dst
 * created on:  24.07.2014 10:08
 * modified by: $Author$
 * modified on: $Date$
 */
public class LePopTables {

    public static final List<PopCount> CLOP_TABLE = PopCount.CLOSED;

    final List<LeClopTable> tables;

    public EntryTable get(PopCount pop) {
        return tables.get(pop.index).root;
    }

    public EntryTable get(PopCount pop, PopCount clop, int count) {
        return tables.get(pop.index).tables.get(clop.index).get(count);
    }

    public EntryTable get(PopCount pop, PopCount clop) {
        return get(pop, clop, 0);
    }

    public LePopTables(List<LeClopTable> tables) {
        this.tables = tables;
        assert tables.size() == 100;
    }

    ///////////////////////////////////////////////////////////

    public static ForkJoinTask<LePopTables> task() {
        return new RecursiveTask<LePopTables>() {

            final LeClopTable clops[] = new LeClopTable[100];

            @Override
            protected LePopTables compute() {

                createLeClopTable(PopCount.of(8, 8), Entry.TABLE);

                // 8:0 ... 8:7 and 0:8 ... 7:8
                for (int i = 7; i >= 0; --i) {
                    invokeAll(
                            task(i, 8, Player.Black),
                            task(8, i, Player.White));
                }

                // copy all 9:x ... x:9 from 8:x ... x:8
                for (PopCount pop : PopCount.TABLE.subList(9 * 9, 100)) {
                    LeClopTable table = clops[pop.truncate(8).index];
                    assert table != null;
                    clops[pop.index] = table;
                }

                final List<ForkJoinTask<?>> tasks = new ArrayList<>(20);

                // generate all all <= 7:7
                for (int i = 7; i > 0; --i) {

                    for (int j = 0; j < i; j++) {
                        tasks.add(task(i, j, Player.Black));
                        tasks.add(task(j, i, Player.White));
                    }

                    tasks.add(task(i, i, Player.Black));

                    invokeAll(tasks);
                    tasks.clear();
                }

                createLeClopTable(PopCount.EMPTY, Entry.EMPTY.singleton);

                return new LePopTables(List.of(clops));
            }

            ForkJoinTask<?> task(int nb, int nw, Player p) {

                PopCount pop = PopCount.of(nb, nw);
                PopCount pup = pop.add(p.pop);

                // lookup already calculated table
                LeClopTable clop = clops[pup.index];
                assert clop != null;

                return task(pop, clop.root);
            }

            ForkJoinTask<?> task(PopCount pop, EntryTable root) {

                return new RecursiveAction() {

                    @Override
                    protected void compute() {
                        createLeClopTable(pop, root.filter(e -> e.pop.le(pop)));
                    }
                };
            }

            LeClopTable createLeClopTable(PopCount pop, EntryTable root) {

                assert clops[pop.index] == null;

                return new Supplier<LeClopTable>() {

                    final EntryTable tables[] = new EntryTable[25];

                    @Override
                    public LeClopTable get() {
                        assert clops[pop.index] == null;
                        final LeClopTable result = new LeClopTable(pop, root, tables);
                        clops[pop.index] = result;
                        return result;
                    }

                    EntryTable parent(PopCount clop) {
                        if (clop.min() == 4)
                            return root;

                        Player inc = clop.nb < clop.nw || clop.nb < 4 ? Player.Black : Player.White;
                        PopCount up = clop.add(inc.pop);

                        assert up.max() <= 4;

                        EntryTable parent = tables[up.index];

                        assert parent != null;

                        return parent;
                    }

                    void build(int i, int j) {
                        PopCount clop = PopCount.of(i, j);

                        if (tables[clop.index] == null) {
                            final EntryTable filtered = parent(clop).filter(e -> e.clop().le(clop));
                            tables[clop.index] = filtered;
                        }
                    }

                    {
                        PopCount mclop = pop.mclop();

                        // preset all clops with no restrictions to root
                        for (PopCount clop : CLOP_TABLE) {
                            if (mclop.le(clop)) {
                                tables[clop.index] = root;
                            }
                        }

                        // filter any remaining up-down
                        for (int i = 4; i>=0; --i) {
                            build(i, i);

                            for (int j = i-1; j>=0; --j) {
                                build(i, j);
                                build(j, i);
                            }
                        }
                    }
                }.get();
            }
        };
    }

    static final Predicate<RingEntry> F1 = new Predicate<RingEntry>() {
        @Override
        public boolean test(RingEntry e) {
            return e.pop().sum()<=8 && e.clop().sum()<4;
        }
    };

    static class LeClopTable {

        final PopCount pop;

        final EntryTable root;

        final List<LeCountTable> tables;

        LeClopTable(final PopCount pop, EntryTable root, EntryTable tables[]) {
            this.pop = pop;
            this.root = root;

            this.tables = List.copyOf(AbstractRandomList.transform(Arrays.asList(tables), LePopTables::lct));
        }
    }

    static class LeCountTable {

        final List<EntryTable> tables;

        LeCountTable(List<EntryTable> tables) {
           this.tables = tables;
        }

        EntryTable get(int count) {
            return count<tables.size() ? tables.get(count) : EntryTable.EMPTY;
        }
    }

    static final LeCountTable LC_EMPTY = new LeCountTable(Collections.emptyList());

    static LeCountTable lct(EntryTable root) {
        if(root.isEmpty())
            return LC_EMPTY;

        List<EntryTable> tables = new ArrayList<>();

        while(!root.isEmpty()) {
            tables.add(root);
            int count = tables.size();
            root = root.filter(e->e.pop.sum()>count);
        }

        tables = List.copyOf(tables);

        return new LeCountTable(tables);
    }

    public static void main(String... args) {
        LePopTables lpt = LePopTables.task().invoke();

        Set<EntryTable> tables = new TreeSet<>(EntryTable.BY_ORDER);

        for (LeClopTable lc : lpt.tables) {
            for (LeCountTable ln : lc.tables) {
                for (EntryTable t : ln.tables) {
                    tables.add(t);
                }
            }
        }

        System.out.format("%d\n", tables.size());

    }
}
