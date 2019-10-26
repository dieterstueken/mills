package mills.stones;

import mills.bits.Player;
import mills.position.Positions;

import java.util.Arrays;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 18.11.12
 * Time: 12:11
 */
public class Mover implements MoveProcessor {

    public final MoveTable moves;

    private final long[] positions;
    private int size = 0;

    Mover(MoveTable moves) {
        this.positions = new long[moves.size()];
        this.moves = moves;
    }

    /**
     * Create a new mover instance.
     * @param swap if the new mover should swap output.
     * @return a new mover instance.
     */
    public Mover mover(boolean swap) {
        return moves.mover(swap);
    }

    public int size() {
        return size;
    }

    public void clear() {
        //verify();
        size = 0;
    }

    public long get201(int index) {
        return positions[index];
    }

    public int white(long i201) {
        return Stones.stones(i201, moved());
    }

    public int black(long i201) {
        return Stones.stones(i201, moved().other());
    }

    public Mover move(int stay, int move) {
        return move(stay, move, move);
    }

    public Mover move(int stay, int move, int mask) {
        clear();
        if(mask!=0) {
            moves.move(stay, move, mask, this);
            unique();
        }

        return this;
    }

    public long i201(int black, int white) {
        return Stones.i201(black, white);
    }

    public Player moved() {
        return Player.White;
    }

    public boolean process(int stay, int move) {
        long i201 = i201(stay, move);
        i201 = Positions.normalize(i201);
        positions[size] = i201;
        ++size;
        return true;
    }

    private void unique() {
        if (size > 0) {

            Arrays.sort(positions, 0, size);

            long p = positions[0];
            int k = 1;
            for (int i = 1; i < size; ++i) {
                long m = positions[i];
                if (m != p) {
                    p = m;
                    if (i != k)
                        positions[k] = m;

                    ++k;
                }
            }

            size = k;
        }
    }
}
