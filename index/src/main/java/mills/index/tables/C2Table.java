package mills.index.tables;

import mills.bits.Clops;
import mills.bits.PopCount;
import mills.ring.EntryMap;
import mills.ring.EntryTable;
import mills.ring.RingEntry;

import java.util.Collection;
import java.util.List;

public class C2Table extends R2Table implements Clops {

    public final PopCount clop;

    public PopCount clop() {
        return clop;
    }

    @Override
    public int hashCode() {
        return getIndex();
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof Clops && isEqual((Clops)other);
    }

    public C2Table(PopCount pop, PopCount clop, EntryTable t2, List<R0Table> t0) {
        super(pop, t2, t0);
        this.clop = clop;
    }

    public static C2Table of(PopCount pop, PopCount clop, Collection<? extends RingEntry> t2, List<R0Table> t0) {
        return new C2Table(pop, clop, EntryTable.of(t2), t0);
    }

    public static C2Table of(PopCount pop, PopCount clop, EntryMap<R0Table> t0m) {
        return of(pop, clop, t0m.keySet(), t0m.values());
    }
}
