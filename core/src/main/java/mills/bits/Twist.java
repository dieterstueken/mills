package mills.bits;

import mills.position.Positions;

import static mills.position.Positions.SWP;
import static mills.position.Positions.WORD;

/**
 * The Enumeration Twist represents a permutation of the three rings.
 * <p>
 * version:     $
 * created by:  d.stueken
 * created on:  29.11.2022 15:55
 * modified by: $
 * modified on: $
 */
abstract public class Twist {

    public final byte msk, s0, s1, s2;

    final String name;

    protected Twist(int msk, int i0, int i1, int i2) {
        this.msk = (byte) msk;
        this.s0 = Positions.si(i0);
        this.s1 = Positions.si(i1);
        this.s2 = Positions.si(i2);

        name = String.format("%d:T%d%c", msk, msk/2, "NS".charAt(msk&1));
    }

    @Override
    public String toString() {
        return name;
    }

    abstract public Twist swap();

    public Twist invert() {
        return this;
    }

    public Twist compose(Twist other) {
        int m = Swapped.compose(msk, other.msk);
        return Swapped.twist(m);
    }

    public short i2(long i201) {
        return (short) ((i201 >>> s2) & WORD);
    }

    public short i0(long i201) {
        return (short) ((i201 >>> s0) & WORD);
    }

    public short i1(long i201) {
        return (short) ((i201 >>> s1) & WORD);
    }

    public short ix(long i201) {
        return (short) ((i201 >>> SWP) & 0x03);
    }

    public long twist(long i201) {
        short i2 = i2(i201);
        short i0 = i0(i201);
        short i1 = i1(i201);
        int stat = Positions.stat(i201);

        int m = (stat>>Positions.SWP)&7;
        m ^= Swapped.compose(m, msk);
        stat ^= m<<SWP;

        return Positions.i201(i2, i0, i1, stat);
    }
}
