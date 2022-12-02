package mills.position;

import mills.ring.RingEntry;

import java.util.List;

/**
 * version:     $
 * created by:  d.stueken
 * created on:  29.11.2022 21:38
 * modified by: $
 * modified on: $
 */
public enum Swap implements Twist {

    T0(I201),
    S0(SWAP),
    T1(Twist.i021(I201)) {
        @Override
        public Twist invert() {
            return T2;
        }
    },
    S1(Twist.i021(SWAP)),
    T2(Twist.i120(I201)) {
        @Override
        public Twist invert() {
            return T1;
        }
    },
    S2(Twist.i120(I201));

    final Builder next;

    Swap(Builder next) {
        this.next = next;
    }

    @Override
    public Twist swap() {
        return VALUES.get(ordinal()^1);
    }

    public long build(RingEntry r2, RingEntry r0, RingEntry r1, int stat) {
        return next.build(r2, r0, r1, stat);
    }

    @Override
    public byte tts() {
        return (byte) ordinal();
    }

    public static final List<Swap> VALUES = List.of(values());

    public static Swap get(int i) {
        return VALUES.get(i);
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
}
