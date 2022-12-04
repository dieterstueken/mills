package mills;

import mills.index.builder.IndexGroup;
import mills.index.builder.IndexGroups;

import java.util.function.Supplier;

public class Main {

    public static void main(String ... args) {

        IndexGroups groups = new IndexGroups();

        timer("time", () -> {
            long total = 0;

            for (IndexGroups.Provider provider : groups.providers()) {
                boolean exists = provider.cached() != null;
                IndexGroup group = provider.get();

                total += group.range();

                System.out.format("%s %sgroups: %d\n",
                        group.pop(),
                        exists ? "ready " : "",
                        group.group().size());

                group.group().forEach((clop, c2t) -> {
                    System.out.format("%s: %4d %,13d\n", clop.toString(), c2t.n20(), c2t.range());
                });
            }

            System.out.format("total: %,d\n", total);

            return null;
        });
    }

    static <T> T timer(String name, Supplier<T> proc) {
        double start = System.currentTimeMillis();
        T t = proc.get();
        double stop = System.currentTimeMillis();

        System.out.format("%s: %.3fs\n", name, (stop - start) / 1000);

        return t;
    }
}