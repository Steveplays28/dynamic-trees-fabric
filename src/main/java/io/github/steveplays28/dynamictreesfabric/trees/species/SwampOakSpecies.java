package io.github.steveplays28.dynamictreesfabric.trees.species;

import io.github.steveplays28.dynamictreesfabric.api.registry.TypedRegistry;
import io.github.steveplays28.dynamictreesfabric.blocks.leaves.LeavesProperties;
import io.github.steveplays28.dynamictreesfabric.init.DTConfigs;
import io.github.steveplays28.dynamictreesfabric.trees.Family;
import io.github.steveplays28.dynamictreesfabric.trees.Species;
import io.github.steveplays28.dynamictreesfabric.util.SafeChunkBounds;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.biome.Biome;

public class SwampOakSpecies extends Species {

    public static final TypedRegistry.EntryType<Species> TYPE = createDefaultType(SwampOakSpecies::new);

    public SwampOakSpecies(ResourceLocation name, Family family, LeavesProperties leavesProperties) {
        super(name, family, leavesProperties);
    }

    private static final int minRadiusForSunkGeneration = 5;

    @Override
    public boolean generate(Level worldObj, LevelAccessor world, BlockPos rootPos, Holder<Biome> biome, RandomSource random, int radius, SafeChunkBounds safeBounds) {
        if (isWater(world.getBlockState(rootPos))) {
            switch (DTConfigs.SWAMP_OAKS_IN_WATER.get()) {
                case SUNK: //generate 1 block down
                    if (radius >= minRadiusForSunkGeneration) {
                        return super.generate(worldObj, world, rootPos.below(), biome, random, radius, safeBounds);
                    } else {
                        return false;
                    }
                case DISABLED: //do not generate
                    return false;
                case ROOTED: //just generate normally
            }
        }
        return super.generate(worldObj, world, rootPos, biome, random, radius, safeBounds);
    }

}
