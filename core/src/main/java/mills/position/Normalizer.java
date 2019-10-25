package mills.position;

import mills.bits.Perm;
import mills.ring.RingEntry;

/**
 * version:     $
 * created by:  d.stueken
 * created on:  25.10.2019 11:07
 * modified by: $
 * modified on: $
 */
public interface Normalizer {

    long SPERM = 1L<<Positions.SP;

    long normalize(RingEntry r2, RingEntry r0, RingEntry r1);

    default long normalize(long i201) {

        if(!Positions.normalized(i201)) {

            RingEntry r2 = Positions.r2(i201);
            RingEntry r0 = Positions.r0(i201);
            RingEntry r1 = Positions.r1(i201);

            i201 =  normalize(r2, r0, r1);
        }

        // drop permutations
        return i201 & (Positions.M201|Positions.NORMALIZED);
    }

    default long normalize(RingEntry r2, RingEntry r0, RingEntry r1, int pm) {
        long m201 = normalize(r2, r0, r1);
        int px = Positions.perm(m201);
        px ^= Perm.compose(pm, px); // get changed bits
        m201 ^= (long) px << Positions.SP;    // assume px is unsigned
        return m201;
    }

    // including perm bits
    default long normalizepm(final long i201) {
        if(Positions.normalized(i201))
            return i201;

        RingEntry r2 = Positions.r2(i201);
        RingEntry r0 = Positions.r0(i201);
        RingEntry r1 = Positions.r1(i201);
        int pm = Positions.perm(i201);

        return normalize(r2, r0, r1, pm);
    }
}
