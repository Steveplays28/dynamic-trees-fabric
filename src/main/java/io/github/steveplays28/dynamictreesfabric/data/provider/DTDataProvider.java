package io.github.steveplays28.dynamictreesfabric.data.provider;

import net.minecraft.resources.ResourceLocation;

import static io.github.steveplays28.dynamictreesfabric.util.ResourceLocationUtils.prefix;

/**
 * @author Harley O'Connor
 */
public interface DTDataProvider {

    default ResourceLocation block(ResourceLocation blockLocation) {
        return prefix(blockLocation, "block/");
    }

    default ResourceLocation item(ResourceLocation resourceLocation) {
        return prefix(resourceLocation, "item/");
    }

}
