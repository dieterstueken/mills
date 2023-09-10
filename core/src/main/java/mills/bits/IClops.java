package mills.bits;

import mills.util.Indexed;

import java.util.Objects;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 09.11.19
 * Time: 17:46
 */
public interface IClops extends Indexed {

    PopCount pop();

    PopCount clop();

    default int getIndex() {
        return index(pop(), clop());
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

    /**
     * Canonicalize to real clops
     * @return a predefined Clop.
     */
    default Clops clops() {
        return Clops.of(getIndex());
    }

    static boolean equals(IClops a, IClops b) {
        return Objects.equals(a.pop(), b.pop()) && Objects.equals(a.clop(), b.clop());
    }

    static int index(PopCount pop, PopCount clop) {

        assert pop!=null && PopCount.P99.sub(pop) != null;
        int index = pop.index;

        if(clop!=null) {
            assert PopCount.P44.sub(clop)!=null;
            //assert pop.mclop().sub(clop)!=null;
            index += (clop.index + 1) * PopCount.SIZE;
        }

        return index;
    }
}
