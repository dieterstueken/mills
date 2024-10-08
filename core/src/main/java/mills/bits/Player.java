package mills.bits;

/**
* Created by IntelliJ IDEA.
* User: stueken
* Date: 07.09.2010
* Time: 18:17:23
*/

import mills.util.Indexed;
import mills.util.listset.DirectListSet;

import java.util.List;

/**
 * Class Player serves as a base of a ternary system
 * to map a sequence of Player objects to an integer value.
 */
public enum Player implements Indexed {

    None(0,0) {

        @Override
        public int count(PopCount pop) {
            return 8 - pop.nb() - pop.nw();
        }

        public boolean canJump(PopCount pop) {
            throw new IllegalArgumentException("can't jump");
        }

        @Override
        public Player opponent() {
            throw new IllegalStateException();
        }

        @Override
        public int stones(BW bw) {
            // void places: no white nor black
            return (bw.w.stones()|bw.b.stones()) ^ 0xff;
        }

    }, Black(1,0) {
        @Override
        public int count(PopCount pop) {
            return pop.nb();
        }

        @Override
        public Player opponent() {
            return White;
        }

        @Override
        public int stones(BW bw) {
            return bw.b.stones();
        }

    }, White(0,1){
        @Override
        public int count(PopCount pop) {
            return pop.nw();
        }

        @Override
        public Player opponent() {
            return Black;
        }

        @Override
        public int stones(BW bw) {
            return bw.w.stones();
        }
    };

    public int wgt() {
        return ordinal();
    }

    public char key() {
        return Character.toLowerCase(name().charAt(0));
    }

    abstract public int count(PopCount pop);

    public int mclop(PopCount pop) {
        return PopCount.mclop(count(pop));
    }

    public boolean canJump(PopCount pop) {
        return count(pop)<=3;
    }

    abstract public Player opponent();

    public Player and(Player other) {
        return other==this ? this : None;
    }

    public Player either(boolean swap) {
        return swap ? opponent() : this;
    }

    abstract public int stones(BW bw);

    @Override
    public int getIndex() {
        return ordinal();
    }

    Player(int nb, int nw) {
        this.pop = PopCount.get(nb,nw);
    }

    public final PopCount pop;

    public static final DirectListSet<Player> PLAYERS = DirectListSet.of(values());
    public static final List<Player> BW = List.of(Black, White);

    public static Player of(int i) {
        return PLAYERS.get(i);
    }
}
