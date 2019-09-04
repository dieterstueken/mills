package mills.index1.partitions;

import mills.ring.RingEntry;
import mills.util.AbstractRandomList;

import java.util.List;
import java.util.function.Predicate;

/**
 * A Predicate to filter RingEntries for a given restriction mask.
 * The restriction mask indicates all bits for which a given RingEntry must NOT be minimized.
 */
class PartitionFilter implements Predicate<RingEntry> {

    public static final List<PartitionFilter> FILTERS = generate();

    static PartitionFilter of(int index) {
        return FILTERS.get(index);
    }

    // mask of restricted min bits.
    final int restriction;

    /**
     * Return if the given RingEntry is compatible to the given mask.
     * The mask will tag permutations for which the i20 candidate will decrease (mlt).
     * The given RingEntry is compatible, if i20 won't decrease for any stable permutation.
     * @param entry to analyze.
     * @return if the entry matches.
     */
    public boolean test(final RingEntry entry) {
        return (entry.pmin() & restriction) == 0;
    }

    PartitionFilter(int index) {
        // bit #0 is irrelevant
        this.restriction = 2*index;
    }

    /**
     * @return a virtual table of 128 MskFilter.
     */
    private static List<PartitionFilter> generate() {
        return AbstractRandomList.generate(128, PartitionFilter::new);
    }
}
