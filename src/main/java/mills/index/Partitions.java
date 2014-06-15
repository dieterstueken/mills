package mills.index;

import mills.bits.PopCount;
import mills.index.partitions.LePopTable;
import mills.index.partitions.PartitionTables;
import mills.util.AbstractRandomList;

import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.RecursiveTask;
import java.util.function.Supplier;

/**
 * version:     $Revision$
 * created by:  dst
 * created on:  13.12.12 12:48
 * modified by: $Author$
 * modified on: $Date$
 */
public class Partitions extends AbstractRandomList<Supplier<R2Table>> {

    public final PartitionTables partitions;
    public final LePopTable lePopTable;

    public Partitions(PartitionTables partitions, LePopTable lePopTable) {
        this.lePopTable = lePopTable;
        this.partitions = partitions;
    }

    public static Partitions open() {
        return BUILDER.join();
    }

    private static final ForkJoinTask<Partitions> BUILDER = new  RecursiveTask<Partitions>() {

        @Override
        protected Partitions compute() {
            ForkJoinTask<PartitionTables> pt = new PartitionTables.Builder().fork();
            LePopTable lePopTable = LePopTable.open();
            PartitionTables partitions = pt.join();
            return new Partitions(partitions, lePopTable);
        }
    }.fork();

    @Override
    public int size() {
        return PopCount.SIZE;
    }

    @Override
    public Supplier<R2Table> get(int index) {
        return get(PopCount.get(index));
    }

    public Supplier<R2Table> get(final PopCount pop) {
        return () -> builder(pop).build();
    }

    private T2Builder builder(final PopCount pop) {
        return new T2Builder(pop) {

            @Override
            protected T0Builder newBuilder() {
                return new T0Builder(partitions, lePopTable);
            }
        };
    }
}
