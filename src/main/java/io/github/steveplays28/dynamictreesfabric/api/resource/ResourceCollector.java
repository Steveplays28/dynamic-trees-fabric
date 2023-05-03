package io.github.steveplays28.dynamictreesfabric.api.resource;

import com.google.common.collect.Maps;
import java.util.function.Supplier;
import net.minecraft.util.Identifier;

/**
 * @author Harley O'Connor
 */
public interface ResourceCollector<R> {

    DTResource<R> put(DTResource<R> resource);

    DTResource<R> computeIfAbsent(Identifier key, Supplier<DTResource<R>> resourceSupplier);

    ResourceAccessor<R> createAccessor();

    void clear();

    static <R> ResourceCollector<R> unordered() {
        return new SimpleResourceCollector<>(Maps::newHashMap);
    }

    static <R> ResourceCollector<R> ordered() {
        return new SimpleResourceCollector<>(Maps::newLinkedHashMap);
    }

}
