package io.github.steveplays28.dynamictreesfabric.resources;

import com.google.common.collect.Lists;
import io.github.steveplays28.dynamictreesfabric.api.resource.ResourceAccessor;
import io.github.steveplays28.dynamictreesfabric.api.resource.TreeResourceManager;
import io.github.steveplays28.dynamictreesfabric.api.resource.TreeResourcePack;
import io.github.steveplays28.dynamictreesfabric.api.resource.loading.ApplierResourceLoader;
import io.github.steveplays28.dynamictreesfabric.api.resource.loading.ResourceLoader;
import io.github.steveplays28.dynamictreesfabric.util.CommonCollectors;

import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.ResourcePack;
import net.minecraft.util.Identifier;

import java.io.FileNotFoundException;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Predicate;
import java.util.stream.Stream;

/**
 * @author Harley O'Connor
 */
public final class TreesResourceManager implements ResourceManager, TreeResourceManager {

	private final List<TreeResourcePack> resourcePacks = Lists.newArrayList();
	private final List<ResourceLoader<?>> resourceLoaders = Lists.newArrayList();

	@Override
	public void addLoader(ResourceLoader<?> loader) {
		this.resourceLoaders.add(loader);
	}

	@Override
	public void addLoaders(ResourceLoader<?>... loaders) {
		this.resourceLoaders.addAll(Arrays.asList(loaders));
	}

	@Override
	public void addLoaderBefore(ResourceLoader<?> loader, ResourceLoader<?> existing) {
		this.resourceLoaders.add(this.resourceLoaders.indexOf(existing), loader);
	}

	@Override
	public void addLoaderAfter(ResourceLoader<?> loader, ResourceLoader<?> existing) {
		this.resourceLoaders.add(this.resourceLoaders.indexOf(existing) + 1, loader);
	}

	@Override
	public void registerAppliers() {
		this.resourceLoaders.stream()
				.filter(ApplierResourceLoader.class::isInstance)
				.map(ApplierResourceLoader.class::cast)
				.forEach(ApplierResourceLoader::registerAppliers);
	}

	@Override
	public void load() {
		this.resourceLoaders.forEach(loader -> loader.load(this).join());
	}

	@Override
	public void gatherData() {
		this.resourceLoaders.forEach(loader -> loader.gatherData(this).join());
	}

	@Override
	public void setup() {
		this.resourceLoaders.forEach(loader -> loader.setup(this).join());
	}

	@Override
	public CompletableFuture<?>[] prepareReload(final Executor gameExecutor, final Executor backgroundExecutor) {
		return this.resourceLoaders.stream()
				.map(loader -> loader.prepareReload(this))
				.toArray(CompletableFuture<?>[]::new);
	}

	/**
	 * Reloads the given {@link CompletableFuture}s. These <b>must</b> be given in the same order as returned from
	 * {@link #prepareReload(Executor, Executor)}.
	 *
	 * @param futures the futures returned from {@link #prepareReload(Executor, Executor)}
	 */
	@Override
	public void reload(final CompletableFuture<?>[] futures) {
		for (int i = 0; i < futures.length; i++) {
			this.reload(this.resourceLoaders.get(i), futures[i]);
		}
	}

	@SuppressWarnings("unchecked")
	private <R> void reload(final ResourceLoader<R> loader, final CompletableFuture<?> future) {
		loader.reload((CompletableFuture<ResourceAccessor<R>>) future, this);
	}

	@Override
	public void addPack(TreeResourcePack pack) {
		this.resourcePacks.add(pack);
	}

	@Override
	public Set<String> getAllNamespaces() {
		return this.resourcePacks.stream()
				.map(treeResourcePack -> treeResourcePack.getNamespaces(null))
				.flatMap(Collection::stream)
				.collect(CommonCollectors.toLinkedSet());
	}

	@Override
	public Optional<Resource> getResource(final Identifier location) {
		final List<Resource> resources = this.getAllResources(location);

		return resources.isEmpty() ? Optional.empty() : Optional.of(resources.get(resources.size() - 1));
	}

	@Override
	public Resource getResourceOrThrow(Identifier location) throws FileNotFoundException {
		return getResource(location).orElseThrow(() -> new FileNotFoundException("Could not find path '" + location + "' in any tree packs."));
	}

	@Override
	public List<Resource> getAllResources(Identifier path) {
		return this.resourcePacks.stream()
				.filter(resourcePack -> resourcePack.hasResource(path))
				.map(resourcePack -> getResource(path, resourcePack))
				.toList();
	}

	private Resource getResource(Identifier path, TreeResourcePack resourcePack) {
		return new Resource(resourcePack.getName(), () -> resourcePack.getResource(path));
	}

	@Override
	public Map<Identifier, Resource> findResources(String path, Predicate<Identifier> filter) {
		Map<Identifier, Resource> resources = new TreeMap<>();

		for (TreeResourcePack pack : this.resourcePacks) {
			for (String namespace : pack.getNamespaces()) {
				Collection<Identifier> subResources = pack.getResources(namespace, path, filter);
				for (Identifier location : subResources) {
					// TODO Should this throw or doing anything if the key already has an associated value?
					resources.computeIfAbsent(location, loc -> getResource(loc, pack));
				}
			}
		}

		return resources;
	}

	@Override
	public Map<Identifier, List<Resource>> findAllResources(String path, Predicate<Identifier> filter) {
		Map<Identifier, List<Resource>> resources = new TreeMap<>();

		for (TreeResourcePack pack : this.resourcePacks) {
			for (String namespace : pack.getNamespaces()) {
				Collection<Identifier> subResources = pack.getResources(namespace, path, filter);
				for (Identifier location : subResources) {
					resources.computeIfAbsent(location, this::getAllResources);
				}
			}
		}

		return resources;
	}

	@Override
	public Stream<ResourcePack> streamResourcePacks() {
		return this.resourcePacks.stream().map(treeResourcePack -> treeResourcePack);
	}

}
