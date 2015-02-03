package ico;

import java.util.ArrayList;

import static java.lang.Math.sqrt;
import static java.lang.Math.toDegrees;

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

    static final double φ = (sqrt(5)+1)/2;

    // a = 2 / sqrt(φ*sqrt(5));
    // a = 2 / sqrt(φ+2);
    // b = a * φ
    // tan α =  a / b = 1/φ = φ-1

    // ri = a/2 / sqrt(3);
    // Ri = ri * φ^2
    // sin β = ri/Ri = 1/φ^2 = 2-φ

    static final double α = toDegrees(Math.atan(φ-1));
    static final double β = toDegrees(Math.asin(2-φ));

    /**
     *  -0---2---4---0--
     *  /|\ /|\ /|\ /|\
     *   | B | 7 | 9 | B
     *  \|/|\|/|\|/|\|/
     *   6 | 8 | A | 6
     *  / \|/ \|/ \|/ \
     *  ---5---1---3---5
     */

    public static class List extends ArrayList<Vertex> {

        public List(int level) {
            super(10*(1<<2*level)+2);

            add6(β + α, 0); // top bottom
            add6(β - α, 0); // upper lower

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

        public void add6(double th, double ph) {
            add2(th, ph);
            add2(th, ph+120);
            add2(th, ph+240);
        }

        void init(int level) {

        }
    }
}
