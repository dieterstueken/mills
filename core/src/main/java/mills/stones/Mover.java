package mills.stones;

import mills.position.Positions;

import java.util.Arrays;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 18.11.12
 * Time: 12:11
 */
public class Mover implements Moves.Process {

    public final Moves moves;
    public final boolean swap;

    private final long[] positions;
    private int size = 0;

    Mover(Moves moves, boolean swap) {
        this.positions = new long[moves.size()];
        this.moves = moves;
        this.swap = swap;
    }

    public String toString() {
        return moves.toString() + ":" + (swap ? "X" : "=");
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

    public Mover move(int stay, int move, int mask) {
        clear();
        if(mask!=0) {
            moves.move(stay, move, mask, this);
            normalize();
        }

        return this;
    }

    private long i201(int stay, int move) {
        return swap ? Stones.i201(move, stay) | Positions.INV : Stones.i201(stay, move);
    }

    public boolean process(int stay, int move) {
        long i201 = i201(stay, move);
        //i201 = Positions.normalize(i201);
        positions[size] = i201;
        ++size;
        return true;
    }

    public Mover normalize() {
        if (size > 0) {

            for(int i=0; i<size; ++i)
                positions[i] = Positions.normalize(positions[i]);

            Arrays.sort(positions, 0, size);

            long p = positions[0];
            int k = 1;
            for (int i = 1; i < size; ++i) {
                long m = positions[i];
                if (!Positions.equals(m,p)) {
                    p = m;
                    if (i != k)
                        positions[k] = m;

                    ++k;
                }
            }

            size = k;
        }

        return this;
    }
}
