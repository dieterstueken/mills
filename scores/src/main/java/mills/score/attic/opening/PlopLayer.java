package mills.score.attic.opening;

import mills.index.IndexProvider;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 12.11.19
 * Time: 17:58
 */
abstract class PlopLayer extends PlopSets {

    PlopLayer(IndexProvider indexes, Plop plop) {
        super(indexes, plop);
    }

    protected PlopLayer(PlopLayer parent) {
        super(parent);
    }

    abstract protected void trace(MovedLayer source, PlopSet tgt);

    public void show() {
        for (PlopSet ps : plops.values()) {
            System.out.format("%c %s[%s] %d/%d\n", getClass().getSimpleName().charAt(0),
                    ps.pop(), ps.clop(),
                    ps.set.cardinality(), ps.index.range());
        }
    }
}
