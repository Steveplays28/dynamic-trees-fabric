package io.github.steveplays28.dynamictreesfabric.api.data;

import io.github.steveplays28.dynamictreesfabric.data.provider.DTItemModelProvider;
import io.github.steveplays28.dynamictreesfabric.trees.Family;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraftforge.client.model.generators.ItemModelBuilder;
import net.minecraftforge.registries.ForgeRegistries;

/**
 * @author Harley O'Connor
 */
public class BranchItemModelGenerator implements Generator<DTItemModelProvider, Family> {

    public static final DependencyKey<Block> PRIMITIVE_LOG_BLOCK = new DependencyKey<>("primitive_log_block");
    public static final DependencyKey<Item> PRIMITIVE_LOG_ITEM = new DependencyKey<>("primitive_log_item");

    @Override
    public void generate(DTItemModelProvider provider, Family input, Dependencies dependencies) {
        final ItemModelBuilder builder = provider.withExistingParent(
                String.valueOf(ForgeRegistries.ITEMS.getKey(dependencies.get(PRIMITIVE_LOG_ITEM))),
                input.getBranchItemParentLocation()
        );
        input.addBranchTextures(
                builder::texture,
                provider.block(ForgeRegistries.BLOCKS.getKey(dependencies.get(PRIMITIVE_LOG_BLOCK)))
        );
    }

    @Override
    public Dependencies gatherDependencies(Family input) {
        return new Dependencies()
                .append(PRIMITIVE_LOG_BLOCK, input.getPrimitiveLog())
                .append(PRIMITIVE_LOG_ITEM, input.getBranchItem());
    }

}
