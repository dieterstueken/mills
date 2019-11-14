package mills.score.opening;

import mills.bits.Player;
import mills.bits.PopCount;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 09.11.19
 * Time: 17:35
 */
public class Plop {

    public static final int COUNT = 18;

    protected final PopCount plop;

    public Plop(int level) {
        this.plop = PopCount.get(level/2, level-level/2);
    }

    public Plop(Plop other) {
        this.plop = other.plop;
    }

    public int level() {
        return plop.sum();
    }

    public Player player() {
        return level()%2==0 ? Player.White : Player.Black;
    }

    @Override
    public String toString() {
        return String.format("(%d)%s%s", level(), plop, player().key());
    }
}
