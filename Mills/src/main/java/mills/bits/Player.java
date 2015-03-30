package mills.bits;

/**
* Created by IntelliJ IDEA.
* User: stueken
* Date: 07.09.2010
* Time: 18:17:23
*/

/**
 * Class Player serves as a base of a ternary system
 * to map a sequence of Player objects to an integer value.
 */
public enum Player {

    None(PopCount.of(0,0)) {

        @Override
        public int count(PopCount pop) {
            return 24 - pop.nb() - pop.nw();
        }

        @Override
        public Player other() {
            throw new IllegalStateException();
        }

        @Override
        public int stones(BW bw) {
            // void places: no white nor black
            return (bw.w.stones()|bw.b.stones()) ^ 0xff;
        }

    }, Black(PopCount.of(1,0)) {
        @Override
        public int count(PopCount pop) {
            return pop.nb();
        }

        @Override
        public Player other() {
            return White;
        }

        @Override
        public int stones(BW bw) {
            return bw.b.stones();
        }

    }, White(PopCount.of(0,1)){
        @Override
        public int count(PopCount pop) {
            return pop.nw();
        }

        @Override
        public Player other() {
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

    abstract public int count(PopCount pop);

    abstract public Player other();

    public Player and(Player other) {
        return other==this ? this : None;
    }

    public Player other(boolean swap) {
        return swap ? other() : this;
    }

    abstract public int stones(BW bw);

    private Player(PopCount pop) {
        this.pop = pop;
    }

    public final PopCount pop;

    private static final Player players[] = values();
    
    public static Player of(int i) {
        return players[i];
    }
}
