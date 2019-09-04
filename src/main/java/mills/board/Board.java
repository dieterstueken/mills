package mills.board;

import mills.bits.Player;
import mills.bits.Ring;
import mills.bits.Sector;
import mills.ring.RingEntry;

import javax.swing.*;
import javax.swing.event.MouseInputAdapter;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.geom.Line2D;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 25.09.2010
 * Time: 18:19:07
 */
public class Board extends JPanel {

    static final Color BOARD_COLOR = new Color(248, 213, 131);

    static final int RADIUS = 100;
    private static final BasicStroke LINK_STROKE = new BasicStroke(2, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_MITER);

    class Position {
        final Ring ring;
        final Sector sector;
        final Point point;

        Position(Ring ring, Sector sector) {
            this.ring = ring;
            this.sector = sector;
            final int size = 27 * ring.radius;
            final int x = sector.x() - 1;
            final int y = sector.y() - 1;
            this.point = new Point(RADIUS + size*x, RADIUS + size*y);
        }

        void draw(final Graphics2D g) {
            switch(player()) {
                case White:
                    Item.WHITE.draw(g, point);
                    break;
                case Black:
                    Item.BLACK.draw(g, point);
                    break;
            }

            if(drag.dest==this)
                Item.RED.draw(g, point);
            else
            if(active.contains(this))
                 Item.GREEN.draw(g, point);
        }

        void repaint() {
            double scale = getScale();
            repaintAt(scale*point.x, scale*point.y);
        }

        public Player player() {
            return stones.getPlayer(ring, sector);
        }

        public void player(Player player) {
            stones.setPlayer(ring, sector, player);
        }

        boolean touches(final Drag drag) {
            return drag.touches(point.x, point.y);
        }
    }

    public class Drag extends MouseInputAdapter {

        Position src = null;
        Position dest = null;

        Point point = new Point();

        boolean touches(double x, double y) {
            double scale = getScale();
            x -= point.x/scale;
            y -= point.y/scale;
            double d2 = x*x + y*y;
            System.out.format("dist: %f\n", d2);
            return d2 < Item.RADIUS * Item.RADIUS;
        }

        private void setDest(final Position p) {
            if(dest!=p) {
                repaint(dest);
                repaint(p);
                dest = p;
            }
        }

        void setMousePosition(String what, MouseEvent e) {
            repaintAt(point.x, point.y);

            point.x = e.getX();
            point.y = e.getY();

            repaintAt(point.x, point.y);

            for(final Position p:active) {
                if(p.touches(this)) {
                    setDest(p);
                    return;
                }
            }

            setDest(src);
        }

        public void mouseMoved(MouseEvent e) {
            setMousePosition("move", e);
        }

        public void mousePressed(MouseEvent e) {
            setMousePosition("press", e);
        }

        public void mouseReleased(MouseEvent e) {
            setMousePosition("release", e);
        }

        public void mouseDragged(MouseEvent e) {
            setMousePosition("drag", e);
        }

        void draw(final Graphics2D g) {

        }
    }

    final Stones stones = new Stones(RingEntry.of(1234), RingEntry.of(4567), RingEntry.of(42));
    final List<Position> positions = new ArrayList<>(24);
    final Drag drag = new Drag();
    final List<Line2D> links = new ArrayList<>(32);

    final Set<Position> active = new HashSet<>();

    Position position(Ring ring, Sector sector) {
        final int i = 8*ring.ordinal() + sector.ordinal();
        return positions.get(i);
    }

    /**
     * trigger repainting for a region around x,y in current scale.
     * @param x coordinate
     * @param y coordinate
     */
    void repaintAt(double x, double y) {
        double radius = Item.RADIUS*getScale();
        x -= radius;
        y -= radius;
        int d = (int)Math.ceil(2*radius);
        repaint((int) x, (int) y, d, d);
    }

    void repaint(final Position p) {
        if(p!=null)
            p.repaint();
    }

    Board() {
        setOpaque(true);
        setBackground(BOARD_COLOR);
        setPreferredSize(new Dimension(2 * RADIUS, 2 * RADIUS));

        for(final Ring r:Ring.values()) {
            for (final Sector s:Sector.values()) {
                final Position position = new Position(r, s);
                positions.add(position);
            }
        }

        for(final Ring r:Ring.values()) {
            for (final Sector s0:Sector.CORNERS) {
                final Sector s1 = s0.rotate(1);
                addLink(position(r, s0), position(r, s1));
            }
        }

        for(final Sector s:Sector.EDGES) {
            addLink(position(Ring.OUTER, s), position(Ring.INNER, s));
        }

        for(final Position p:positions)
            //if(p.player()==Player.White)
                active.add(p);

        addMouseListener(drag);
        addMouseMotionListener(drag);
    }

    private void addLink(Position p1, Position p2) {
        final Line2D link = new Line2D.Double(p1.point, p2.point);
        links.add(link);
    }

    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        g = g.create();
        try {
            Graphics2D g2 = (Graphics2D) g;

            RenderingHints rh = new RenderingHints(
                    RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHints(rh);

            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_DEFAULT);

            double scale = getScale();
            g2.scale(scale, scale);
            paint2D(g2);
        } finally {
            g.dispose();
        }
    }

    double getScale() {
        double diameter = Math.min(getSize().width, getSize().height);
        double scale = diameter / RADIUS / 2;
        return scale;
    }

    public void paint2D(Graphics2D g) {
        
        final Rectangle clip = g.getClipBounds();

        System.out.format("update: %4d %4d %4d %4d\n", clip.x, clip.y, clip.width, clip.height);

        g.setColor(Color.black);
        g.setStroke(LINK_STROKE);

        for (final Line2D link : links)
            g.draw(link);
/*
        g.setColor(Color.blue);
        g.setStroke(new BasicStroke(0));
        g.draw(new Ellipse2D.Double(clip.x, clip.y, clip.width, clip.height));
*/

        for(final Position p:positions)
            p.draw(g);

        drag.draw(g);
    }

    public static void main(String... args) {
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                createAndShowGUI();
            }
        });
    }

    private static void createAndShowGUI() {
        final JFrame frame = new JFrame("Nine Men's Morris");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);


        //Create the menu bar.  Make it have a green background.
        JMenuBar menuBar = new JMenuBar();
        menuBar.setOpaque(true);
        menuBar.setBackground(new Color(154, 165, 127));
        menuBar.setPreferredSize(new Dimension(200, 20));
        frame.setJMenuBar(menuBar);

        Board board = new Board();

        frame.getContentPane().add(board, BorderLayout.CENTER);
        frame.pack();
        frame.setVisible(true);
    }

}
