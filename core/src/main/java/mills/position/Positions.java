package mills.position;

import mills.bits.Perm;
import mills.bits.Player;
import mills.bits.PopCount;
import mills.ring.Entries;
import mills.ring.RingEntry;
import mills.stones.Mills;
import mills.stones.Stones;

import java.util.function.IntBinaryOperator;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 14.08.11
 * Time: 11:02
 */

/**
 * Class Positions manages compacted bit masks representing a position of two or three rings.
 *
 * A single ring fits into a short. Since MAX_INDEX < 1<<13 bits 13-15 are void.
 *
 * The pair of inner and outer rings fits into an int i20.
 *
 * A complete triple of three rings is packed into a long i201.
 *
 * The middle ring (1) prepends the i20 bits (<<32).
 *
 * Since a long value keeps 64 bits the uppermost 16 bit my carry some additional information.
 * Bits 48,49,50 carry the applied permutations, bit 51 shows if the i20 part was swapped.
 * Bit 52 indicates if the position has been normalized already.
 * Bit 53 is used to indicate closed positions after elevation.
 *
 * A i201 position is normalized if r1 is minimized and r0 <= r2.
 *
 * ........ ........ #### #### ####
 *            CMSPPP  r1   r0   r2
 *
 * Bits 54-63 are currently unused
 */

public interface Positions {

    /**
     * An i201 index takes 3*16 = 48 bits to represent three ringTable entries.
     * For a normalized index p1 is normalized and p0<=p2.
     * Thus a normalized i201 index is the smallest value of all its permutations.
     * Thus 16 bits are available to carry additional status information.
     */

    static int MASK = (1<<16)-1;
    static long M201 = (1L<<48)-1;

    //static int S2 = 0;
    //static int S0 = 16;
    //static int S1 = 32;

    static int S1 = 0;
    static int S0 = 16;
    static int S2 = 32;

    static int SP = 48; // base of additional bits

    // bits 48,49,50,51: permutations/swap applied

    // bits 52:53 SWP3: swapped rings if jumping
    static long SWPX = 1L<<(SP+4);
    static long SWPY = 1L<<(SP+5);


     // if the entry was already normalized
    static long NORMALIZED = 1L<<(SP+7);

    static boolean normalized(long i201) {
        return (i201&NORMALIZED) != 0;
    }

    // the entry results of an elevated close
    static long CLOSED = 1L<<(SP+8);

    static boolean closed(long i201) {
        return (i201&CLOSED) != 0;
    }

    static short i2(long i201) {return (short) (MASK&(i201>>S2));}
    static short i0(long i201) {return (short) (MASK&(i201>>S0));}
    static short i1(long i201) {return (short) (MASK&(i201>>S1));}
    static byte perm(long i201) {return (byte) (Perm.PERM&(i201>>SP));}

    static RingEntry r2(long i201) {return Entries.of(i2(i201));}
    static RingEntry r0(long i201) {return Entries.of(i0(i201));}
    static RingEntry r1(long i201) {return Entries.of(i1(i201));}

    static long stones(int black, int white) {
        short i2 = Stones.i2(black, white);
        short i0 = Stones.i0(black, white);
        short i1 = Stones.i1(black, white);
        return i201(i2, i0, i1);
    }

    interface Builder {
        long i201(int black, int white);
    }

    static final Builder BW = new Builder() {
        @Override
        public long i201(int black, int white) {
            return stones(black, white);
        }

        public String toString() {
            return "BW";
        }
    };

    static final Builder WB = new Builder() {
        @Override
        public long i201(int black, int white) {
            return stones(white, black);
        }

        public String toString() {
            return "WB";
        }
    };

    static long swapped(long i201) {
        RingEntry r2 = r2(i201).swapped();
        RingEntry r0 = r0(i201).swapped();
        RingEntry r1 = r1(i201).swapped();
        int pm = perm(i201);

        return i201(r2.index, r0.index, r1.index, pm);
    }

    static PopCount pop(long i201) {
        PopCount pop = r2(i201).pop;
        pop = pop.add(r0(i201).pop);
        pop = pop.add(r1(i201).pop);
        return pop;
    }

    /**
     * Count closed mills.
     * @param i201 position to analyze.
     * @return population count of closed mills.
     */
    static PopCount clop(long i201) {
        return PopCount.of(
                Mills.count(i201, Player.Black),
                Mills.count(i201, Player.White)
        );
    }

    static Position position(long i201) {
        return new Position(i201);
    }

    static boolean equals(long p1, long p2) {
        long diff = (p1 ^ p2) & M201;
        return diff == 0;
    }

    static long i201(int i2, int i0, int i1) {
        long i201;
        i201  = ((long) i2) << S2;
        i201 |= ((long) i0) << S0;
        i201 |= ((long) i1) << S1;
        return i201;
    }

    static long n201(int i2, int i0, int i1) {
        long n201 = i201(i2, i0, i1);
        assert n201 == (normalize(n201) & M201);
        return n201;
    }


    static long i201(int i2, int i0, int i1, int perm) {
        long i201 = i201(i2, i0, i1);
        i201 |= ((long) perm) << SP;
        return i201;
    }

    // for a given i1: build a normalized i201 index with possibly swapped 2:0 whichever is smaller.
    static long m201(short i2, short i0, short i1, int perm) {
        if(i2<i0)
            return NORMALIZED | i201(i0, i2, i1, perm^Perm.SWP);
        else // i2 >= i0
            return NORMALIZED | i201(i2, i0, i1, perm);
    }

    // decompose, permute and compose an i201 index
    static long permute(final long i201, int perm) {

        // decompose
        RingEntry r2 = r2(i201);
        RingEntry r0 = r0(i201);
        RingEntry r1 = r1(i201);
        int pm = perm(i201);

        // permute each
        int i2 = r2.perm(perm);
        int i0 = r0.perm(perm);
        int i1 = r1.perm(perm);
        pm = compose(pm, perm);

        // compose
        if((perm&Perm.SWP)==0)
            return i201(i2, i0, i1, pm);
        else
            return i201(i0, i2, i1, pm);
    }

    // without perm bits
    static long normalize(long i201) {

        if(!normalized(i201)) {

            RingEntry r2 = r2(i201);
            RingEntry r0 = r0(i201);
            RingEntry r1 = r1(i201);

            i201 =  normalize(r2, r0, r1);
        }

        // drop permutations
        return i201 & (M201|NORMALIZED);
    }

    // including perm bits
    static long normalizepm(final long i201) {
        if(normalized(i201))
            return i201;

        RingEntry r2 = r2(i201);
        RingEntry r0 = r0(i201);
        RingEntry r1 = r1(i201);
        int pm = perm(i201);

        return normalize(r2, r0, r1, pm);
    }

    static long normalize3(final long i201) {
        if(normalized(i201))
            return i201;

        RingEntry r2 = r2(i201);
        RingEntry r0 = r0(i201);
        RingEntry r1 = r1(i201);
        int pm = perm(i201);

        long m201 = normalize(r2, r0, r1, pm);
        long m120 = normalize(r1, r2, r0, pm) | SWPX;
        long m102 = normalize(r1, r0, r2, pm) | SWPY;

        if((m201&M201) < ((m102&M201))) {
            return (m201&M201) < (m120&M201) ? m201 : m120;
        } else {
            return (m102&M201) < (m120&M201) ? m102 : m120;
        }
    }

    static long normalize(RingEntry r2, RingEntry r0, RingEntry r1, int pm) {
        long m201 = normalize(r2, r0, r1);
        int px = perm(m201);
        px ^= compose(pm, px); // get changed bits
        m201 ^= (long) px << SP;    // assume px is unsigned
        return m201;
    }

    static long normalize(RingEntry r2, RingEntry r0, RingEntry r1) {

        // minimize middle ring (1)
        int perm = r1.mix;
        if(perm!=0) {
            r2 = Entries.of(r2.perm(perm));
            r0 = Entries.of(r0.perm(perm));
            r1 = Entries.of(r1.perm(perm));
            //pm = Perm.compose(pm, perm);
        }

        // extract indexes
        short i2 = r2.index;
        short i0 = r0.index;

        final short i1 = r1.index;  // will stay stable

        // first possible candidate
        long m201 = m201(i2, i0, i1, 0);
        int m02 = m02(m201);

        // mask of permutations under which r1 remains normalized and any of r2 or r0 reduces
        int candidates = r1.meq & (r2.mlt|r0.mlt) & 0xff;

        perm=0;
        candidates >>= 1;
        while(candidates!=0) {

            ++perm;

            if(candidates%16==0) {
                candidates >>= 4;
                perm += 4;
            }

            if(candidates%4==0) {
                candidates >>= 2;
                perm += 2;
            }

            if(candidates%2!=0) {

                // find if this permutation is smaller (ignoring r1 and any flags)
                i2 = r2.perm(perm);
                i0 = r0.perm(perm);

                long l201 = m201(i2, i0, i1, perm);
                int l02 = m02(l201);
                //if((l201& M201) < (m201& M201))
                if(l02 < m02) {
                    m201 = l201;
                    m02 = l02;
                }
            }

            candidates >>= 1;
        }

        return m201;
    }

    static int m02(long i201) {
        return i0(i201)*(1<<16) + i2(i201);
    }

    static byte compose(int p1, int p2) {
        return (byte) PERMS.applyAsInt(p1, p2);
    }

    IntBinaryOperator PERMS = new IntBinaryOperator() {

        // put into the protected context of a class
        private byte composed[] = new byte[64];

        {
            for (Perm p1 : Perm.VALUES) {
                for (Perm p2 : Perm.VALUES) {
                    Perm px = p1.compose(p2);
                    composed[8*p1.ordinal() + p2.ordinal()] = (byte) px.ordinal();
                }
            }

        }

        @Override
        public int applyAsInt(int left, int right) {
            return composed[8*left + right];
        }
    };
}
