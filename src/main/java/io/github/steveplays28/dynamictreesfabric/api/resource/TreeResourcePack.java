package io.github.steveplays28.dynamictreesfabric.api.resource;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Set;
import java.util.function.Predicate;
import net.minecraft.resource.ResourcePack;
import net.minecraft.util.Identifier;

/**
 * A {@linkplain ResourcePack resource pack} that reads from the {@code trees} folder.
 *
 * @author Harley O'Connor
 */
public interface TreeResourcePack extends ResourcePack {

    String FOLDER = "trees";

    @SuppressWarnings("ConstantConditions")
    default InputStream getResource(Identifier location) throws IOException {
        return this.open(null, location);
    }

    @SuppressWarnings("ConstantConditions")
    default Collection<Identifier> getResources(String namespace, String path,
                                                      Predicate<Identifier> filter) {
        return this.findResources(null, namespace, path, filter);
    }

    @SuppressWarnings("ConstantConditions")
    default boolean hasResource(Identifier location) {
        return this.hasResource(null, location);
    }

    @SuppressWarnings("ConstantConditions")
    default Set<String> getNamespaces() {
        return this.getNamespaces(null);
    }

}
