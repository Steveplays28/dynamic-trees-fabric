package io.github.steveplays28.dynamictreesfabric.api.resource;

import java.util.Map;
import java.util.function.Supplier;

import net.minecraft.util.Identifier;

/**
 * @author Harley O'Connor
 */
final class SimpleResourceCollector<R> implements ResourceCollector<R> {

	@SuppressWarnings("rawtypes")
	private final Supplier<Map> mapSupplier;
	public Map<Identifier, DTResource<R>> resources;

	@SuppressWarnings("unchecked")
	public SimpleResourceCollector(@SuppressWarnings("rawtypes") Supplier<Map> mapSupplier) {
		this.mapSupplier = mapSupplier;
		this.resources = (Map<Identifier, DTResource<R>>) mapSupplier.get();
	}

	@Override
	public DTResource<R> put(DTResource<R> resource) {
		return this.resources.put(resource.getLocation(), resource);
	}

	@Override
	public DTResource<R> computeIfAbsent(Identifier key, Supplier<DTResource<R>> resourceSupplier) {
		return this.resources.computeIfAbsent(key, k -> resourceSupplier.get());
	}

	@Override
	public ResourceAccessor<R> createAccessor() {
		return new SimpleResourceAccessor<>(this.resources, this::newMap);
	}

	@SuppressWarnings("unchecked")
	@Override
	public void clear() {
		this.resources = (Map<Identifier, DTResource<R>>) mapSupplier.get();
	}

	@SuppressWarnings("unchecked")
	private <N> Map<Identifier, DTResource<N>> newMap() {
		return (Map<Identifier, DTResource<N>>) this.mapSupplier.get();
	}

}
