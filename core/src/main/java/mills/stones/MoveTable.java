package mills.stones;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 30.12.11
 * Time: 14:30
 */

import mills.bits.Player;
import mills.bits.Sector;

/**
 * Class Moves executes move operations based on a move table.
 * each move is propagated to a Move.Processor.
 * This class is not thread safe.
 */
abstract public class MoveTable {

    public static final MoveTable JUMP = new MoveTable(jumps()) {
        public String toString() {
            return "JUMP";
        }
    };

    public static final MoveTable MOVE = new MoveTable(moves()) {
        public String toString() {
            return "MOVE";
        }
    };

    public static final MoveTable TAKE = new MoveTable(takes()) {
        public String toString() {
            return "TAKE";
        }

        public int move(int stay, int move, int mask, MoveProcessor target) {

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
                    if(target.process(stay, move^m))
                        return -n-1;
                }
            }

            return n;
        }
    };

    public static MoveTable moves(boolean jumps) {
        return jumps ? MoveTable.JUMP : MoveTable.MOVE;
    }

    public Mover mover(boolean swap) {
        if(swap)
            return new Mover(this) {
                public long i201(int black, int white) {
                    return Stones.i201(white, black);
                }

                public Player moved() {
                    return Player.Black;
                }

                public String toString() {
                    return MoveTable.this.toString() + "X";
                }
            };
        else
            return new Mover(this) {
                public String toString() {
                    return MoveTable.this.toString() + "=";
                }
            };
    }

    public Mover mover() {
        return mover(false);
    }

    private final int[] moves;

    private MoveTable(final int[] moves) {
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
    public int move(final int stay, final int move, final int mask, MoveProcessor target) {

        if(mask==0)
            return 0;

        final int free = Stones.STONES ^ (stay|move);
        int n=0;

        // try all possible mv
        for(int i=0; i< moves.length; ++i) {
            final int m = moves[i];

            // must not change any opponents stone
            if((m&stay)!=0)
                continue;

            // must match any masked bit.
            if((m&mask)==0)
                continue;

            // must match any free bit.
            if((m&free)==0)
                continue;

            ++n;

            // target may abort further processing by returning true
            if(target.process(stay, move^m))
                return -n-1;
        }

        return n;
    }

    public boolean any(int stay, int move, int mask) {
        return move(stay, move, mask, MoveProcessor.ANY) != 0;
    }

    //////////////////////////////////////////////////////////////////

    private static int mv(int p1, int p2) {
        return (1 << p1) | (1 << p2);
    }

    static int[] moves() {
        final int[] moves = new int[32];
        int n = 0;

        for(Sector e:Sector.EDGES) {
            int m2 = e.mask();
            int m0 = m2<<8;
            int m1 = m2<<16;

            moves[n++] = m1|m0;
            moves[n++] = m1|m2;
        }

        for(int mv:Sector.moves()) {
            moves[n++] = mv;
            moves[n++] = mv<<8;
            moves[n++] = mv<<16;
        }

        return moves;
    }

    static int[] jumps() {
        //final List<Move> moves = new ArrayList<>(23*24/2);

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
