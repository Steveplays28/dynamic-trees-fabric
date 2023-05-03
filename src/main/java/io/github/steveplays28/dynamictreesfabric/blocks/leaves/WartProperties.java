package io.github.steveplays28.dynamictreesfabric.blocks.leaves;

import io.github.steveplays28.dynamictreesfabric.api.registry.TypedRegistry;
import io.github.steveplays28.dynamictreesfabric.data.DTBlockTags;
import java.util.Collections;
import java.util.List;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.MapColor;
import net.minecraft.block.Material;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.util.Identifier;

/**
 * @author Harley O'Connor
 */
public class WartProperties extends SolidLeavesProperties {

    public static final TypedRegistry.EntryType<LeavesProperties> TYPE = TypedRegistry.newType(WartProperties::new);

    public WartProperties(final Identifier registryName) {
        super(registryName);
    }

    @Override
    protected String getBlockRegistryNameSuffix() {
        return "_wart";
    }

    @Override
    public Material getDefaultMaterial() {
        return Material.SOLID_ORGANIC;
    }

    @Override
    public AbstractBlock.Settings getDefaultBlockProperties(Material material, MapColor materialColor) {
        return AbstractBlock.Settings.of(material, materialColor).strength(1.0F).sounds(BlockSoundGroup.WART_BLOCK)./*harvestTool(ToolType.HOE).*/ticksRandomly();
    }

    @Override
    public List<TagKey<Block>> defaultLeavesTags() {
        return Collections.singletonList(DTBlockTags.WART_BLOCKS);
    }

}
