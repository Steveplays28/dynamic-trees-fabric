package io.github.steveplays28.dynamictreesfabric.trees.species;

import io.github.steveplays28.dynamictreesfabric.api.registry.TypedRegistry;
import io.github.steveplays28.dynamictreesfabric.blocks.leaves.LeavesProperties;
import io.github.steveplays28.dynamictreesfabric.init.DTConfigs;
import io.github.steveplays28.dynamictreesfabric.trees.Family;
import io.github.steveplays28.dynamictreesfabric.trees.Species;
import io.github.steveplays28.dynamictreesfabric.util.SafeChunkBounds;

import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.biome.Biome;

public class SwampOakSpecies extends Species {

	public static final TypedRegistry.EntryType<Species> TYPE = createDefaultType(SwampOakSpecies::new);
	private static final int minRadiusForSunkGeneration = 5;

	public SwampOakSpecies(Identifier name, Family family, LeavesProperties leavesProperties) {
		super(name, family, leavesProperties);
	}

	@Override
	public boolean generate(World worldObj, WorldAccess world, BlockPos rootPos, RegistryEntry<Biome> biome, Random random, int radius, SafeChunkBounds safeBounds) {
		if (isWater(world.getBlockState(rootPos))) {
			switch (DTConfigs.SWAMP_OAKS_IN_WATER.get()) {
				case SUNK: //generate 1 block down
					if (radius >= minRadiusForSunkGeneration) {
						return super.generate(worldObj, world, rootPos.down(), biome, random, radius, safeBounds);
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
