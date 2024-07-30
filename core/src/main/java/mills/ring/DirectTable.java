package mills.ring;

import mills.util.listset.DirectListSet;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 30.07.24
 * Time: 20:12
 */
public interface DirectTable extends EntryTable, DirectListSet<RingEntry> {

    @Override
    default int findIndex(int ringIndex) {
        return DirectListSet.super.findIndex(ringIndex);
    }

    @Override
    DirectTable headSet(RingEntry toElement);

    @Override
    DirectTable headSet(int size);
}
