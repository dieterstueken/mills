package mills.index;

import mills.bits.PopCount;

import java.util.ServiceLoader;

public interface IndexProvider {

    PosIndex build(PopCount pop);

    static IndexProvider load() {
        ServiceLoader<IndexProvider> services = ServiceLoader.load(IndexProvider.class);
        return services.findFirst().orElse(null);
    }
}
