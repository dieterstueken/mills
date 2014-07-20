package mills.index;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import mills.bits.PopCount;
import mills.ring.EntryTable;
import mills.ring.RingEntry;
import mills.util.AbstractRandomList;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.RecursiveTask;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 7/19/14
 * Time: 5:14 PM
 */
public class ClopIndex extends AbstractRandomList<PosIndex> {

    final R2Table index;

    final List<R2Table> clopTable;

    public ClopIndex(R2Table index, List<R2Table> clopTable) {
        this.index = index;
        this.clopTable = clopTable;

        if(clopTable.size()!=25)
            throw new IllegalArgumentException("clop table size: " + clopTable.size());
    }

    @Override
    public int size() {
        return 25;
    }

    @Override
    public R2Table get(int index) {
        return clopTable.get(index);
    }

    public static class Builder extends RecursiveTask<ClopIndex> {

        final R2Table index;

        List<C2Builder> builders = AbstractRandomList.generate(25, this::builder);

        C2Builder builder(int index) {
            return new C2Builder() {
                @Override
                EntryTable table(List<RingEntry> list) {
                    return Builder.this.table(list);
                }
            };
        }

        public EntryTable table(List<RingEntry> list) {
            return EntryTable.of(list);
        }

        public Builder(R2Table index) {
            this.index = index;
        }

        public ClopIndex compute() {

            List<ForkJoinTask<List<R0Table>>> tasks = AbstractRandomList.generate(index.size(), this::task);

            Lists.reverse(tasks).forEach(ForkJoinTask::fork);

            for(int i=0; i<tasks.size(); ++i) {
                ForkJoinTask<List<R0Table>> task = tasks.get(i);
                List<R0Table> tables = task.join();
                RingEntry r2 = index.key(i);
                add(r2, tables);
            }

            PopCount pop = index.pop;

            List<R2Table> clopTable = AbstractRandomList.generate(25, i -> builders.get(i).build(pop));
            return new ClopIndex(index, clopTable);
        }

        private void add(RingEntry r2, List<R0Table> tables) {
            for(int i=0; i<25; ++i) {
                final R0Table table = tables.get(i);
                builders.get(i).add(r2, table);
            }
        }

        ForkJoinTask<List<R0Table>> task(int i) {

            return new RecursiveTask<List<R0Table>>() {

                @Override
                protected List<R0Table> compute() {
                    C0Builders b = new C0Builders() {

                        @Override
                        EntryTable table(List<RingEntry> list) {
                            return Builder.this.table(list);
                        }
                    };

                    RingEntry r2 = index.key(i);
                    R0Table t0 = index.value(i);

                    return ImmutableList.copyOf(b.build(r2, t0));
                }
            };
        }
    }

    abstract static class C2Builder {

        abstract EntryTable table(List<RingEntry> list);

        final List<RingEntry> entries = new ArrayList<>();
        final List<R0Table> tables = new ArrayList<>();

        void add(RingEntry entry, R0Table table) {
            if(!table.isEmpty()) {
                entries.add(entry);
                tables.add(table);
            }
        }

        R2Table build(PopCount pop) {
            return new R2Table(pop, table(entries), tables);
        }
    }
}
