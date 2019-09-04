package mills.board;

import mills.bits.Ring;
import mills.bits.Sector;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 03.10.2010
 * Time: 17:38:50
 */
class Locations {

    static final int RADIUS = 100;

    final List<Ring2D> board = new ArrayList<>(3);

    class Ring2D {

        final List<Point> points = new ArrayList<>(8);

        public Point position(final Sector s) {
            return points.get(s.ordinal());
        }

        Ring2D(final Ring  ring) {
            final int size = 30 * ring.radius;
            for (final Sector sector : Sector.values()) {
                final int x = sector.x() - 1;
                final int y = sector.y() - 1;
                final Point point = new Point(RADIUS + size * x, RADIUS + size * y);
                points.add(point);
            }
        }
    }

    Locations() {
        for(final Ring r:Ring.values()) {
            board.add(new Ring2D(r));
        }
    }

    Ring2D ring(final Ring ring) {
        return board.get(ring.ordinal());
    }

    Point position(final Ring r, final Sector s) {
        return ring(r).position(s);
    }
}
