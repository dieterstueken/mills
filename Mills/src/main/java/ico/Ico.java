package ico;

/**
 * version:     $Revision$
 * created by:  dst
 * created on:  01.02.2015 11:51
 * modified by: $Author$
 * modified on: $Date$
 */
public class Ico {

    final int i, v,f,e;

    public Ico() {
        this(0, 12, 20, 30);
    }

    public Ico(int i, int v, int f, int e) {
        this.i = i;
        this.v = v;
        this.f = f;
        this.e = e;
    }

    public Ico next() {
        return new Ico(
                i+1,
                v + e,
                f * 4,
                2*e + 3*f);
    }

    @Override
    public String toString() {
        double g = 1<<(2*i);
        return String.format("%8d %8d %8d / %12.1f %12.1f %12.1f %15f.0", v, f, e, (v-2)/g, f/g, e/g, g);
    }

    public static void main(String ... ags) {
        for(Ico ico = new Ico(); ico.i<10; ico=ico.next())
            System.out.println(ico.toString());
    }
}
