package mills.bits;

import mills.util.Indexed;
import mills.util.ListSet;

import java.util.List;

import static mills.bits.PopCount.NCLOPS;

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
        return pop().index* NCLOPS + clop().index;
    }

    static Clops _of(PopCount pop, PopCount clop) {
        return new Clops() {
            @Override
            public PopCount pop() {
                return pop;
            }

            @Override
            public PopCount clop() {
                return clop;
            }
        };
    }

    static Clops _of(int index) {
        PopCount pop = PopCount.get(index / NCLOPS);
        PopCount clop = PopCount.get(index % NCLOPS);
        return _of(pop, clop);
    }

    List<Clops> CLOPS = ListSet.generate(NCLOPS*PopCount.TABLE.size(), Clops::_of);

    static Clops get(int index) {
        return CLOPS.get(index);
    }

    static Clops get(PopCount pop, PopCount clop) {
        return get(pop.index* NCLOPS + clop.index);
    }
}
