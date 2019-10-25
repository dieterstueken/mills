package mills.scores.opening;

import mills.position.Position;
import mills.position.Positions;
import mills.stones.Stones;
import mills.util.AbstractRandomList;

import java.util.Arrays;
import java.util.List;

/**
 * version:     $Revision$
 * created by:  dst
 * created on:  14.10.13 10:54
 * modified by: $Author$
 * modified on: $Date$
 */
public class PutStone<Target extends Position.Factory> {

    final Target target;

    long positions[] = new long[24];
    int size = 0;

    final Positions.Builder builder;

    interface Processor {
        void process(int black, int white);
    }

    final Processor insert;

    PutStone(Target target, boolean swap) {

        this.target = target;
        this.builder = swap ? Positions.WB : Positions.BW;

        this.insert = (black, white) -> {
            long i201 = builder.i201(black, white);
            i201 = target.normalize(i201);

            if (size >= positions.length)
                positions = Arrays.copyOf(positions, 2 * size);

            positions[size] = i201;
            ++size;
        };
    }

    public static <T extends Position.Factory> PutStone<T>
    put(T target, boolean swap) {
        return new PutStone<>(target, swap);
    }

    public static <T extends Position.Factory> PutStone<T>
    hit(T target, boolean swap) {

        return new PutStone<T>(target, !swap) {

            final Processor hit = (black, white) -> {
                int take = black ^ Stones.closed(black);
                if (take == 0)
                    take = black;

                // swap and take a black stone
                move(white, black, take, insert);
            };

            public PutStone move(int black, int white, int mask) {
                return move(black, white, mask, hit);
            }
        };
    }

    public int size() {
        return size;
    }

    public PutStone move(int black, int white, int mask) {
        return move(black, white, mask, insert);
    }

    public PutStone move(int black, int white, int mask, Processor processor) {
        clear();

        int m = 1 << 24;
        while (mask != 0) {
            int j;
            m >>>= 1;

            if (mask <= (j = m >>> 8))
                m = j;

            if (mask <= (j = m >>> 4))
                m = j;

            if (mask <= (j = m >>> 2))
                m = j;

            if ((m & mask) != 0) {
                // clear bit
                mask ^= m;
                processor.process(black, white ^ m);
            }
        }

        unique();

        return this;
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

    public void clear() {
        //verify();
        size = 0;
    }

    public long get201(int index) {
        return positions[index];
    }

    // debug
    public final List<Position> moved = new AbstractRandomList<Position>() {

        @Override
        public Position get(int index) {
            final long m201 = get201(index);
            return target.position(m201);
        }

        @Override
        public int size() {
            return size;
        }
    };

}
