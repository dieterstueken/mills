package mills.bits;

import mills.util.listset.DirectListSet;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 10.09.23
 * Time: 12:54
 */
public class Clops implements IClops {

    final int index;

    final PopCount pop;

    final PopCount clop;

    private Clops(int index) {
        this.index = index;
        this.pop = PopCount.get(index % PopCount.SIZE);
        int ci = index/PopCount.SIZE;
        this.clop = ci==0 ? null : PopCount.get(ci-1);

        if(IClops.index(pop, clop) != index)
            throw new AssertionError("invalid index");
    }
    @Override
    public PopCount pop() {
        return pop;
    }

    @Override
    public PopCount clop() {
        return clop;
    }

    @Override
    public Clops clops() {
        return this;
    }

    public int getIndex() {
        return index;
    }

    @Override
    public String toString() {
        if(clop!=null)
            return String.format("%s-%s", pop, clop);
        else
            return String.format("%s", pop);
    }

    @Override
    public int hashCode() {
        return getIndex();
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof IClops other && IClops.equals(this, other);
    }

    public static Clops of(int index) {
        return Clops.CLOPS.get(index);
    }

    public static Clops of(PopCount pop, PopCount clop) {
        return of(IClops.index(pop, clop));
    }
    public static final int MCLOPS = PopCount.NCLOPS+1;

    public static final int NCLOPS = PopCount.SIZE * MCLOPS;

    public static final DirectListSet<Clops> CLOPS = DirectListSet.of(new Clops[NCLOPS], Clops::new);

    public static final Clops EMPTY = CLOPS.getFirst();
}
