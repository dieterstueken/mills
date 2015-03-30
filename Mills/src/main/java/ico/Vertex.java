package ico;

import java.util.ArrayList;

/**
 *   R  = a * sqrt(10+2*sqrt(5))/4 = a * 0.951
 *   Ri = a * sqrt(3)*(3+sqrt(5))/12 = a * 0.75576
 *
 *   edge:
 *   a = R * 4/sqrt(10+2*sqrt(5)) =  1.05146
 *
 *   face:
 *   ro = a/sqrt(3)
 *   ri = ro/2
 *   h = ri + ro
 *
 *   spherical angles:
 *   cos α = sqrt(1/5) = 0.4472159342
 *   sin α = sqrt(4/5) = 0.894427191
 *   α = 63.4348°
 *
 *   sin ϱi = ri/R = 2 / sqrt(3*(10*2*sqrt(5))) = 0.172668
 *   ϱi = 9.943°
 *
 *   sin ϱo = ro / R = 2*ri/R = 0.345336
 *   ϱo = 20.2°
 */

class Vertex {

    final double th;

    final double ph;

    public Vertex(double th, double ph) {
        this.ph = Math.IEEEremainder(ph, 360);
        this.th = th;
    }

    public String toString() {
        return String.format("%10.2f %10.2f", th, ph);
    }

    /**
     * 2φ-1 = sqrt(5)
     *
     * 1/φ = φ-1
     * φ^2 = φ+1
     * φ^3 = 2φ+1
     */

    //static final double φ = (sqrt(5)+1)/2;

    static final double t0 = 90-Math.toDegrees(Math.acos(-3.0/5))/2;

    /**
     *             0
     *   |   |   |   |   |   |
     * --2---4---6---8---A---2---  t0
     * \/ \ / \ / \ / \ / \ / \/
     * 7---9---B---3---5---7---9-
     * |   |   |   |   |   |   |
     *             1
     */

    public static class List extends ArrayList<Vertex> {

        public List(int level) {
            super(10*(1<<2*level)+2);

            add2(0, 0);

            for(int i=0; i<5; ++i)
                add2(t0, 72*i);

            if(level>0)
                init(level-1);
        }

        public void add(double th, double ph) {
            Vertex v = new Vertex(th, ph);
            add(v);
        }

        public void add2(double th, double ph) {
            add(th, ph);
            add(-th, ph+180);
        }


        void init(int level) {

        }
    }
}
