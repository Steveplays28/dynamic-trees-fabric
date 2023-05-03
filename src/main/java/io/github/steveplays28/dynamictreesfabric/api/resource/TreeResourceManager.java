package io.github.steveplays28.dynamictreesfabric.api.resource;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.stream.Stream;

import io.github.steveplays28.dynamictreesfabric.api.resource.loading.ResourceLoader;

import net.minecraft.resource.ResourcePack;

/**
 * @author Harley O'Connor
 */
public interface TreeResourceManager extends net.minecraft.resource.ResourceManager {

	void addLoader(ResourceLoader<?> loader);

	void addLoaders(ResourceLoader<?>... loaders);

	void addLoaderBefore(ResourceLoader<?> loader, ResourceLoader<?> existing);

	void addLoaderAfter(ResourceLoader<?> loader, ResourceLoader<?> existing);

	void registerAppliers();

	void load();

	void gatherData();

	void setup();

	CompletableFuture<?>[] prepareReload(Executor gameExecutor, Executor backgroundExecutor);

	void reload(CompletableFuture<?>[] futures);

	void addPack(TreeResourcePack pack);

	@Override
	Stream<ResourcePack> streamResourcePacks();

}
