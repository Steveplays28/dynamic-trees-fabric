package io.github.steveplays28.dynamictreesfabric.trees.families;

import io.github.steveplays28.dynamictreesfabric.api.registry.TypedRegistry;
import io.github.steveplays28.dynamictreesfabric.data.DTBlockTags;
import io.github.steveplays28.dynamictreesfabric.data.DTItemTags;
import io.github.steveplays28.dynamictreesfabric.trees.Family;
import io.github.steveplays28.dynamictreesfabric.util.BlockBounds;
import java.util.Collections;
import java.util.List;
import net.minecraft.block.Block;
import net.minecraft.block.Material;
import net.minecraft.item.Item;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Direction;

/**
 * @author Harley O'Connor
 */
public class NetherFungusFamily extends Family {

    public static final TypedRegistry.EntryType<Family> TYPE = TypedRegistry.newType(NetherFungusFamily::new);

    public NetherFungusFamily(Identifier name) {
        super(name);
    }

    @Override
    public int getPrimaryThickness() {
        return 3;
    }

    @Override
    public int getSecondaryThickness() {
        return 4;
    }

    @Override
    public Material getDefaultBranchMaterial() {
        return Material.NETHER_WOOD;
    }

    @Override
    public BlockSoundGroup getDefaultBranchSoundType() {
        return BlockSoundGroup.NETHER_STEM;
    }

    @Override
    public boolean isFireProof() {
        return true;
    }

    public BlockBounds expandLeavesBlockBounds(BlockBounds bounds) {
        return bounds.expand(1).expand(Direction.DOWN, 3);
    }

    @Override
    public List<TagKey<Block>> defaultBranchTags() {
        return Collections.singletonList(DTBlockTags.FUNGUS_BRANCHES);
    }

    @Override
    public List<TagKey<Item>> defaultBranchItemTags() {
        return Collections.singletonList(DTItemTags.FUNGUS_BRANCHES);
    }

    @Override
    public List<TagKey<Block>> defaultStrippedBranchTags() {
        return Collections.singletonList(DTBlockTags.STRIPPED_FUNGUS_BRANCHES);
    }

}
