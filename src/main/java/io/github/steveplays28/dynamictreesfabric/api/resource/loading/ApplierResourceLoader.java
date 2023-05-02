package io.github.steveplays28.dynamictreesfabric.api.resource.loading;

import io.github.steveplays28.dynamictreesfabric.api.treepacks.ApplierRegistryEvent;
import net.minecraftforge.fml.ModLoader;

/**
 * @author Harley O'Connor
 */
public interface ApplierResourceLoader<P> extends ResourceLoader<P> {

    void registerAppliers();

    static void postApplierEvent(ApplierRegistryEvent<?, ?> event) {
        ModLoader.get().postEvent(event);
    }

}
