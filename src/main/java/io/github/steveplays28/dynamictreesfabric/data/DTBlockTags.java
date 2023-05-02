package io.github.steveplays28.dynamictreesfabric.data;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;

/**
 * @author Harley O'Connor
 */
public final class DTBlockTags {

    public static final TagKey<Block> BRANCHES = bind("branches");
    public static final TagKey<Block> STRIPPED_BRANCHES = bind("stripped_branches");
    public static final TagKey<Block> BRANCHES_THAT_BURN = bind("branches_that_burn");
    public static final TagKey<Block> STRIPPED_BRANCHES_THAT_BURN = bind("stripped_branches_that_burn");
    public static final TagKey<Block> FOLIAGE = bind("foliage");
    public static final TagKey<Block> FUNGUS_BRANCHES = bind("fungus_branches");
    public static final TagKey<Block> STRIPPED_FUNGUS_BRANCHES = bind("stripped_fungus_branches");
    public static final TagKey<Block> FUNGUS_CAPS = bind("fungus_caps");
    public static final TagKey<Block> LEAVES = bind("leaves");
    public static final TagKey<Block> SAPLINGS = bind("saplings");
    public static final TagKey<Block> WART_BLOCKS = bind("wart_blocks");

    private static TagKey<Block> bind(String identifier) {
        return BlockTags.create(new ResourceLocation(io.github.steveplays28.dynamictreesfabric.DynamicTreesFabric.MOD_ID, identifier));
    }
}
