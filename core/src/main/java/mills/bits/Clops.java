package mills.bits;

import mills.util.DirectListSet;
import mills.util.Indexed;

import java.util.Objects;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 09.11.19
 * Time: 17:46
 */
public interface Clops extends Indexed {

    PopCount pop();

    PopCount clop();

    default int getIndex() {
        return index(this);
    }

    default boolean isEqual(Clops other) {
        return Objects.equals(pop(), other.pop()) && Objects.equals(clop(), other.clop());
    }

    /**
     * If all stones belong to closed mills those may be broken.
     * @param player to analyze
     * @return number of closed mills that may be broken.
     */
    default int closed(Player player) {
        int np = player.count(pop());
        int nc = player.count(clop());

        if(np==3 && nc==1)
            return 1;

        if(np==5 && nc==2)
            return 2;

        if(np==7 && nc==3)
            return 3;

        if(np==8 && nc==4)
            return 4;

        return 0;
    }

    /**
     *
     * @param player to move
     * @return number of additionally closeable mills
     */
    default int closeables(Player player) {
        int np = player.count(pop());
        int nc = player.count(clop());

        if(nc==0) {
            if(np>=4)
                return 2;
            if(np>=2)
                return 1;
        } else if(nc==1) {
            if(np>=6)
                return 2;
            if(np>=4)
                return 1;
        } else if(nc==2) {
            if(np>=6)
                return 1;
        } else if(nc==3) {
            if(np>=7)
                return 1;
        }

        return 0;
    }

    static Clops of(int index) {
        return CLOPS.get(index);
    }

    static Clops of(PopCount pop, PopCount clop) {
        assert pop!=null && PopCount.P99.sub(pop) != null;
        assert clop == null || (PopCount.P44.sub(clop)!=null && pop.mclop().sub(clop)!=null);
        return of(index(pop, clop));
    }

    // canonicalize
    static Clops of(Clops clop) {
        return of(clop.pop(), clop.clop());
    }

    /////////////////////////////////////////////////////////

    int MCLOPS = PopCount.NCLOPS+1;
    int NCLOPS = PopCount.SIZE * MCLOPS;

    DirectListSet<Clops> CLOPS = DirectListSet.of(new Clops[NCLOPS], Clops::create);

    Clops EMPTY = of(PopCount.EMPTY, PopCount.EMPTY);

    private static Clops create(PopCount pop, PopCount clop) {
        return new Clops() {
            @Override
            public PopCount pop() {
                return pop;
            }

            @Override
            public PopCount clop() {
                return clop;
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
            public boolean equals(Object other) {
                return other instanceof Clops && isEqual((Clops)other);
            }
        };
    }

    static int index(Clops clops) {
        return index(clops.pop(), clops.clop());
    }

    static int index(PopCount pop, PopCount clop) {

        int index = pop.index;

        if(clop!=null)
            index += (clop.index+1) * PopCount.SIZE;

        return index;
    }

    private static Clops create(final int index) {
        PopCount pop = PopCount.get(index % PopCount.SIZE);

        int ci = index/PopCount.SIZE;
        PopCount clop = ci==0 ? null : PopCount.get(ci-1);
        Clops clops = create(pop, clop);

        if(clops.getIndex() != index)
            assert clops.getIndex() == index;

        return clops;
    }
}
