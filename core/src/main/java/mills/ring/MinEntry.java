package mills.ring;

import mills.bits.Perm;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 30.07.24
 * Time: 20:51
 */
abstract public class MinEntry extends RingEntry {

    protected MinEntry(final short index, final byte meq, final byte mlt,
                               final byte pmin, final byte mix, final short[] perm) {
                super(index, meq, mlt, pmin, mix, perm);
                assert mlt==0;
                assert pmin==meq;
                assert mix==0;
            }

            @Override
            public boolean isMin() {
                return true;
            }

            @Override
            public short min() {
                return index;
            }

            @Override
            public RingEntry minimized() {
                return this;
            }

            @Override
            public Perm pmix() {
                return Perm.R0;
            }
}
