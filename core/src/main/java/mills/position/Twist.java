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
        int index = compose(tts(), other.tts());
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

    static Builder i012(Builder target) {
        return (r2, r0, r1, stat) -> target.build(r0, r1, r2, applyTTS(stat, (byte)2));
    }

    static Builder i120(Builder target) {
        return (r2, r0, r1, stat) -> target.build(r1, r2, r0, applyTTS(stat, (byte)4));
    }

    default int applyTo(int perm) {
        return applyTTS(perm, this.tts());
    }

    static int compose(int stat0, int stat1) {
        return applyTTS(stat0, tts(stat1));
    }

    static int invert(int stat) {
        int tts = tts(stat)+2;

        // tts == 2 | 4: swap
        if((tts&6)==4)
            stat ^= 2<<OFF;

        return stat;
    }

    static int applyTTS(int stat0, byte tt1) {

        byte tt0 = tts(stat0);

        // apply changed bits
        tt0 ^= Swap.compose(tt0, tt1);
        stat0 ^= tt0 << OFF;

        return stat0;
    }
}
