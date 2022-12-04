package mills.position;

import mills.ring.RingEntry;
import mills.util.Indexed;

import java.util.List;

import static mills.position.Positions.*;

/**
 * version:     $
 * created by:  d.stueken
 * created on:  29.11.2022 21:38
 * modified by: $
 * modified on: $
 */
public enum Swap implements Indexed, Builder {

    T201(S2, S0, S1), // III
    S021(S0, S2, S1), // XII
    T012(S0, S1, S2), // ///
    S102(S1, S0, S2), // >I<
    T120(S1, S2, S0), // \\\
    S210(S2, S1, S0); // IX

    final byte s0, s2, s1;

    Swap(byte s2, byte s0, byte s1) {
        this.s2 = s2;
        this.s0 = s0;
        this.s1 = s1;
    }

    static final int S = 1;
    static final int TT = 6;
    static final int TTS = TT|S;
    static final int OFF = 3;

    public byte tts() {
        return (byte) ordinal();
    }

    public int getIndex() {
        return ordinal();
    }

    public Swap compose(Swap other) {
        return of(compose(tts(), other.tts()));
    }

    public Swap invert() {
        return switch (this) {
            case T120 -> T012;
            case T012 -> T120;
            default -> this;
        };
    }


    @Override
    public long build(RingEntry r2, RingEntry r0, RingEntry r1, int stat) {
        return build(r2.index, r0.index, r1.index, stat);
    }

    @Override
    public long build(short i2, short i0, short i1, int stat) {
        long i201 = permute(stat);
        i201 |= ((long) i2) << s2;
        i201 |= ((long) i0) << s0;
        i201 |= ((long) i1) << s1;
        return i201;
    }

    public Swap swap() {
        return VALUES.get(ordinal()^1);
    }

    public int permute(int stat0) {

        byte tt0 = tts(stat0);

        // apply changed bits
        tt0 ^= compose(tt0, tts());
        stat0 ^= tt0 << OFF;

        return stat0;
    }


    public static final List<Swap> VALUES = List.of(values());


    public static byte tts(long i201) {
        return (byte)((i201>>OFF)&TTS);
    }

    public static Swap of(short status) {
        return VALUES.get(tts(status));
    }

    public static Swap of(byte tts) {
        return VALUES.get(tts);
    }

    public static byte compose(byte m0, byte m1) {

        // m1.swapped ? m1-m0 : m1+m0
        if((m1&S)==0)
            m1 += m0&TT;
        else
            m1 -= (m0&TT) - TT;

        // normalize twist
        m1 %= TT;

        // apply swap
        m1 ^= m0&S;

        // no additional bits expected.
        assert m1==(m1&TTS);

        return m1;
    }

    public static short compose(short stat1, short stat2) {
        byte tts = compose(tts(stat1), tts(stat2));
        int changed = stat1 ^ tts<<OFF;
        stat1 ^= changed;
        return stat1;
    }

    public static short invert(short stat) {
        byte tts = tts(stat+2);
        if((tts&5)==4)
            stat ^= 2<<OFF;
        return stat;
    }
}
