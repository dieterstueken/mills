package mills.index;

import mills.bits.PopCount;
import mills.ring.EntryTable;
import mills.ring.RingEntry;

import java.util.Collection;
import java.util.List;
import java.util.function.LongUnaryOperator;

abstract public class C2Table extends R2Table {

    public final PopCount clop;

    public PopCount clop() {
        return clop;
    }

    public C2Table(PopCount pop, PopCount clop, EntryTable t2, List<R0Table> t0) {
        super(pop, t2, t0);
        this.clop = clop;
    }

    public static C2Table of(PopCount pop, PopCount clop, Collection<? extends RingEntry> t2, List<R0Table> t0, LongUnaryOperator normalize) {
        return new C2Table(pop, clop, EntryTable.of(t2), t0) {
            @Override
            public long normalize(long i201) {
                return normalize.applyAsLong(i201);
            }
        };
    }
}
