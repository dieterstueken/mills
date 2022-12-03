package mills.position;

import mills.bits.Perm;
import mills.ring.RingEntry;

import static mills.position.Positions.*;

/**
 * created on:  01.12.2022 22:41
 */
public class Normalizer implements Builder {

    public long normalize(final long i201) {

        if(Positions.normalized(i201))
            return i201;

        RingEntry r2 = Positions.r2(i201);
        RingEntry r0 = Positions.r0(i201);
        RingEntry r1 = Positions.r1(i201);
        int perms = perms(i201);

        return normalize(r2, r0, r1, perms);
    }

    public long normalize(RingEntry r2, RingEntry r0, RingEntry r1, int perms) {
        if(Positions.normalized(perms))
            return build(r2, r0, r1, perms);

        if(r0.min() < r2.min()) {
            return minimize(r0, r2, r1, perms^Positions.SWP);
        } else
            return minimize(r2, r0, r1, perms);
    }

    protected long minimize(RingEntry r2, RingEntry r0, RingEntry r1, int perms) {

        if(!r2.isMin()) {
            int perm = r2.mix;
            r2 = r2.permute(perm);
            r0 = r0.permute(perm);
            r1 = r1.permute(perm);
            perms = compose(perms, perm);
        }

        long n201 = minimize(r2, r0, r1);
        n201 = prepend(n201, perms);

        return n201 | NORMALIZED;
    }

    protected long minimize(RingEntry r2, RingEntry r0, RingEntry r1) {
        // initial value, assume r2 is minimized
        assert r2.isMin();

        long m201 = build(r2, r0, r1, 0);

        if (r2 == r1) {
            // shortcut: r2==r1==r0
            assert r0 == r1;
            return m201;
        }

        // only minima of r2 to analyze except 0
        int msk = 0xfe & r2.min;

        while(msk!=0) {
            int perm = Integer.highestOneBit(msk);
            msk ^= perm;

            RingEntry p2 = r2.permute(perm);
            RingEntry p0 = r0.permute(perm);
            RingEntry p1 = r1.permute(perm);

            long i201 = build(p2, p0, p1, perm);

            if (i201 < m201)
                m201 = i201;
        }

        return m201;
    }

    public long build(final RingEntry r2, final RingEntry r0, final RingEntry r1, final int stat) {
        return Positions.i201(r2.index, r0.index, r1.index, stat);
    }

    static final Normalizer NORMAL = new Normalizer();

    static final Normalizer JUMPS = new Normalizer() {

        protected long minimize(RingEntry r2, RingEntry r0, RingEntry r1, int perms) {

            if(r1.min()<r2.min())
                return super.minimize(r1, r2, r0, Swap.T2.applyTo(perms));

            if(r1==r2 || r1==r0)
                return NORMAL.minimize(r1, r2, r0, Swap.T2.applyTo(perms));

            return super.minimize(r2, r0, r1, perms);
        }

        @Override
        public long build(final RingEntry r2, final RingEntry r0, final RingEntry r1, final int stat) {
            // possible swap of r0:r1
            if(r1.index<r0.index)
                return Swap.S2.build(r2, r1, r0, stat);
            else
                return super.build(r2, r0, r1, stat);
        }
    };

    /**
     * Compose two operations including swap bit.
     * @param pm1 first permutation
     * @param pm2 second permutation
     * @return composed permutation
     */
    public static int compose(int pm1, int pm2) {
        pm1 = Perm.compose(pm1, pm2);
        pm1 = Twist.compose(pm1, pm2);
        pm1 ^= pm2& INV;
        return pm1;
    }

    /**
     * Add a previous permutation on i201.
     * @param i201 position to modify.
     * @param first permutation to prepend.
     * @return the permuted position.
     */
    public static long prepend(long i201, int first) {
        int second = perms(i201);
        int result = compose(first, second);
        int changed = second^result;
        i201 ^= changed;
        return i201;
    }
}
