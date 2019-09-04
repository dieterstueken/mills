package mills.index2;

import mills.ring.EntryTable;
import mills.ring.RingEntry;
import mills.util.AbstractRandomList;
import mills.util.IndexTable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 12/26/14
 * Time: 6:05 PM
 */
public class R2Table implements RingMap<R0Table> {

    // full size containing R0Table.EMPTY
    final List<R0Table> t0;

    // partial list of non NULL entries
    final EntryTable t2;

    final IndexTable index;

    R2Table(List<RingEntry> t2, List<R0Table> t0) {
        assert t0.size()==RingEntry.MAX_INDEX;

        this.t2 = EntryTable.of(t2);
        this.t0 = List.copyOf(t0);
        //this.t2 = RingEntry.TABLE.filter(e -> !t0.get(e.index).isEmpty());
        index = IndexTable.sum(t2, e -> t0.get(e.index).range());
    }

    static final R2Table EMPTY = new R2Table(EntryTable.EMPTY, Collections.nCopies(RingEntry.MAX_INDEX, R0Table.EMPTY));

    @Override
    public int size() {
        return t2.size();
    }

    @Override
    public int index(int i) {
        return index.get(i);
    }

    @Override
    public RingEntry entry(int i) {
        return t2.get(i);
    }

    @Override
    public R0Table get(int i) {
        RingEntry e = entry(i);
        return t0.get(e.index);
    }

    static class Builder {
        final List<RingEntry> l2 = new ArrayList<>();
        final List<R0Table> l0 = new ArrayList<>(RingEntry.MAX_INDEX);

        Builder() {
            for(int i=0; i<RingEntry.MAX_INDEX; ++i)
                l0.add(R0Table.EMPTY);
        }

        public void process(RingEntry r2, R0Table t0) {
            assert !t0.isEmpty();

            l2.add(r2);
            l0.set(r2.index, t0);
        }

        public R2Table build() {
            return l2.isEmpty() ? EMPTY : new R2Table(l2, l0);
        }

        public void reset() {
            l2.clear();
            for(int i=0; i<RingEntry.MAX_INDEX; ++i)
                l0.set(i, R0Table.EMPTY);
        }
    }


    public static class Builders extends AbstractRandomList<R2Table> {

        final List<Builder> builders = new ArrayList<>();

        public Builders() {
            for(int i=0; i<R2Tables.SIZE; ++i) {
                builders.add(new Builder());
            }
        }

        public void reset() {
            builders.forEach(Builder::reset);
        }

        @Override
        public int size() {
            return R2Tables.SIZE;
        }

        @Override
        public R2Table get(int index) {
            return builders.get(index).build();
        }

        public void process(RingEntry r2, List<R0Table> l0) {
            for(int i=0; i<R2Tables.SIZE; ++i) {
                final R0Table t0 = l0.get(i);
                if(!t0.isEmpty()) {
                    final Builder builder = builders.get(i);
                    builder.process(r2, t0);
                }
            }
        }
    }
}
