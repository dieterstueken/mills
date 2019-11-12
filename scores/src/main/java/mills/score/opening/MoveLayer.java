package mills.score.opening;

import mills.index.IndexProvider;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 12.11.19
 * Time: 22:04
 */
public class MoveLayer extends PlopLayer {

    final CloseLayer closed;

    MoveLayer(IndexProvider indexes, int layer) {
        super(indexes, layer);
        closed = new CloseLayer(this);
    }

    void elevate(PlopLayer source) {

        for (PlopSet value : source.plops.values()) {
            new PlopMover(value, closed).run();
        }

        for (PlopSet value : closed.plops.values()) {
            new PlopCloser(value, this).run();
        }
    }
}
