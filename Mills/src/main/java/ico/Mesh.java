package ico;

import static java.lang.Math.sqrt;
import static java.lang.Math.toDegrees;

/**
 * version:     $Revision$
 * created by:  dst
 * created on:  02.02.2015 14:52
 * modified by: $Author$
 * modified on: $Date$
 */
public class Mesh {

    private final Vertex.List vertices;
    private final Face.List faces;

    Mesh(int level) {

        int k = 1<<2*level;

        vertices = new Vertex.List(k);
        faces = new Face.List(20*k);

        double φ = (sqrt(5)+1)/2;

        // a = 2 / sqrt(φ*sqrt(5));
        // a = 2 / sqrt(φ+2);
        // b = a * φ
        // tan α =  a / b = 1/φ = φ-1

        // ri = a/2 / sqrt(3);
        // Ri = ri * φ^2
        // sin β = ri/Ri = 1/φ^2 = 2-φ

        double α = toDegrees(Math.atan(φ-1));
        double β = toDegrees(Math.asin(2-φ));

        /**
         *  -0---4---8---0--
         *  /|\ /|\ /|\ /|\
         *   | 2 | 6 | A | 2
         *  \|/|\|/|\|/|\|/
         *   7 | B | 3 | 7
         *  / \|/ \|/ \|/ \
         *  ---9---1---5---9
         */

        // vertices.add(β + α, 0);   // 0
        // vertices.add(β - α, 0);   // 2

        // for(int i=2; i<6; ++i)
        //    vertices.rotate(i-2, 120);

        /**
         *         0
         *  -*---*---*---*---
         *  /|\2/|\5/|\8/|\2
         *  9|1*3|4*6|7*9|1*
         *  \|/|\|/|\|/|\|/
         *  F*D|C*A|I*G|F*D
         *  /E\|/B\|/H\|/E\
         *  ---*---*---*---*
         *         J
         */

        // top
        faces.add(0, 2, 4, 5, 8, 2);

        // upper row 3x3
        for(int i=0; i<3; ++i) {
            int i0 = 2*i;
            int i1 = i0+1;
            int i2 = (i0+2)%6;
            int i4 = (i0+4)%6;
            int i8 = 10-i2;
            int i6 = 10-i4;

            int f1 = 3*i+1;
            int f2 =  1 + f1;
            int f3 =  1 + f2;
            int f9 =  1 + (f1+7)%9;
            int f4 =  1 + (f1+2)%9;
            int fd = 18 - (f1+4)%9;
            int fc = 18 - (f1+5)%9;

            faces.add(i1, i0, i8, f9, fd, f2);
            faces.add(i1, i2, i0, 0, f1, f3);
            faces.add(i1, i6, i2, f4, f2, fc);
        }

        // mirror
        //for(int i=0; i<10; i++)
        //    mirror(faces.get(19-i));
    }



    /**
     *         0
     *  -*---*---*---*---
     *  /|\2/|\5/|\8/|\2
     *  9|1*3|4*6|7*9|1*
     *  \|/|\|/|\|/|\|/
     *  F*D|C*A|I*G|F*D
     *  /E\|/B\|/H\|/E\
     *  ---*---*---*---*
     *         J
     */

    Face.List getFaces() {

        return new Face.List(20) {
            {
                // top
                faces.add(0, 2, 4, 5, 8, 2);

                // upper row 3x3
                for(int i=0; i<3; ++i) {
                    int i0 = 2*i;
                    int i1 = i0+1;
                    int i2 = (i0+2)%6;
                    int i4 = (i0+4)%6;
                    int i8 = 10-i2;
                    int i6 = 10-i4;

                    int f1 = 3*i+1;
                    int f2 =  1 + f1;
                    int f3 =  1 + f2;
                    int f9 =  1 + (f1+7)%9;
                    int f4 =  1 + (f1+2)%9;
                    int fd = 18 - (f1+4)%9;
                    int fc = 18 - (f1+5)%9;

                    faces.add(i1, i0, i8, f9, fd, f2);
                    faces.add(i1, i2, i0, 0, f1, f3);
                    faces.add(i1, i6, i2, f4, f2, fc);
                }
            }

            Face mirror(Face f) {
                return add(11-f.p0, 11-f.p2, 11-f.p1, 19-f.f0, 19-f.f2, 19-f.f0);
            }
        };
    }

    public static void main(String ... args) {
        Mesh m = new Mesh(0);
    }
}
