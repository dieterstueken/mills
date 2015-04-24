package ico;

import javafx.geometry.Point3D;

/**
 * version:     $Revision$
 * created by:  dst
 * created on:  22.04.2015 15:48
 * modified by: $Author$
 * modified on: $Date$
 */
class Vertex implements Comparable<Vertex> {

    final int i;
    final Point3D p;

    Vertex(int i, Point3D p) {
        this.i = i;
        this.p = p;
    }

    @Override
    public int compareTo(Vertex o) {
        return Integer.compare(i, o.i);
    }
}
