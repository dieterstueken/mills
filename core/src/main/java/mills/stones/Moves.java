package mills.stones;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 30.12.11
 * Time: 14:30
 */

import mills.bits.Sector;

/**
 * Class Moves executes move operations based on a move table.
 * Each move is propagated to a Move.Processor.
 * This class is not thread safe.
 */
abstract public class Moves {

    public static final boolean ABORT = true;

    public interface Process {
        boolean process(int stay, int move, int mask);
    }

    public static final Moves JUMP = new Moves(jumps()) {
        public String toString() {
            return "JUMP";
        }
    };

    public static final Moves MOVE = new Moves(moves()) {
        public String toString() {
            return "MOVE";
        }
    };

    public static final Moves TAKE = new Moves(takes()) {
        public String toString() {
            return "TAKE";
        }

        public int move(int stay, int move, int mask, Process target) {

            if(mask==0)
                return 0;

            int j;
            int n=0;
            int m = 1<<24;

            while(mask!=0) {
                m >>= 1;

                if(mask<=(j=m>>>8))
                    m = j;

                if(mask<=(j=m>>>4))
                    m = j;

                if(mask<=(j=m>>>2))
                    m = j;

                if((m&mask)!=0) {
                    // clear bit
                    mask ^= m;

                    ++n;
                    // target may abort further processing by returning true
                    if(target.process(stay, move, m) == ABORT)
                        return -n-1;
                }
            }

            return n;
        }
    };

    public static Moves moves(boolean jumps) {
        return jumps ? Moves.JUMP : Moves.MOVE;
    }

    public Mover mover(boolean swap) {
        return new Mover(this, swap);
    }

    public Mover mover() {
        return mover(false);
    }

    private final int[] moves;

    private Moves(final int[] moves) {
        this.moves = moves;
    }

    public int size() {
        return moves.length;
    }

    /**
     * Move stones.
     * @param stay stones
     * @param move stones
     * @param mask stones to be moved
     * @return # of different mv or -n-1 if aborted
     */
    public int move(final int stay, final int move, final int mask, Process target) {

        if(mask==0)
            return 0;

        final int free = Stones.STONES ^ (stay|move);
        int n=0;

        // try all possible mv
        for (final int m : moves) {
            // must not change any opponents stone
            if ((m & stay) != 0)
                continue;

            // must match any masked bit.
            if ((m & mask) == 0)
                continue;

            // must match any free bit.
            if ((m & free) == 0)
                continue;

            ++n;

            // target may abort further processing by returning true
            if (target.process(stay, move, m) == ABORT)
                return -n - 1;
        }

        return n;
    }

    private static final Process ANY = new Process() {
        @Override
        public boolean process(int stay, int move, int mask) {
            return ABORT;
        }

        public String toString() {
            return "ANY";
        }
    };

    public boolean any(int stay, int move, int mask) {
        return move(stay, move, mask, ANY) != 0;
    }

    //////////////////////////////////////////////////////////////////

    private static int mv(int p1, int p2) {
        return (1 << p1) | (1 << p2);
    }

    static int[] moves() {
        final int[] moves = new int[32];
        int n = 0;

        for(Sector e:Sector.EDGES) {
            int m1 = e.mask();
            moves[n++] = m1*0x101;
            moves[n++] = m1*0x10001;
        }

        for(int mv:Sector.moves()) {
            moves[n++] = mv;
            moves[n++] = mv*0x100;
            moves[n++] = mv*0x10000;
        }

        return moves;
    }

    static int[] jumps() {

        final int[] moves = new int[23*24/2];
        int n = 0;

        for(int p1=0; p1<24; ++p1) {
            for(int p2=p1+1; p2<24; ++p2) {
                moves[n++] = mv(p1, p2);
            }
        }

        return moves;
    }

    static int[] takes() {
        final int[] takes = new int[24];

        for(int i=0; i<24; i++)
            takes[i] = 1<<i;

        return takes;
    }



}
