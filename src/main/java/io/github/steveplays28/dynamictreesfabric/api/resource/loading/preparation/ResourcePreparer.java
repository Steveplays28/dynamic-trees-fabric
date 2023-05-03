package io.github.steveplays28.dynamictreesfabric.api.resource.loading.preparation;

import io.github.steveplays28.dynamictreesfabric.api.resource.ResourceAccessor;

import net.minecraft.resource.ResourceManager;

/**
 * @author Harley O'Connor
 */
public interface ResourcePreparer<R> {

	ResourceAccessor<R> prepare(ResourceManager resourceManager);

}
