package io.github.steveplays28.dynamictreesfabric.systems.genfeatures;

import io.github.steveplays28.dynamictreesfabric.api.TreeHelper;
import io.github.steveplays28.dynamictreesfabric.api.configurations.ConfigurationProperty;
import io.github.steveplays28.dynamictreesfabric.systems.genfeatures.context.PostRotContext;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

/**
 * A{@link GenFeature} handling post rot behaviour in which the soil below the base branch is turned to the {@link
 * #ROTTEN_SOIL} property block after that branch has rotted away.
 *
 * @author Harley O'Connor
 */
public class RotSoilGenFeature extends GenFeature {

    public static final ConfigurationProperty<Block> ROTTEN_SOIL = ConfigurationProperty.block("rotten_soil");

    public RotSoilGenFeature(ResourceLocation registryName) {
        super(registryName);
    }

    @Override
    protected void registerProperties() {
        this.register(ROTTEN_SOIL);
    }

    @Override
    protected GenFeatureConfiguration createDefaultConfiguration() {
        return super.createDefaultConfiguration().with(ROTTEN_SOIL, Blocks.DIRT);
    }

    @Override
    protected boolean postRot(GenFeatureConfiguration configuration, PostRotContext context) {
        final LevelAccessor world = context.world();
        final BlockPos belowPos = context.pos().below();

        if (!TreeHelper.isRooty(world.getBlockState(belowPos))) {
            return false;
        }

        // Change rooty dirt to rotted soil.
        world.setBlock(belowPos, configuration.get(ROTTEN_SOIL).defaultBlockState(), 3);
        return true;
    }

}
