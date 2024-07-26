package mills.score.attic.opening;

import mills.bits.Player;
import mills.bits.PopCount;
import mills.util.Indexed;
import mills.util.ListSet;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 09.11.19
 * Time: 17:35
 */

/**
 * Class Plop indexes an opening level[18]: 00,01,11, ... 88,89.
 */
public class Plop implements Indexed {

    public static final int COUNT = 18;

    public static final ListSet<Plop> LIST = ListSet.ofIndexed(COUNT, Plop::new);

    public static final Plop EMPTY = LIST.getFirst();

    protected final PopCount pop;

    private Plop(int level) {
        this.pop = PopCount.get(level/2, level-level/2);
    }

    public int getIndex() {
        return pop.sum();
    }

    public Player player() {
        return getIndex()%2==0 ? Player.White : Player.Black;
    }

    public Plop next() {
        int next = getIndex() + 1;
        return next<LIST.size()?LIST.get(next) : null;

    }

    @Override
    public String toString() {
        return String.format("%d: %s%s", getIndex(), pop, player().key());
    }
}
