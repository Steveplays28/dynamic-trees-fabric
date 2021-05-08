package com.ferreusveritas.dynamictrees.systems.genfeatures;

import com.ferreusveritas.dynamictrees.api.IPostRotGenFeature;
import com.ferreusveritas.dynamictrees.api.TreeHelper;
import com.ferreusveritas.dynamictrees.api.configurations.ConfigurationProperty;
import com.ferreusveritas.dynamictrees.systems.genfeatures.config.ConfiguredGenFeature;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;

import java.util.Random;

/**
 * Implementation of {@link IPostRotGenFeature} that rots the soil after a
 *
 * @author Harley O'Connor
 */
public class RotSoilGenFeature extends GenFeature implements IPostRotGenFeature {

    public static final ConfigurationProperty<Block> ROTTED_SOIL = ConfigurationProperty.block("rotted_soil");

    public RotSoilGenFeature(ResourceLocation registryName) {
        super(registryName);
    }

    @Override
    protected void registerProperties() {
        this.register(ROTTED_SOIL);
    }

    @Override
    protected ConfiguredGenFeature<GenFeature> createDefaultConfiguration() {
        return super.createDefaultConfiguration()
                .with(ROTTED_SOIL, Blocks.DIRT);
    }

    @Override
    public void postRot(ConfiguredGenFeature<?> configuredGenFeature, IWorld world, BlockPos pos, int neighborCount, int radius, Random random, boolean rapid) {
        if (!TreeHelper.isRooty(world.getBlockState(pos.below())))
            return;

        world.setBlock(pos.below(), configuredGenFeature.get(ROTTED_SOIL).defaultBlockState(), 3); // Change rooty dirt to rotted soil.
    }

}
