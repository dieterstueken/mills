package mills.score.generator;

import mills.bits.IClops;
import mills.bits.PopCount;

public interface ClopLayer extends Layer, IClops {

    PopCount clop();
}
