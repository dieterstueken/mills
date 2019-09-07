package mills.bits;

import java.util.List;
import java.util.function.UnaryOperator;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 07.09.19
 * Time: 17:27
 */
public class SectorOperations implements SectorOperation {

    final String name;

    final UnaryOperator<Sector> map;

    SectorOperations(String name, UnaryOperator<Sector> map) {
        this.name = name;
        this.map = map;
        assert SectorOperation.verify(map);
    }

    @Override
    public String toString() {
        return name;
    }

    @Override
    public boolean equals(Object other) {
        if(this==other)
            return true;

        if(other instanceof SectorOperation) {
            SectorOperation op = (SectorOperation) other;
            return SectorOperation.equals(this::map, op::map);
        }

        return false;
    }

    @Override
    public Sector map(Sector s) {
        return map.apply(s);
    }

    public SectorOperation invert() {
        return this;
    }

    static final SectorOperations MOP = new SectorOperations("M", SectorOperation.MOP);

    static final SectorOperations NOP = new SectorOperations("I", UnaryOperator.identity());

    static final SectorOperations ROP = new SectorOperations("R", SectorOperation.ROP) {
        @Override
        public SectorOperation invert() {
            return LOP;
        }
    };

    static final SectorOperations XOP = new SectorOperations("X", ROP.join(ROP)::map);

    static final SectorOperations LOP = new SectorOperations("L", XOP.join(ROP)::map) {
        @Override
        public SectorOperation invert() {
                    return ROP;
                }
    };

    static final List<SectorOperations> ROTATE = List.of(NOP, ROP, XOP, LOP);
}
