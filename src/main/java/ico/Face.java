package ico;

import java.util.ArrayList;


class Face {

    final int p0, p1, p2;
    final int f0, f1, f2;

    public Face(int p0, int p1, int p2, int f0, int f1, int f2) {
        this.p0 = p0;
        this.p1 = p1;
        this.p2 = p2;
        this.f0 = f0;
        this.f1 = f1;
        this.f2 = f2;
    }

    public String toString() {
        return String.format("%d,%d,%d %d %d %d", p0,p1,p2, f0,f1,f2);
    }

    public static class List extends ArrayList<Face> {
        public List(int initialCapacity) {
            super(initialCapacity);
        }

        public Face add(int p0, int p1, int p2, int f0, int f1, int f2) {
            Face face = new Face(p0,p1,p2, f0,f1,f2);
            add(face);
            return face;
        }
    }
}
