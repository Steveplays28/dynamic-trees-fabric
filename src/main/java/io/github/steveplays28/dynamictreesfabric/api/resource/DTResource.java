package io.github.steveplays28.dynamictreesfabric.api.resource;

import java.util.function.Function;
import net.minecraft.util.Identifier;

/**
 * Container for a resource object that is keyed by its location.
 *
 * @param <R> the type of the resource object
 * @author Harley O'Connor
 */
public final class DTResource<R> {

    private final Identifier location;
    private final R resource;

    public DTResource(Identifier location, R resource) {
        this.location = location;
        this.resource = resource;
    }

    public <N> DTResource<N> map(Function<R, N> mapper) {
        return new DTResource<>(this.location, mapper.apply(this.resource));
    }

    public Identifier getLocation() {
        return location;
    }

    public R getResource() {
        return resource;
    }

}
