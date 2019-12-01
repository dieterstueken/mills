package mills.score.opening;

import mills.bits.Clops;
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

    abstract protected void trace(MovedLayer source, PlopSet tgt);

    protected void trace(MovedLayer src) {
        forEach(tgt -> trace(src, tgt));
    }

    public void show() {
        for (Clops clops : plops.keySet()) {
            System.out.format("%c %s[%s]\n", getClass().getSimpleName().charAt(0), clops.pop(), clops.clop());
        }
    }
}
