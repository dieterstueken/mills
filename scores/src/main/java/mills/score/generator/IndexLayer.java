package mills.score.generator;

import mills.bits.PopCount;
import mills.index.PosIndex;

public interface IndexLayer extends ClopLayer {

    PosIndex index();

    default PopCount pop() {
        return index().pop();
    }

    default PopCount clop() {
        return index().clop();
    }
}
