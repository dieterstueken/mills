package mills.index.tables;

import mills.bits.Clops;
import mills.bits.PopCount;
import mills.ring.EntryTable;

import java.util.List;

abstract public class C2Table extends R2Table implements Clops {

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
}
