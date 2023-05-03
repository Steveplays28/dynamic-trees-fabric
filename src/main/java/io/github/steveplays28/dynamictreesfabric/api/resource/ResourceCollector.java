package io.github.steveplays28.dynamictreesfabric.api.resource;

import java.util.function.Supplier;

import com.google.common.collect.Maps;

import net.minecraft.util.Identifier;

/**
 * @author Harley O'Connor
 */
public interface ResourceCollector<R> {

	static <R> ResourceCollector<R> unordered() {
		return new SimpleResourceCollector<>(Maps::newHashMap);
	}

	static <R> ResourceCollector<R> ordered() {
		return new SimpleResourceCollector<>(Maps::newLinkedHashMap);
	}

	DTResource<R> put(DTResource<R> resource);

	DTResource<R> computeIfAbsent(Identifier key, Supplier<DTResource<R>> resourceSupplier);

	ResourceAccessor<R> createAccessor();

	void clear();

}
