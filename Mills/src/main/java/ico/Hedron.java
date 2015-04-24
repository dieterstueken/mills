package ico;

import javafx.collections.FXCollections;
import javafx.collections.ObservableIntegerArray;
import javafx.geometry.Point3D;

import java.util.ArrayList;
import java.util.List;

/**
 * version:     $Revision$
 * created by:  dst
 * created on:  23.04.2015 10:29
 * modified by: $Author$
 * modified on: $Date$
 */
public class Hedron {

    final List<Vertex> points = new ArrayList<>(6);

    final ObservableIntegerArray edges = FXCollections.observableIntegerArray();

    final ObservableIntegerArray faces = FXCollections.observableIntegerArray();

    Vertex vertex(Point3D point) {
        int index = points.size();
        Vertex v = new Vertex(index, point);
        points.add(v);
        return v;
    }

    /**
     * Find given edge or create a new one.
     * The returned index is odd of the edge is inverted.
     *
     * @param i0 first vertex index
     * @param i1 first vertex index
     * @return edge index.
     */
    int edge(int i0, int i1) {

        int n = edges.size();

        for(int i=0; i<n; i+=2) {
            int k0 = edges.get(i);
            int k1 = edges.get(i+1);

            if(k0==i0 && k1==i1)
                return i;

            if(k0==i1 && k1==i0)
                return i+1;
        }

        edges.addAll(i0, i1);

        return n;
    }

    /**
     * Add a new triangle of three vertices.
     * @param i0 first vertex index
     * @param i1 second vertex index
     * @param i2 third vertex index
     * @return the face index (*3)
     */
    int face(int i0, int i1, int i2) {

        int e0 = edge(i0, i1);
        int e1 = edge(i1, i2);
        int e2 = edge(i2, i0);

        faces.addAll(e0, e1, e2);
        return faces.size()-3;
    }
}
