package mills.position;

import mills.ring.RingEntry;

/**
 * version:     $
 * created by:  d.stueken
 * created on:  01.12.2022 18:04
 * modified by: $
 * modified on: $
 */
public interface Builder {

    long build(RingEntry r2, RingEntry r0, RingEntry r1, int stat);

    default long build(long i201) {
        RingEntry r2= Positions.r2(i201);
        RingEntry r0 = Positions.r0(i201);
        RingEntry r1 = Positions.r1(i201);
        int stat = Positions.stat(i201);
        return build(r2, r0, r1, stat);
    }

/*    // 100
    // i201
    static long i201(RingEntry r2, RingEntry r0, RingEntry r1, int stat) {
        return Positions.i201(r2.index, r0.index, r1.index, stat);
    }

    // 010
    static long i021(RingEntry r2, RingEntry r0, RingEntry r1, int stat) {
        return i201(r0, r2, r1, stat^SWP);
    }

    static long i210(RingEntry r2, RingEntry r0, RingEntry r1, int stat) {
        return Swap.T012.swap.build(r2, r0, r1, stat);
    }

    static long i012(RingEntry r2, RingEntry r0, RingEntry r1, int stat) {
        return Swap.T012.build(r2, r0, r1, stat);
    }

    static long i102(RingEntry r2, RingEntry r0, RingEntry r1, int stat) {
        return Swap.T012.swap.build(r2, r0, r1, stat);
    }

    static long i120(RingEntry r2, RingEntry r0, RingEntry r1, int stat) {
        return Swap.T120.build(r2, r0, r1, stat);
    }

    // 110
    static long ixx1(RingEntry r2, RingEntry r0, RingEntry r1, int stat) {
        if(r0.index<r2.index)
            return i021(r2, r0, r1, stat);
        else
            return i201(r2, r0, r1, stat);
    }

    // 10J
    static long i2xx(RingEntry r2, RingEntry r0, RingEntry r1, int stat) {
        if(r0.index<r1.index)
            return i201(r2, r0, r1, stat);
        else
            return i210(r1, r0, r2, stat);
    }

    // 01J
    static long i0xx(RingEntry r2, RingEntry r0, RingEntry r1, int stat) {
        if(r2.index<r1.index)
            return i021(r2, r0, r1, stat);
        else
            return i012(r2, r0, r1, stat);
    }

    // i001
    static long i1xx(RingEntry r2, RingEntry r0, RingEntry r1, int stat) {
        if(r0.index<r2.index)
            return i102(r2, r0, r1, stat);
        else
            return i120(r2, r0, r1, stat);
    }

    // 11J
    static long i20xx(RingEntry r2, RingEntry r0, RingEntry r1, int stat) {
        return Math.min(i2xx(r2, r0, r1, stat), i0xx(r2, r0, r1, stat));
    }

    // 101
    static long i21xx(RingEntry r2, RingEntry r0, RingEntry r1, int stat) {
        return Math.min(i2xx(r2, r0, r1, stat), i1xx(r2, r0, r1, stat));
    }

    // 011
    static long i01xx(RingEntry r2, RingEntry r0, RingEntry r1, int stat) {
        return Math.min(i0xx(r2, r0, r1, stat), i1xx(r2, r0, r1, stat));
    }

    // 111
    static long ixxx(RingEntry r2, RingEntry r0, RingEntry r1, int stat) {
        return Math.min(i01xx(r2, r0, r1, stat), i2xx(r2, r0, r1, stat));
    }*/
}
