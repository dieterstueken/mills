package mills.position;

import mills.ring.RingEntry;
import mills.util.Indexed;

import static mills.position.Positions.SWP;

/**
 * The Enumeration Twist represents a permutation of the three rings.
 * <p>
 * version:     $
 * created by:  d.stueken
 * created on:  29.11.2022 15:55
 * modified by: $
 * modified on: $
 */
public interface Twist extends Indexed, Builder {

    int S = 1;
    int TT = 6;
    int TTS = TT|S;
    int OFF = 3;

    long build(RingEntry r2, RingEntry r0, RingEntry r1, int stat);

    Twist swap();

    default Twist invert() {
        return this;
    }

    default Twist compose(Twist other) {
        int index = Swap.compose(tts(), other.tts());
        return Swap.get(index);
    }

    byte tts();

    default int getIndex() {
        return tts();
    }

    static byte tts(long i201) {
        return (byte) ((i201 >>> OFF) & TTS);
    }

    static Twist get(int perm) {
        return Swap.get(tts(perm));
    }

    Builder I201 = Positions::i201;

    Builder SWAP = (r2, r0, r1, stat) -> Positions.i201(r0, r2, r1, stat^SWP);

    static Builder i021(Builder target) {
        return (r2, r0, r1, stat) -> target.build(r0, r1, r2, stat(stat, 2));
    }

    static Builder i120(Builder target) {
        return (r2, r0, r1, stat) -> target.build(r1, r2, r0, stat(stat, 4));
    }

    static int compose(int stat0, int stat1) {
        return stat(stat0, tts(stat1));
    }

    static int stat(int stat0, int t1) {

        byte t0 = tts(stat0);

        // apply changed bits
        t0 ^= Swap.compose(t0, (byte)t1);
        stat0 ^= t0 << OFF;

        return stat0;
    }
}
