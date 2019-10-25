package mills.partitions;

import mills.bits.Perm;
import mills.position.Normalizer;
import mills.position.Positions;
import mills.ring.Entries;
import mills.ring.RingEntry;

/**
 * Normalistion 120 normalizes R1 first (which will be minimized).
 * Then R20 are minimized. R1 is restricted to positions which must not reduce R20 further.
 * ? does any position exist with smaller R1 but other R20?
 */
public class N120 implements Normalizer {

    public long normalize3(final long i201) {
        if(Positions.normalized(i201))
            return i201;

        RingEntry r2 = Positions.r2(i201);
        RingEntry r0 = Positions.r0(i201);
        RingEntry r1 = Positions.r1(i201);
        int pm = Positions.perm(i201);

        long m201 = normalize(r2, r0, r1, pm);
        long m120 = normalize(r1, r2, r0, pm) | Positions.SWPX;
        long m102 = normalize(r1, r0, r2, pm) | Positions.SWPY;

        if((m201& Positions.M201) < ((m102& Positions.M201))) {
            return (m201& Positions.M201) < (m120& Positions.M201) ? m201 : m120;
        } else {
            return (m102& Positions.M201) < (m120& Positions.M201) ? m102 : m120;
        }
    }

    public long normalize(RingEntry r2, RingEntry r0, RingEntry r1) {

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
        int m02 = Positions.m02(m201);

        // mask of permutations under which r1 remains normalized and any of r2 or r0 reduces
        int candidates = r1.meq & (r2.mlt|r0.mlt) & 0xff;

        perm=0;
        candidates >>>= 1;
        while(candidates!=0) {

            ++perm;

            if(candidates%16==0) {
                candidates >>>= 4;
                perm += 4;
            }

            if(candidates%4==0) {
                candidates >>>= 2;
                perm += 2;
            }

            if(candidates%2!=0) {

                // find if this permutation is smaller (ignoring r1 and any flags)
                i2 = r2.perm(perm);
                i0 = r0.perm(perm);

                long l201 = m201(i2, i0, i1, perm);
                int l02 = Positions.m02(l201);
                //if((l201& M201) < (m201& M201))
                if(l02 < m02) {
                    m201 = l201;
                    m02 = l02;
                }
            }

            candidates >>>= 1;
        }

        return m201;
    }

    // for a given i1: build a normalized i201 index with possibly swapped 2:0 whichever is smaller.
    static long m201(short i2, short i0, short i1, int perm) {
        if(i2<i0)
            return Positions.NORMALIZED | Positions.i201(i0, i2, i1, perm^ Perm.SWP);
        else // i2 >= i0
            return Positions.NORMALIZED | Positions.i201(i2, i0, i1, perm);
    }


    // return a permutation mask of all reducing operations.
    // Bit#0 is set to 1 if an initial swap reduces the rank.
    static int mlt20(RingEntry r2, RingEntry r0) {

        // r0 must be <= r2, else swap and tag
        if(r2.index<r0.index)
            return mlt20(r0, r2) | 1;

        // any reduction needs at least one lesser index
        int candidates = (r2.mlt|r0.mlt)&0xff;
        if(candidates==0)
            return 0;

        short i2 = r2.index;
        short i0 = r0.index;

        // current index value
        final int i20 = i20(i2, i0);

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
                if(i20(i2, i0)<i20 || i20(i0, i2)<i20)
                    result |= (1<<pi);
            }

            ++pi;
        }

        return result;
    }

    // utility function to speed up comparison by mapping to an integer.
    static int i20(int i2, int i0) {
        return i2 | (i0<<16);
    }
}
