package mills.bits;

import java.util.List;

/**
 * version:     $
 * created by:  d.stueken
 * created on:  29.11.2022 21:38
 * modified by: $
 * modified on: $
 */
class Swapped extends Twist {

    final Twist S;

    @Override
    public Twist invert() {
        // 0,2,4 -> 0,4,2
        if(msk>0)
            return VALUES.get(msk^0x6);
        return this;
    }

    private Swapped(int msk, int i0, int i1, int i2) {
        super(msk, i0, i1, i2);

        S = new Twist(msk+1, i2, i1, i0) {
            @Override
            public Swapped swap() {
                return Swapped.this;
            }
        };

    }

    @Override
    public Twist swap() {
        return S;
    }

    public static final Swapped T0 = new Swapped(0,0,1,2);
    public static final Swapped T1 = new Swapped(2,1,2,0);
    public static final Swapped T2 = new Swapped(4,2,0,1);

    public static final List<Twist> VALUES = List.of(T0, T0.S, T1, T1.S, T2, T2.S);

    public static Twist twist(int i) {
        return VALUES.get(i);
    }

    public static int compose(int m0, int m1) {
        int m = m0;

        if((m&1)==0)
            m += m1&6;
        else
            m += 6-(m1&6);

        m %= 6;
        m ^= m1&1;

        return m;
    }
}
