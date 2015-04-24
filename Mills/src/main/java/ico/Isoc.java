package ico;

import javafx.geometry.Point3D;

/**
 * version:     $Revision$
 * created by:  dst
 * created on:  20.04.2015 15:48
 * modified by: $Author$
 * modified on: $Date$
 */
public class Isoc extends Hedron {

    public Isoc() {

        vertex(new Point3D(1,0,0));
        vertex(new Point3D(-1,0,0));

        vertex(new Point3D(0,1,0));
        vertex(new Point3D(0,-1,0));

        vertex(new Point3D(0,0,1));
        vertex(new Point3D(0,0,-1));

        for(int m=0; m<8; ++m) {

            int i0 = m&1;
            int i1 = 2 + ((m>>1)&1);
            int i2 = 4 + ((m>>2)&1);

            int p = (i0^i1^i2)&1;
            if(p==0)
                face(i0, i1, i2);
            else
                face(i2, i1, i0);
        }
    }

    public static void main(String ... args) {

        Hedron isoc = new Isoc();

        System.out.format("edges[%d]:\n", isoc.edges.size()/2);

        for(int i=0; i<isoc.edges.size(); i+=2) {
            int i0 = isoc.edges.get(i);
            int i1 = isoc.edges.get(i+1);

            System.out.format("e%2d: %d-%d\n", i/2, i0, i1);
        }

        System.out.format("faces[%d]:\n", isoc.faces.size()/3);

        for(int i=0; i<isoc.faces.size(); i+=3) {

            int e0 = isoc.faces.get(i);
            int e1 = isoc.faces.get(i+1);
            int e2 = isoc.faces.get(i+2);

            int v0 = isoc.edges.get(e0);
            int v1 = isoc.edges.get(e1);
            int v2 = isoc.edges.get(e2);

            System.out.format("f%d: %d-%d-%d %d:%d:%d \n", i/3, v0, v1, v2, e0, e1, e2);
        }
    }
}
