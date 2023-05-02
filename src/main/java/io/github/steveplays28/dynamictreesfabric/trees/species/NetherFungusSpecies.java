package io.github.steveplays28.dynamictreesfabric.trees.species;

import io.github.steveplays28.dynamictreesfabric.api.registry.TypedRegistry;
import io.github.steveplays28.dynamictreesfabric.blocks.leaves.LeavesProperties;
import io.github.steveplays28.dynamictreesfabric.blocks.rootyblocks.SoilHelper;
import io.github.steveplays28.dynamictreesfabric.data.DTBlockTags;
import io.github.steveplays28.dynamictreesfabric.data.DTItemTags;
import io.github.steveplays28.dynamictreesfabric.systems.dropcreators.DropCreators;
import io.github.steveplays28.dynamictreesfabric.systems.genfeatures.GenFeatures;
import io.github.steveplays28.dynamictreesfabric.trees.Family;
import io.github.steveplays28.dynamictreesfabric.trees.Species;
import io.github.steveplays28.dynamictreesfabric.util.CommonVoxelShapes;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.Tags;

import java.util.Collections;
import java.util.List;
import java.util.function.BiConsumer;

import static io.github.steveplays28.dynamictreesfabric.util.ResourceLocationUtils.surround;

/**
 * @author Harley O'Connor
 */
public class NetherFungusSpecies extends Species {

    public static final TypedRegistry.EntryType<Species> TYPE = createDefaultType(NetherFungusSpecies::new);

    public NetherFungusSpecies(ResourceLocation name, Family family, LeavesProperties leavesProperties) {
        super(name, family, leavesProperties);
        this.setSaplingShape(CommonVoxelShapes.SAPLING);
    }

    @Override
    public Species setDefaultGrowingParameters() {
        this.setBasicGrowingParameters(0f, 14.0f, 0, 4, 1f);
        return super.setDefaultGrowingParameters();
    }

    @Override
    protected void setStandardSoils() {
        this.addAcceptableSoils(SoilHelper.NETHER_SOIL_LIKE, SoilHelper.FUNGUS_LIKE, SoilHelper.DIRT_LIKE);
    }

    @Override
    public Species setPreReloadDefaults() {
        return this.setDefaultGrowingParameters()
                .setSaplingShape(CommonVoxelShapes.FLAT_MUSHROOM)
                .setSaplingSound(SoundType.FUNGUS)
                .setCanSaplingGrowNaturally(false)
                .addDropCreators(DropCreators.LOG)
                .envFactor(Tags.Biomes.IS_COLD, 0.25f)
                .envFactor(Tags.Biomes.IS_WET, 0.75f);
    }

    @Override
    public Species setPostReloadDefaults() {
        if (!this.hasGenFeatures()) {
            this.addGenFeature(GenFeatures.CLEAR_VOLUME).addGenFeature(GenFeatures.SHROOMLIGHT);
        }
        return super.setPostReloadDefaults();
    }

    @Override
    public boolean isAcceptableSoilForWorldgen(LevelAccessor world, BlockPos pos, BlockState soilBlockState) {
        if (soilBlockState.getBlock() == Blocks.NETHERRACK) {
            return true; //Soil exception for worldgen
        }
        return super.isAcceptableSoilForWorldgen(world, pos, soilBlockState);
    }

    @Override
    public float defaultSeedComposterChance() {
        return 0.65f;
    }

    @Override
    public List<TagKey<Block>> defaultSaplingTags() {
        return Collections.singletonList(DTBlockTags.FUNGUS_CAPS);
    }

    @Override
    public List<TagKey<Item>> defaultSeedTags() {
        return Collections.singletonList(DTItemTags.FUNGUS_CAPS);
    }

    @Override
    public ResourceLocation getSaplingSmartModelLocation() {
        return io.github.steveplays28.dynamictreesfabric.DynamicTreesFabric.resLoc("block/smartmodel/mushroom_" + (this.getSaplingShape() == CommonVoxelShapes.FLAT_MUSHROOM ? "flat" : "round"));
    }

    @Override
    public void addSaplingTextures(BiConsumer<String, ResourceLocation> textureConsumer,
                                   ResourceLocation leavesTextureLocation, ResourceLocation barkTextureLocation) {
        final ResourceLocation capLocation = surround(this.getRegistryName(), "block/", "_cap");
        textureConsumer.accept("particle", capLocation);
        textureConsumer.accept("stem", barkTextureLocation);
        textureConsumer.accept("cap", capLocation);
    }

}
