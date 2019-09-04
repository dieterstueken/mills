package mills.index;

import mills.bits.PopCount;

import java.util.List;
import java.util.ServiceLoader;

public interface IndexProvider extends List<PosIndex> {

    PosIndex get(int index);

    default int size() {
        return 0;
    }

    default PosIndex get(PopCount pop) {
        return get(pop.index);
    }

    static IndexProvider load() {
        ServiceLoader<IndexProvider> services = ServiceLoader.load(IndexProvider.class);
        return services.findFirst().orElse(null);
    }
}
