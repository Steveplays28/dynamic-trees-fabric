package com.ferreusveritas.dynamictrees.api.resource;

import com.ferreusveritas.dynamictrees.api.resource.loading.ResourceLoader;
import net.minecraft.resources.IResourceManager;
import net.minecraft.resources.IResourcePack;

import java.util.Collection;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.stream.Stream;

/**
 * @author Harley O'Connor
 */
public interface ResourceManager extends IResourceManager {

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

    void addPacks(Collection<TreeResourcePack> packs);

    @Override
    Stream<IResourcePack> listPacks();

}
