package mills.stones;

import mills.position.Positions;
import mills.util.AbstractRandomList;

import java.util.Arrays;
import java.util.List;
import java.util.function.LongConsumer;
import java.util.function.LongFunction;

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

    public <T> List<T> transform(LongFunction<T> generator) {
        return new AbstractRandomList<>() {

            @Override
            public int size() {
                return size;
            }

            @Override
            public T get(int index) {
                return generator.apply(get201(index));
            }
        };
    }

    public Mover move(int stay, int move, int mask) {
        clear();
        if(mask!=0) {
            moves.move(stay, move, mask, this);
        }

        return this;
    }

    private long i201(int stay, int move) {
        return swap ? Stones.i201(move, stay) | Positions.INV : Stones.i201(stay, move);
    }

    public boolean process(int stay, int move, int mask) {
        long i201 = i201(stay, move^mask);
        //i201 = Positions.normalize(i201);
        positions[size] = i201;
        ++size;
        return !Moves.ABORT;
    }

    public Mover analyze(LongConsumer analyzer) {

        for(int i=0; i<size; ++i)
            analyzer.accept(positions[i]);

        return this;
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
