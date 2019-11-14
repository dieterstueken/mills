package mills.score.opening;

import mills.index.IndexProvider;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 12.11.19
 * Time: 17:58
 */
abstract class PlopLayer extends PlopSets {

    PlopLayer(IndexProvider indexes, int layer) {
        super(indexes, layer);
    }

    protected PlopLayer(PlopLayer parent) {
        super(parent);
    }

    protected void elevate(PlopSet plops) {
        try(PlopMover mover = elevator(plops)) {
            plops.process(mover);
        }
    }

    abstract protected PlopMover elevator(PlopSet source);
}
