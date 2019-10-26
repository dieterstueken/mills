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
public class Mover implements Moves.Process {

    public final Moves moves;
    public final Player player;

    private final long[] positions;
    private int size = 0;

    Mover(Moves moves, Player player) {
        this.positions = new long[moves.size()];
        this.moves = moves;
        this.player = player;
    }

    public String toString() {
        return moves.toString() + ":" + player.name().substring(0,1);
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
        return Stones.stones(i201, player());
    }

    public int black(long i201) {
        return Stones.stones(i201, player().other());
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

    private long i201(int stay, int move) {
        return switch(player) {
            case White -> Stones.i201(stay, move);
            case Black -> Stones.i201(move, stay);
            case None ->  0;
        };
    }

    public Player player() {
        return player;
    }

    public boolean process(int stay, int move) {
        long i201 = i201(stay, move);
        i201 = Positions.normalize(i201);
        positions[size] = i201;
        ++size;
        return true;
    }

    public Mover unique() {
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

        return this;
    }
}
