package mills.index2;

import mills.bits.PopCount;
import mills.index.Partitions;
import mills.ring.EntryTable;
import mills.ring.EntryTables;
import mills.ring.RingEntry;
import mills.util.AbstractRandomList;
import mills.util.IndexTable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 12/25/14
 * Time: 11:05 AM
 */
abstract public class R0Table implements RingMap<EntryTable> {

    public static class Builder {

        final EntryTables tables;

        final List<RingEntry> l0 = new ArrayList<>();
        final List<RingEntry> l1 = new ArrayList<>();
        final List<Short> s1 = new ArrayList<>();

        public Builder(EntryTables tables) {
            this.tables = tables;
        }

        void process(RingEntry r1) {
            l1.add(r1);
        }

        void finish(RingEntry r0) {
            if(l1.isEmpty())
                return;

            final Short i1 = tables.index(l1);
            s1.add(i1);
            l0.add(r0);

            l1.clear();
        }

        public R0Table build() {
            assert l1.isEmpty();
            assert l1.size() == s1.size();
            if(l0.isEmpty())
                return EMPTY;

            if(l0.size()==1)
                return singleton(l0.get(0), tables.get(s1.get(0)));

            EntryTable t0 = EntryTable.of(l0);
            List<EntryTable> t1 = tables.build(s1);

            return R0Table.of(t0, t1);
        }

        public void reset() {
            l0.clear();
            l1.clear();
            s1.clear();
        }
    }

    static R0Table of(EntryTable t0, List<EntryTable> t1) {
        IndexTable index = IndexTable.sum(t1, Collection::size);
        return new R0Table() {

            @Override
            public int size() {
                return t0.size();
            }

            @Override
            public int index(int i) {
                return index.get(i);
            }

            @Override
            public RingEntry entry(int i) {
                return t0.get(i);
            }

            @Override
            public EntryTable get(int i) {
                return t1.get(i);
            }
        };
    }

    static R0Table singleton(RingEntry ringEntry, EntryTable table) {
        return new R0Table() {

            @Override
            public int size() {
                return 1;
            }

            @Override
            public int index(int i) {
                return table.size();
            }

            @Override
            public RingEntry entry(int i) {
                if(i!=0)
                    throw new IndexOutOfBoundsException("Index: "+i);
                return ringEntry;
            }

            @Override
            public EntryTable get(int i) {
                if(i!=0)
                    throw new IndexOutOfBoundsException("Index: "+i);
                return table;
            }
        };
    }

    static final R0Table EMPTY = new R0Table() {

        @Override
        public int size() {
            return 0;
        }

        @Override
        public int index(int i) {
            return 0;
        }

        @Override
        public RingEntry entry(int i) {
            throw new IndexOutOfBoundsException("empty");
        }

        @Override
        public EntryTable get(int i) {
            throw new IndexOutOfBoundsException("empty");
        }
    };

    static class Builders extends AbstractRandomList<R0Table> {

        final List<Builder> builders = new ArrayList<>(R2Tables.SIZE);
        final Partitions partitions;

        Builders(EntryTables tables, Partitions partitions) {
            this.partitions = partitions;

            for (PopCount clop : PopCount.CLOSED) {
                builders.add(new Builder(tables));
            }
        }

        void process(PopCount pop, RingEntry r2) {

            PopCount pop0 = pop.sub(r2.pop);

            EntryTable t0 = partitions.lePopTable.get(pop0);

            for (RingEntry r0 : t0) {
                // i2<=t0
                if (r0.index > r2.index)
                    break;

                PopCount pop1 = pop0.sub(r0.pop);
                int mlt = r2.mlt20s(r0);
                EntryTable t1 = partitions.partitions.get(pop1.index).get(mlt);

                RingEntry rad = r2.and(r0).radials();

                for (RingEntry r1 : t1) {
                    PopCount clop = r1.clop().add(rad.and(r1).pop);
                    builders.get(clop.index).process(r1);
                }

                for (Builder builder : builders) {
                    builder.finish(r0);
                }
            }
        }

        public void reset() {
            builders.forEach(Builder::reset);
        }

        @Override
        public R0Table get(int index) {
            return builders.get(index).build();
        }

        @Override
        public int size() {
            return R2Tables.SIZE;
        }
    }
}
