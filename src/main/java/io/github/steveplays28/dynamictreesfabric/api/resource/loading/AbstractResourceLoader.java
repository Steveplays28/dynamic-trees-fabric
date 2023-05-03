package io.github.steveplays28.dynamictreesfabric.api.resource.loading;

import java.util.concurrent.CompletableFuture;

import io.github.steveplays28.dynamictreesfabric.api.resource.ResourceAccessor;
import io.github.steveplays28.dynamictreesfabric.api.resource.loading.preparation.ResourcePreparer;

import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Util;

/**
 * @author Harley O'Connor
 */
public abstract class AbstractResourceLoader<R> implements ResourceLoader<R> {

	private final ResourcePreparer<R> resourcePreparer;

	public AbstractResourceLoader(ResourcePreparer<R> resourcePreparer) {
		this.resourcePreparer = resourcePreparer;
	}

	@Override
	public final CompletableFuture<Void> gatherData(ResourceManager resourceManager) {
		return CompletableFuture.supplyAsync(
				() -> this.resourcePreparer.prepare(resourceManager),
				Util.getMainWorkerExecutor()
		).thenAccept(preparedObject ->
				this.applyOnGatherData(preparedObject, resourceManager)
		);
	}

	@Override
	public final CompletableFuture<Void> load(ResourceManager resourceManager) {
		return CompletableFuture.supplyAsync(
				() -> this.resourcePreparer.prepare(resourceManager),
				Util.getMainWorkerExecutor()
		).thenAccept(preparedObject ->
				this.applyOnLoad(preparedObject, resourceManager)
		);
	}

	@Override
	public final CompletableFuture<Void> setup(ResourceManager resourceManager) {
		return CompletableFuture.supplyAsync(
				() -> this.resourcePreparer.prepare(resourceManager),
				Util.getMainWorkerExecutor()
		).thenAccept(preparedObject ->
				this.applyOnSetup(preparedObject, resourceManager)
		);
	}

	@Override
	public CompletableFuture<ResourceAccessor<R>> prepareReload(ResourceManager resourceManager) {
		return CompletableFuture.supplyAsync(
				() -> this.resourcePreparer.prepare(resourceManager),
				Util.getMainWorkerExecutor()
		);
	}

	@Override
	public final void reload(CompletableFuture<ResourceAccessor<R>> future,
	                         ResourceManager resourceManager) {
		this.applyOnReload(future.join(), resourceManager);
	}

	@Override
	public void applyOnLoad(ResourceAccessor<R> resourceAccessor, ResourceManager resourceManager) {

	}

	@Override
	public void applyOnGatherData(ResourceAccessor<R> resourceAccessor, ResourceManager resourceManager) {

	}

	@Override
	public void applyOnSetup(ResourceAccessor<R> resourceAccessor, ResourceManager resourceManager) {

	}

	@Override
	public void applyOnReload(ResourceAccessor<R> resourceAccessor, ResourceManager resourceManager) {

	}

}
