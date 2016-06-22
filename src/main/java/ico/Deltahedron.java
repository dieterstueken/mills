package ico;

import javafx.collections.FXCollections;
import javafx.collections.ObservableIntegerArray;
import javafx.geometry.Point3D;

import java.io.PrintStream;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * version:     $Revision$
 * created by:  dst
 * created on:  23.04.2015 10:29
 * modified by: $Author$
 * modified on: $Date$
 */
public class Deltahedron {

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

        faces.addAll(e0, e1, e2, 0, 0, 0);
        return faces.size()-6;
    }

    class Divider extends AbstractList<List<Vertex>> {

        final List<List<Vertex>> divisions = new ArrayList<>(edges.size()/2);

        Divider(ObservableIntegerArray edges, int divs) {
            for(int i=0; i<edges.size(); i+=2) {
                Vertex v0 = points.get((edges.get(i)));
                Vertex v1 = points.get((edges.get(i+1)));
                divisions.add(divide(v0, v1, divs));
            }
        }

        @Override
        public List<Vertex> get(int index) {
            List<Vertex> division = divisions.get(index/2);

            if((index&1)==0)
                return division;
            else // odd index returns the reverted division.
                return new AbstractList<Vertex>() {

                    @Override
                    public int size() {
                        return division.size();
                    }

                    @Override
                    public Vertex get(int index) {
                        index = division.size() - index -1;
                        return division.get(index);
                    }
                };
        }

        @Override
        public int size() {
            return divisions.size()*2;
        }

        List<Vertex> divide(Vertex v0, Vertex v1, int dvs) {

            if(dvs==0)
                return Collections.singletonList(v0);

            List<Vertex> division = new ArrayList<>(dvs+1);

            division.add(v0);

            if(dvs>1) {
                double t = v0.p.dotProduct(v1.p);
                Point3D pt = v1.p.subtract(v0.p.multiply(t)).normalize();

                double phi = Math.acos(t) / dvs;
                for (int i = 1; i < dvs; ++i) {
                    double arc = i * phi;
                    double s = Math.sin(arc);
                    double c = Math.cos(arc);
                    Point3D pi = pt.multiply(s).add(v0.p.multiply(c));
                    Vertex vi = vertex(pi);
                    division.add(vi);
                }
            }

            division.add(v1);

            return division;
        }
    }

    Deltahedron() {

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

    Deltahedron(Deltahedron src, int divs) {
        points.addAll(src.points);

        Divider div = new Divider(src.edges, divs);

        for(int i=0; i<src.faces.size(); i+=6) {
            int i0 = src.faces.get(i);
            int i1 = src.faces.get(i + 1);
            int i2 = src.faces.get(i + 2);

            List<Vertex> bottom = div.get(i0);
            List<Vertex> left = div.get(i2 ^ 1);
            List<Vertex> right = div.get(i1);

            for (int j = 1; j <= divs; ++j) {
                Vertex vl = left.get(j);
                Vertex vr = right.get(j);
                List<Vertex> top = div.divide(vl, vr, divs - j);
                top.size();
                for (int k = 0; k < top.size(); ++k) {
                    Vertex v0 = top.get(k);
                    Vertex v1 = bottom.get(k);
                    Vertex v2 = bottom.get(k + 1);
                    face(v0.i, v1.i, v2.i);
                }

                bottom = top;
            }
        }
    }

    public Deltahedron divide(int divs) {
        return divs>1 ? new Deltahedron(this, divs) : this;
    }

    void dump(PrintStream out) {

        System.out.format("edges[%d]:\n", edges.size()/2);

        for(int i=0; i<edges.size(); i+=2) {
            int i0 = edges.get(i);
            int i1 = edges.get(i+1);

            System.out.format("e%2d: %d-%d\n", i/2, i0, i1);
        }

        System.out.format("faces[%d]:\n", faces.size()/6);

        for(int i=0; i<faces.size(); i+=6) {

            int e0 = faces.get(i);
            int e1 = faces.get(i+1);
            int e2 = faces.get(i+2);

            int v0 = edges.get(e0);
            int v1 = edges.get(e1);
            int v2 = edges.get(e2);

            System.out.format("f%d: %d-%d-%d %d:%d:%d \n", i/3, v0, v1, v2, e0, e1, e2);
        }
    }

    public static void main(String ... args) {
        Deltahedron h = new Deltahedron();

        h.dump(System.out);

        h = h.divide(5);

        h.dump(System.out);
    }
}
