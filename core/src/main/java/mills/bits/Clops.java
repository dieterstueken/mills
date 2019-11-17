package mills.bits;

import mills.util.Indexed;
import mills.util.ListSet;

import java.util.List;

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
        return index(pop(), clop());
    }

    static Clops get(int index) {
        return CLOPS.get(index);
    }

    static Clops get(PopCount pop, PopCount clop) {
        assert pop!=null && PopCount.P99.sub(pop) != null;
        assert clop == null || PopCount.P44.sub(clop)!=null;
        return get(index(pop, clop));
    }

    // canonicalize
    static Clops get(Clops clop) {
        return get(clop.pop(), clop.clop());
    }

    /////////////////////////////////////////////////////////

    int MCLOPS = PopCount.NCLOPS+1;
    int NCLOPS = PopCount.SIZE * MCLOPS;

    List<Clops> CLOPS = ListSet.generate(NCLOPS, Clops::_of);

    Clops EMPTY = get(PopCount.EMPTY, PopCount.EMPTY);

    private static Clops _of(PopCount pop, PopCount clop) {
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
                return String.format("%s[%s]", pop, clop);
            }
        };
    }

    private static int index(PopCount pop, PopCount clop) {

        int index = MCLOPS * pop.index;

        if(clop!=null)
            index += clop.index+1;

        return index;
    }

    private static Clops _of(final int index) {
        PopCount pop = PopCount.get(index / MCLOPS);

        int ci = index%MCLOPS;
        PopCount clop = ci==0 ? null : PopCount.get(ci-1);
        Clops clops = _of(pop, clop);

        if(clops.getIndex() != index)
            assert clops.getIndex() == index;

        return clops;
    }
}
