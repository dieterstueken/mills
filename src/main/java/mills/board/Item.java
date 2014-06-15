package mills.board;

import com.google.common.collect.ImmutableList;

import java.awt.*;
import java.awt.geom.Point2D;

/*
* version        :  $Revision: $
* created by     :  dst
* date created   :  06.10.2010, 12:34:07
* last mod by    :  $Author: $
* date last mod  :  $Date: $
*
*/

/**
 * An Item is something which can be drawn at a board position.
 * All Items have a virtual RADIUS of 10 (normalized pixels)
 * resp. a diameter of 20.
 */
abstract class Item {
    public static final int RADIUS = 10;

    public static final Item BLACK = new Stone(Color.black, Color.darkGray);
    public static final Item WHITE = new Stone(Color.white, Color.lightGray);

    public static final Item RED = new Outline(Color.red);
    public static final Item GREEN = new Outline(Color.green);

    public static final Item NOTHING = new Item() {
        void draw(Graphics2D g){}
        void draw(Graphics2D g, double x, double y) {}
    };

    // draw this item to a normalized Graphics2D at (0,0)
    abstract void draw(Graphics2D g);

    void draw(Graphics2D g, Point2D pos) {
        draw(g, pos.getX(), pos.getY());
    }

    void draw(Graphics2D g, double x, double y) {

        final Rectangle clip = g.getClipBounds();

        // see if it has to be painted at all
        if(x+RADIUS < clip.x) return;
        if(y+RADIUS < clip.y) return;
        if(x-RADIUS > clip.x+clip.width) return;
        if(y-RADIUS > clip.y+clip.height) return;

        g = (Graphics2D) g.create();

        try {
            g.translate(x, y);
            draw(g);
        } finally {
            g.dispose();
        }
    }

    static class Stone extends Item {
        public static final java.util.List<Integer> SIZES = ImmutableList.of(9,7,6,4,3);
        final Color c1, c2;

        Stone(Color c1, Color c2) {
            this.c1 = c1;
            this.c2 = c2;
        }

        void draw(Graphics2D g) {
            int i=0;
            for(final Integer s:SIZES) {
                Color c = (i++%2)==0 ? c1 :c2;
                g.setColor(c);
                g.fillOval(-s, -s, 2*s, 2*s);
            }
        }
    }

    static class Outline extends Item {
        static final int r = 2;

        final Color c;

        public Outline(final Color c) {
            this.c = c;
        }

        void draw(Graphics2D g) {
            g.setColor(c);
            g.fillOval(-r, -r, 2*r, 2*r);
        }
    }
}
