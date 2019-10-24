package mills.index.builder;

import mills.bits.Perm;
import mills.bits.Perms;
import mills.position.Positions;
import mills.ring.RingEntry;

public class N201 {

    static long normalize(long i201) {

        if(!Positions.normalized(i201)) {

            RingEntry r2 = Positions.r2(i201);
            RingEntry r0 = Positions.r0(i201);
            RingEntry r1 = Positions.r1(i201);

            i201 =  normalize(r2, r0, r1);
        }

        // drop permutations
        return i201 & (Positions.M201| Positions.NORMALIZED);
    }

    // including perm bits
    static long normalizepm(final long i201) {
        if(Positions.normalized(i201))
            return i201;

        RingEntry r2 = Positions.r2(i201);
        RingEntry r0 = Positions.r0(i201);
        RingEntry r1 = Positions.r1(i201);
        int pm = Positions.perm(i201);

        return normalize(r2, r0, r1, pm);
    }

    static long normalize(RingEntry r2, RingEntry r0, RingEntry r1, int pm) {
        long m201 = normalize(r2, r0, r1);
        int px = Positions.perm(m201);
        px ^= Perm.compose(pm, px); // get changed bits
        m201 ^= (long) px << Positions.SP;    // assume px is unsigned
        return m201;
    }

    static long normalize(RingEntry r2, RingEntry r0, RingEntry r1) {

        // find minimum of r2 or r1

        if(r2.min() < r1.min())
            return normalize(r1, r2, r1) | Perm.SWP * Positions.PERM;

        // permutations to minimize r20
        int mlt = r2.pmin() & r0.pmlt();

        long m201 = Positions.i201(r2, r0, r1);
        Perm pmin = Perm.R0;

        for (Perm perm : Perms.of(mlt & 0xfe)) {
            long j201 = Positions.i201(r2, r0, r1);
            if(j201<m201) {
                m201 = j201;
                pmin = perm;
            }
        }

         return m201 | pmin.ordinal() * Positions.PERM;
    }
}
