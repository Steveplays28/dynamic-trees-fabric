package io.github.steveplays28.dynamictreesfabric.data.provider;

import static io.github.steveplays28.dynamictreesfabric.util.ResourceLocationUtils.prefix;

import net.minecraft.util.Identifier;

/**
 * @author Harley O'Connor
 */
public interface DTDataProvider {

    default Identifier block(Identifier blockLocation) {
        return prefix(blockLocation, "block/");
    }

    default Identifier item(Identifier resourceLocation) {
        return prefix(resourceLocation, "item/");
    }

}
