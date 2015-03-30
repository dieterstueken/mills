package mills.partitions;

import mills.ring.EntryTable;

import java.util.Collections;
import java.util.List;

/**
* Created by IntelliJ IDEA.
* User: stueken
* Date: 12/27/14
* Time: 1:38 PM
*/
public class Partition {

    public interface Group {

        default EntryTable root() {
            return EntryTable.EMPTY;
        }

        default EntryTable get(int rad, int clop) {
            return EntryTable.EMPTY;
        }

        default int count() {
            return 0;
        }

        public static final Group EMPTY = new Group() {};
    }

    final List<Group> groups;

    final List<Group> gset;

    public Partition(List<Group> groups, List<Group> gset) {
        this.groups = groups;
        this.gset = gset;
    }

    public EntryTable root() {
        return group(0).root();
    }

    public Group group(int msk) {
        return groups.get(msk);
    }

    public List<? extends Group> gset() {
        return gset;
    }

    public static final Partition EMPTY = new Partition(Collections.nCopies(128, Group.EMPTY), Collections.emptyList());
}
