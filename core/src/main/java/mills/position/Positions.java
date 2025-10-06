package mills.position;

import mills.bits.Clops;
import mills.bits.Perm;
import mills.bits.Perms;
import mills.bits.PopCount;
import mills.ring.Entries;
import mills.ring.RingEntry;
import mills.stones.Stones;

/*
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 14.08.11
 * Time: 11:02
 *
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
 * The status helps to track operations on an i201 value.
 * Bits 0,1,2 carry the applied permutations, bit 3 indicates if the i20 part was swapped.
 * Bit 4/5 indicate ring torsion of 0,1,2 (applicable for pop counts <=(3,3))
 * Bit 6 indicates if colors have been inverted.
 * Bit 7 indicates if the position has been normalized already.
 * Other bits a re currently unused and may be used to indicate closed mills or moved sector index.
 *
 * #### #### #### .... .... .... ....
 *  r2   r0   r1            NITT SPPP
 *
 */

public interface Positions {

    int WORD = (1<<16)-1;

    byte S1 = 16;
    byte S0 = 32;
    byte S2 = 48;

    static byte si(int i) {
        return switch (i % 3) {
            case 1, -2 -> S1;
            case 2, -1 -> S2;
            default -> S0;
        };
    }

    short SWP = 1<<3;

    short INV = 1<<6;

    short NORMALIZED = 1<<7;

    short PERMS = NORMALIZED-1;

    static short i2(long i201) {return (short) ((i201>>>S2) & WORD);}
    static short i0(long i201) {return (short) ((i201>>>S0) & WORD);}
    static short i1(long i201) {return (short) ((i201>>>S1) & WORD);}
    static short stat(long i201) {return (short) (i201 & WORD);}

    static long m201(long i201) {return i201>>>S1;}
    static short perms(long i201) {return (short) (i201 & PERMS);}
    static Perm perm(long i201) {return Perm.get(stat(i201));}

    static boolean normalized(long i201) {
        return (i201&NORMALIZED) != 0;
    }

    static RingEntry r2(long i201) {return Entries.entry(i2(i201));}
    static RingEntry r0(long i201) {return Entries.entry(i0(i201));}
    static RingEntry r1(long i201) {return Entries.entry(i1(i201));}

    static String format(long i201) {
        short i2 = i2(i201);
        short i0 = i0(i201);
        short i1 = i1(i201);
        int pm = stat(i201);

        return String.format("%d,%d,%d,%02X", i2, i0, i1, pm);
    }

    static long stones(int black, int white) {
        short i2 = Stones.i2(black, white);
        short i0 = Stones.i0(black, white);
        short i1 = Stones.i1(black, white);
        return i201(i2, i0, i1);
    }

    static long inverted(long i201) {
        short i2 = r2(i201).inverted;
        short i0 = r0(i201).inverted;
        short i1 = r1(i201).inverted;
        int stat = stat(i201);

        return i201(i2, i0, i1, stat^INV);
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
        RingEntry r2 = r2(i201);
        RingEntry r0 = r0(i201);
        RingEntry r1 = r1(i201);

        PopCount clop = r2.clop().add(r0.clop().add(r1.clop()));
        PopCount rad = r2.radials().and(r0).and(r1).pop;

        return clop.add(rad);
    }

    static Clops clops(long i201) {
        RingEntry r2 = r2(i201);
        RingEntry r0 = r0(i201);
        RingEntry r1 = r1(i201);

        PopCount pop = r2(i201).pop;
        pop = pop.add(r0(i201).pop);
        pop = pop.add(r1(i201).pop);

        PopCount clop = r2.clop().add(r0.clop().add(r1.clop()))
                .add(r2.radials().and(r0).and(r1).pop);

        return Clops.of(pop, clop);
    }

    static Position position(long i201) {
        return Position.of(i201);
    }

    static boolean equals(long p1, long p2) {
        long diff = (p1 ^ p2) | WORD; // mask all status bits
        return diff == WORD;
    }

    static int i20(RingEntry e2, RingEntry e0) {
        return (e2.index<<16) + e0.index;
    }

    static int i20(int i2, int i0) {
        return (i2<<16) + i0;
    }

    static long i201(short i2, short i0, short i1, int stat) {
        long i201 = stat&WORD;
        i201 |= ((long) i2) << S2;
        i201 |= ((long) i0) << S0;
        i201 |= ((long) i1) << S1;
        return i201;
    }

    static long i201(RingEntry r2, RingEntry r0, RingEntry r1, int stat) {
        return i201(r2.index, r0.index, r1.index, stat);
    }

    static long i201(short i2, short i0, short i1) {
        return i201(i2, i0, i1, 0);
    }

    /**
     * Compose two operations including swaps.
     * @param pm1 first permutation
     * @param pm2 second permutation
     * @return composed permutation
     */
    static short compose(short pm1, short pm2) {
        short pm3 = Perm.compose(pm1, pm2);
        pm3 |= Swap.compose(pm1, pm2);
        pm3 |= (pm1^pm2)&INV;
        return pm3;
    }

    /**
     * Calculate the inversion of a given status code.
     * @param stat status to invert.
     * @return the inverted status.
     */
    static short invert(short stat) {
        short inv = Perm.invert(stat);
        inv |= Swap.invert(stat);
        inv |= stat&INV;
        return inv;
    }

    // decompose, permute and compose an i201 index
    static long permute(long i201, int perm) {

        // decompose
        RingEntry r2 = r2(i201);
        RingEntry r0 = r0(i201);
        RingEntry r1 = r1(i201);
        short pm = perms(i201);

        // permute each
        r2 = r2.permute(perm);
        r0 = r0.permute(perm);
        r1 = r1.permute(perm);

        pm = Perm.compose(pm, perm);

        if((perm&INV)!=0) {
            r2 = r2.inverted();
            r0 = r0.inverted();
            r1 = r1.inverted();
            pm ^= INV;
        }

        return Swap.of((short)perm).build(r2, r0, r1, pm);
    }

    /**
     * Revert a position to its original state.
     */
    static long revert(long i201) {
        short perms = perms(i201);
        perms = invert(perms);
        return permute(i201, perms);
    }

    static long m201(RingEntry r2, RingEntry r0, RingEntry r1, int stat) {
        if(r0.index < r2.index)
            return i201(r0, r2, r1, stat|SWP);
        else
            return i201(r2, r0, r1, stat);
    }

    /**
     * This is the central procedure to normalize a position.
     * Conditions:
     * r1 must be minimized.
     * for each r1.min choose the smallest (r2, r0) resp (r0, r2).
     * @return the smallest combination found plus the status reflecting the necessary permutations.
     */
    static long normalizeR1(RingEntry r2, RingEntry r0, RingEntry r1) {

        int perm = r1.mix;

        if(perm!=0) {
            r2 = r2.permute(perm);
            r0 = r0.permute(perm);
            r1 = r1.permute(perm);
        }

        long m201 = m201(r2, r0, r1, perm);

        // p1 is minimized. Test for all stable perms of p1 except perm(0).
        for (Perm p : Perms.of(r1.meq & 0xfe)) {

            RingEntry p2 = r2.permute(p);
            RingEntry p0 = r0.permute(p);
            RingEntry p1 = r1.permute(p);

            perm = p.ordinal();

            long i201 = m201(p2, p0, p1, perm);

            if (i201 < m201) {
                // compare including current perm
                m201 = i201;
            }
        }

        return m201 | NORMALIZED;
    }

    static long normalize(final long i201) {
        return Normalizer.NORMAL.build(i201);
    }

    /**
     * Return a perm mask of all stable permutations.
     * If any permutation (with possible swap) reduces r20 return 0.
     * Else at least bit #0 is set.
     * @param e2 entry on ring 0 (minimized).
     * @param e0 entry on ring 2.
     * @return a perm mask of all stable permutations or 0.
     */
    static int meq(RingEntry e2, RingEntry e0) {

        // can be reduced immediately
        if(!e2.isMin())
            return 0;

        int e0min = e0.min();
        int e2index = e2.index;

        // some e0 is smaller, abort
        if(e0min<e2index)
            return 0;

        byte meq = e2.meq;

        // no further analysis necessary.
        if(e2==e0)
            return meq&0xff;

        // check if any e0 reduces while e2 remains stable
        if ((meq & e0.mlt) != 0)
            return 0;

        // e0 must stay stable, too.
        meq &= e0.meq;

        // if e0 is always bigger than e2 no further swaps or minima are expected.
        if (e0min > e2index)
            return meq&0xff;

        int e0index = e0.index;

        // by now: e0min == e2index (== e2min).
        // but still e0index > e2index.
        // Analyze stability after swapping e2 and e0:
        // Search for possible permutations of e2 with e2.perm(i) >= e0min.
        // e0index == e2.perm(i): additional stable permutation if swapped.
        // e0index < e2.perm(i): unstable (smaller) permutation after swap.
        // search all minima permutations of e0 excluding already verified meq.


        int msk = 0xff & e0.pmin & ~meq;

        long bitseq = Perms.of(msk).bitseq;
        while (bitseq != 0) {
            int i = (int) (bitseq&Perm.MSK);
            bitseq >>>= 4;

            int p2 = e2.perm(i);

            // even reduces
            if (p2 < e0index) {
                return 0;
            }

            // stable if swapped
            if (p2 == e0index) {
                meq |= (1<<i);
            }
        }

        return meq&0xff;
    }

    static int mlt20(RingEntry r2, RingEntry r0) {
        // r0 must be <= r2, else swap and tag
        if(r0.index<r2.index)
            return mlt20(r0, r2) | 1;

        // any reduction needs at least one lesser index
        int candidates = (r2.mlt|r0.mlt)&0xff;
        if(candidates==0)
            return 0;

        short i2 = r2.index;
        short i0 = r0.index;

        // current index value
        final int i20 = i20(r2, r0);

        // identity transformation won't reduce ever.
        int result = 0;
        int pi = 1;

        while(candidates!=0) {

            candidates >>>= 1;

            if(candidates%16==0) {
                candidates >>>= 4;
                pi += 4;
            }

            if(candidates%4==0) {
                candidates >>>= 2;
                pi += 2;
            }

            if(candidates%2!=0) {
                i2 = r2.perm(pi);
                i0 = r0.perm(pi);

                // check swapped combinations too (swaps are not tagged here)
                if(i20(i2, i0)<i20 || i20(i0, i2)<i20) {
                    result |= (1 << pi);
                }
            }

            ++pi;
        }

        return result;
    }
}
