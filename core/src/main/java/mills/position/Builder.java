package mills.position;

import mills.ring.Entries;
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

    default long build(short i2, short i0, short i1, int stat) {
        return build(Entries.of(i2), Entries.of(i0), Entries.of(i1), stat);
    }

    default long build(long i201) {
        RingEntry r2= Positions.r2(i201);
        RingEntry r0 = Positions.r0(i201);
        RingEntry r1 = Positions.r1(i201);
        int stat = Positions.stat(i201);
        return build(r2, r0, r1, stat);
    }
}
