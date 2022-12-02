package mills.position;

import mills.bits.Perm;
import mills.bits.Perms;
import mills.bits.PopCount;
import mills.ring.RingEntry;

import static mills.position.Positions.INV;
import static mills.position.Positions.NORMALIZED;
import static mills.position.Positions.perms;

/**
 * version:     $
 * created by:  d.stueken
 * created on:  01.12.2022 22:41
 * modified by: $
 * modified on: $
 */
public interface Normalizer {

    static long normalize(Builder b, RingEntry r2, RingEntry r0, RingEntry r1) {
        long m201 = b.build(r2, r0, r1, 0);

        for (Perm p : Perms.of(0xfe)) {
            RingEntry p2 = r2.permute(p);
            RingEntry p0 = r0.permute(p);
            RingEntry p1 = r1.permute(p);
            int perm = p.ordinal();

            long i201 = b.build(p2, p0, p1, perm);

            if (i201 < m201)
                m201 = i201;
        }

        if(m201==Integer.MAX_VALUE)
            throw new IllegalStateException("normalization failed");

        return m201 | NORMALIZED;
    }

    static Builder builder(RingEntry r2, RingEntry r0, RingEntry r1) {
        short min = r2.min();
        PopCount pop = r2.pop;
        int msk = 1;

        // analyze r0
        short m = r0.min();
        if(m<min) {
            min = m;
            msk = 2;
        } else if(m==min) {
            msk |= 2;
        }

        if(pop.le(PopCount.P33)) {
            pop = pop.add(r0.pop);
            if (pop.le(PopCount.P33)) {
                pop = pop.add(r1.pop);
                if(pop.le(PopCount.P33)) {
                    m = r1.min();
                    if(m<min)
                        return Swap.T0;

                    if(m==min)
                        msk |= 4;

                    msk += 3;
                }
            }
        }

        switch(msk) {
            case 1: return Swap.T0;
            case 2: return Swap.S0;
            case 3: return Normalizer::txx1;
        }

        throw new IllegalStateException("no builder found");
    }

    static long txx1(RingEntry r2, RingEntry r0, RingEntry r1, int stat) {
        if(r0.min<r2.min)
            return Swap.T0.build(r2, r0, r1, stat);
        else
            return Swap.S0.build(r2, r0, r1, stat);
    }

    static long normalize(RingEntry r2, RingEntry r0, RingEntry r1) {
        Builder b = builder(r2, r0, r1);
        return normalize(b, r2, r0, r1);
    }

    static long normalize(RingEntry r2, RingEntry r0, RingEntry r1, int perms) {
        long n201 = normalize(r2, r0, r1);

        // changed permutation flags
        perms ^= compose(perms, perms(n201));

        // apply change
        n201 ^= perms;

        return n201;
    }

    /**
     * Compose two operations including swap bit.
     * @param pm1 first permutation
     * @param pm2 second permutation
     * @return composed permutation
     */
    static int compose(int pm1, int pm2) {
        pm1 = Perm.compose(pm1, pm2);
        pm1 = Twist.compose(pm1, pm2);
        pm1 ^= pm2& INV;
        return pm1;
    }
}
