package mills.bits;

import mills.ring.RingEntry;
import mills.util.Indexed;
import mills.util.listset.DirectListSet;

/**
 * Class DRPop represents population counts for diagonal and radial positions.
 */
public class DRPop implements Indexed {

    // 5*6/2 pop counts with a sum()<=4
    public static int P44 = 5*6/2;

    final PopCount dia;

    final PopCount rad;

    public PopCount dia() {
        return dia;
    }

    public PopCount rad() {
        return rad;
    }

    public PopCount pop() {
        return dia.add(rad);
    }

    @Override
    public int getIndex() {
        return indexOf(dia.index, rad.index);
    }

    public int hashCode() {
        return getIndex();
    }

    public boolean eq(RingEntry entry) {
        return this.dia == entry.diaglonals().pop &&  this.rad == entry.radials().pop ;
    }

    public boolean ge(RingEntry entry) {
        return this.dia.ge(entry.diaglonals().pop) && this.rad.ge(entry.radials().pop);
    }

    public DRPop sub(DRPop other) {
        return other==null ? null : sub(other.dia, other.rad);
    }

    public DRPop sub(RingEntry entry) {
        return entry==null ? null : sub(entry.diaglonals().pop, entry.radials().pop);
    }

    public DRPop sub(PopCount dia, PopCount rad) {
        dia = this.dia.sub(dia);
        if(dia==null)
            return null;

        rad = this.rad.sub(rad);
        if(rad==null)
            return null;

        return of(dia, rad);
    }

    public static int indexOf(int dia, int rad) {
        if(Math.min(dia,rad)<0 || Math.max(dia,rad)>=P44)
            throw new IndexOutOfBoundsException("index out of range: [" + dia + ", " + rad + ']');
        return P44*dia + rad;
    }

    public static DRPop of(int index) {
       return TABLE.get(index);
    }

    public static DRPop of(PopCount dia, PopCount rad) {
        return of(indexOf(dia.index, rad.index));
    }

    public static DRPop of(int dia, int rad) {
        return of(indexOf(dia, rad));
    }

    public static DRPop of(RingEntry entry) {
        return of(entry.diaglonals().pop, entry.radials().pop);
    }

    private static DRPop create(int index) {
        PopCount dia = PopCount.get(index/P44);
        PopCount rad = PopCount.get(index%P44);
        return new DRPop(dia, rad);
    }

    private DRPop(PopCount dia, PopCount rad) {
        this.dia = dia;
        this.rad = rad;
    }

    public static int SIZE = P44*P44;

    public static final DirectListSet<DRPop> TABLE = DirectListSet.of(new DRPop[SIZE], DRPop::create);
}
