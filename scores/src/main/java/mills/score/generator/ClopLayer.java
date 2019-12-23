package mills.score.generator;

import mills.bits.Clops;
import mills.bits.PopCount;

public interface ClopLayer extends Layer, Clops {

    PopCount clop();
}
