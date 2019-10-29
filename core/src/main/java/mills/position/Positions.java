package mills.position;

import mills.bits.Perm;
import mills.bits.Perms;
import mills.bits.PopCount;
import mills.ring.Entries;
import mills.ring.RingEntry;
import mills.stones.Stones;

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
 * A complete triple of three rings is packed into a long i201.
 *
 * Since a long value keeps 64 bits 16 bit are free to carry some additional status information.
 * Those are kept in the lower short to simplify sorting of i201 values.
 * This also simplifies status modifications.
 *
 * The status helps tracking operations on an i201 value.
 * Bits 0,1,2 carry the applied permutations, bit 3 indicates if the i20 part was swapped.
 * Bit 4 indicates if colors have been inverted.
 * Bit 5 indicates if the position has been normalized already.
 * Other bits a re currently unused and may be used to indicate closed mills or moved sector index.
 *
 * #### #### #### .... .... .... ....
 *  r2   r0   r1              NI SPPP
 *
 */

public interface Positions {

    int WORD = (1<<16)-1;

    int S1 = 16;
    int S0 = 32;
    int S2 = 48;

    int SWP = 1<<3;
    int INV = 1<<4;
    int PERMS = (1<<5)-1;
    int NORMALIZED = 1<<5;

    static short i2(long i201) {return (short) ((i201>>>S2) & WORD);}
    static short i0(long i201) {return (short) ((i201>>>S0) & WORD);}
    static short i1(long i201) {return (short) ((i201>>>S1) & WORD);}

    static int stat(long i201) {return (int) (i201 & WORD);}
    static int perms(long i201) {return (int) (i201 & PERMS);}
    static Perm perm(long i201) {return Perm.get(stat(i201));}
    static boolean normalized(long i201) {
        return (i201&NORMALIZED) != 0;
    }

    static RingEntry r2(long i201) {return Entries.of(i2(i201));}
    static RingEntry r0(long i201) {return Entries.of(i0(i201));}
    static RingEntry r1(long i201) {return Entries.of(i1(i201));}


    static long stones(int black, int white) {
        short i2 = Stones.i2(black, white);
        short i0 = Stones.i0(black, white);
        short i1 = Stones.i1(black, white);
        return i201(i2, i0, i1);
    }

    static long inverted(long i201) {
        RingEntry r2 = r2(i201).inverted();
        RingEntry r0 = r0(i201).inverted();
        RingEntry r1 = r1(i201).inverted();
        int stat = stat(i201);

        return i201(r2, r0, r1, stat^INV);
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
        RingEntry r2 = r2(i201).inverted();
        RingEntry r0 = r0(i201).inverted();
        RingEntry r1 = r1(i201).inverted();

        PopCount clop = r2.clop().add(r0.clop().add(r0.clop()));
        PopCount rad = clop.add(r2.radials().and(r0).and(r1).pop);

        return clop.add(rad);
    }

    static Position position(long i201) {
        return new Position(i201);
    }

    static boolean equals(long p1, long p2) {
        long diff = (p1 ^ p2) | WORD; // mask all status bits
        return diff == WORD;
    }

    static long i201(RingEntry r2, RingEntry r0, RingEntry r1, int stat) {
        return i201(r2.index, r0.index, r1.index, stat);
    }

    static long i201(short i2, short i0, short i1, int stat) {
        long i201 = stat&WORD;
        i201 |= ((long) i2) << S2;
        i201 |= ((long) i0) << S0;
        i201 |= ((long) i1) << S1;
        return i201;
    }

    static long i201(short i2, short i0, short i1) {
        return i201(i2, i0, i1, 0);
    }

    /**
     * Compose two operations including swap bit.
     * @param pm1
     * @param pm2
     * @return
     */
    static int compose(int pm1, int pm2) {
        int result = Perm.get(pm1).compose(pm2);

        // compose possible swap and inv flags
        result |= (pm1 ^ pm2) & (SWP|INV);

        return result;
    }

    // decompose, permute and compose an i201 index
    static long permute(long i201, int perm) {

        // decompose
        RingEntry r2 = r2(i201);
        RingEntry r0 = r0(i201);
        RingEntry r1 = r1(i201);
        int pm = perms(i201);

        // permute each
        r2 = r2.permute(perm);
        r0 = r0.permute(perm);
        r1 = r1.permute(perm);

        if((perm&INV)!=0) {
            r2 = r2.inverted();
            r0 = r0.inverted();
            r1 = r1.inverted();
        }

        pm = compose(perm, pm);

        // apply swap
        if((perm&SWP)!=0)
            return i201(r0, r1, r1, pm);
        else
            return i201(r2, r0, r1, pm);
    }

    static long permute(final long i201, Perm perm) {
        return permute(i201, perm.ordinal());
    }

    static long normalize(RingEntry r2, RingEntry r0, RingEntry r1) {

        int perm = 0;

        // find minimum of r2 or r1
        if (r2.min() < r0.min()) {
            RingEntry tmp = r0;
            r2 = r0;
            r0 = tmp;
            perm = SWP;
        }

        // apply initial normalisation on r2
        int pmin = r2.mix;
        if (pmin != 0) {
            r2 = r2.permute(pmin);
            r0 = r0.permute(pmin);
            r1 = r1.permute(pmin);
            perm |= pmin;
        }

        long m201 = Positions.i201(r2, r0, r1, 0);

        // possible permutations to minimize r20
        int mlt = r2.meq & (r0.mlt | r0.meq & r1.mlt);
        for (Perm p : Perms.of(mlt & 0xfe)) {
            long i201 = i201(r2.permute(p), r0.permute(p), r1.permute(p), 0);
            if (i201 < m201) {
                m201 = i201;
                pmin = p.ordinal();
            }
        }

        perm = compose(perm, pmin) | Positions.NORMALIZED;

        return m201 | perm;
    }

    static long normalize(long i201) {
        return normalize(i201, false);
    }

    static long normalinv(long i201) {
        return normalize(i201, true);
    }

    static long normalize(long i201, boolean invert) {

        if(Positions.normalized(i201))
            return i201;

        RingEntry r2 = Positions.r2(i201);
        RingEntry r0 = Positions.r0(i201);
        RingEntry r1 = Positions.r1(i201);

        int perm = perms(i201);

        if(invert) {
            r2 = r2.inverted();
            r0 = r0.inverted();
            r1 = r1.inverted();
            perm ^= INV;
        }

        i201 = normalize(r2, r0, r1);

        // changed permutations
        perm ^= compose(perm, perms(i201));

        // apply change
        i201 ^= perm;

        return i201;
    }
}
