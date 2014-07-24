package mills.partitions;

import com.google.common.collect.ImmutableList;
import mills.bits.Player;
import mills.bits.PopCount;
import mills.ring.EntryTable;
import mills.ring.RingEntry;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
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

    public static final List<PopCount> CLOP_TABLE = PopCount.TABLE.subList(0, 25);

    final List<LeClopTable> tables;

    public EntryTable get(PopCount pop) {
        return tables.get(pop.index).root;
    }

    public EntryTable get(PopCount pop, PopCount clop) {
        return tables.get(pop.index).tables.get(clop.index);
    }

    public EntryTable get1(PopCount pop, PopCount clop) {
        return tables.get(pop.index).tables1.get(clop.index);
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

                createLeClopTable(PopCount.of(8, 8), RingEntry.TABLE);

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

                createLeClopTable(PopCount.EMPTY, RingEntry.of(0).singleton);

                return new LePopTables(ImmutableList.copyOf(clops));
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
                        assert result.verify();
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

        final List<EntryTable> tables;

        final List<EntryTable> tables1;

        LeClopTable(final PopCount pop, EntryTable root, EntryTable tables[]) {
            this.pop = pop;
            this.root = root;
            this.tables = Arrays.asList(tables);

            this.tables1 = new ArrayList<>(25);

            for (EntryTable t : tables) {
                EntryTable t1 = t.filter(F1);
                tables1.add(t1);
            }

            assert tables.length == 25;
        }

        boolean verify(PopCount clop) {
            EntryTable table = tables.get(clop.index);

            assert table!=null;

            EntryTable ref = RingEntry.TABLE.filter(e->e.pop.le(pop) && e.clop().le(clop));
            return table.equals(ref);
        }

        boolean verify() {
            for (PopCount clop : CLOP_TABLE) {
                if(!verify(clop))
                    return false;
            }

            return true;
        }
    }

    public boolean verify() {

        for (LeClopTable table : tables) {
            if(!table.verify())
                return false;
        }

        return true;
    }

    public static void main(String... args) {
        LePopTables lpt = LePopTables.task().invoke();
        System.out.println(lpt.verify());
    }
}
