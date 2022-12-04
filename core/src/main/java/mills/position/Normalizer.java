package mills.position;

import mills.bits.Perms;
import mills.ring.RingEntry;

import static mills.position.Positions.*;

/**
 * created on:  01.12.2022 22:41
 */
public class Normalizer implements Builder {

    public long build(RingEntry r2, RingEntry r0, RingEntry r1, int perms) {
        if(Positions.normalized(perms))
            return Positions.i201(r2, r0, r1, perms);

        if(r0.min() < r2.min()) {
            return minimize(r0, r2, r1, perms^Positions.SWP);
        } else
            return minimize(r2, r0, r1, perms);
    }

    protected long minimize(RingEntry r2, RingEntry r0, RingEntry r1, int perms) {

        if(!r2.isMin()) {
            short perm = r2.mix;
            r2 = r2.permute(perm);
            r0 = r0.permute(perm);
            r1 = r1.permute(perm);
            perms = compose((short)perms, perm);
        }

        long n201 = minimize(r2, r0, r1);
        n201 = prepend(n201, perms);

        return n201 | NORMALIZED;
    }

    protected long minimize(RingEntry r2, RingEntry r0, RingEntry r1) {
        // initial value, assume r2 is minimized
        assert r2.isMin();

        long m201 = swap(r2, r0, r1, 0);

        // only minima of r2 to analyze except 0
        int bitseq = Perms.of(mlt(r2, r0, r1)).bitseq;

        while(bitseq!=0) {
            int perm = bitseq&0x3;
            bitseq >>>= 4;

            RingEntry p2 = r2.permute(perm);
            RingEntry p0 = r0.permute(perm);
            RingEntry p1 = r1.permute(perm);

            long i201 = swap(p2, p0, p1, perm);

            if (i201 < m201)
                m201 = i201;
        }

        return m201;
    }

    protected long swap(RingEntry r2, RingEntry r0, RingEntry r1, int stat) {
        // possible swap of r2:r0
        if(r0.index<r2.index)
            return Positions.i201(r0, r2, r1, stat^SWP);
        else
            return Positions.i201(r2, r0, r1, stat);
    }

    public static final Normalizer NORMAL = new Normalizer();

    public static final Normalizer JUMP = new Normalizer() {

        protected long minimize(RingEntry r2, RingEntry r0, RingEntry r1, int perms) {

            // general swap of r0:r1
            if(r1.min()<r2.min())
                return super.minimize(r1, r2, r0, Swap.T120.permute(perms));

            //if(r1==r2 || r1==r0)
            //    return super.minimize(r1, r2, r0, Swap.T2.applyTo(perms));

            return super.minimize(r2, r0, r1, perms);
        }

        @Override
        public long swap(RingEntry r2, RingEntry r0, RingEntry r1, int stat) {
            // possible swap of r0:r1
            if(r1.index<r0.index)
                return super.swap(r2, r1, r0, Swap.T120.permute(stat));
            else
                return super.swap(r2, r0, r1, stat);
        }
    };

    int mlt(RingEntry r2, RingEntry r0, RingEntry r1) {

        int m2 = r2.min();
        int m0 = r0.min();

        int mlt = r2.pmin&0xff;

        // take r0 into account, too
        if(m0==m2) {
            // no additional flags from r0, only reductions of r1 are relevant
            if(r2==r0)
                mlt &= r1.mlt;
            else // additional flags from shifted r0
                mlt |= r0.pmin &0xff;
        } else {
            // also any reductions of r0 (no jumps)
            mlt &= r0.mlt|r0.meq;
        }

        return mlt;
    }

    /**
     * Add a previous permutation on i201.
     * @param i201 position to modify.
     * @param first permutation to prepend.
     * @return the permuted position.
     */
    public static long prepend(long i201, int first) {
        short second = perms(i201);
        short result = compose((short)first, second);
        int changed = second^result;
        i201 ^= changed;
        return i201;
    }
}
