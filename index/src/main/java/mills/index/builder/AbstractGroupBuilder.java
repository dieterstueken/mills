package mills.index.builder;

import mills.index.IndexProvider;
import mills.ring.Entries;
import mills.ring.EntryTable;

import java.util.concurrent.ForkJoinTask;

/**
 * version:     $
 * created by:  d.stueken
 * created on:  02.03.2022 13:31
 * modified by: $
 * modified on: $
 */
public abstract class AbstractGroupBuilder implements IndexProvider {

    final Partitions partitions;
    final PopMap<EntryTable> lePops;
    final PopMap<EntryTable> minPops;

    public AbstractGroupBuilder() {
        this.lePops = PopMap.lePops(Entries.TABLE);
        this.minPops = PopMap.lePops(Entries.MINIMIZED);

        var task = ForkJoinTask.adapt(Partitions::new).fork();
        this.partitions = task.join();
    }
}
