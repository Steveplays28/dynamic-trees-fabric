package io.github.steveplays28.dynamictreesfabric.systems.substances;

import io.github.steveplays28.dynamictreesfabric.api.TreeHelper;
import io.github.steveplays28.dynamictreesfabric.api.substances.SubstanceEffect;
import io.github.steveplays28.dynamictreesfabric.blocks.rootyblocks.RootyBlock;
import io.github.steveplays28.dynamictreesfabric.compat.waila.WailaOther;
import io.github.steveplays28.dynamictreesfabric.trees.Species;

import net.minecraft.block.BlockState;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class MegaSubstance implements SubstanceEffect {

	@Override
	public boolean apply(World world, BlockPos rootPos) {

		BlockState blockState = world.getBlockState(rootPos);
		RootyBlock dirt = TreeHelper.getRooty(blockState);
		final Species species = dirt.getSpecies(blockState, world, rootPos);
		final Species megaSpecies = species.getMegaSpecies();

		if (megaSpecies.isValid()) {
			int fertility = dirt.getFertility(blockState, world, rootPos);
			megaSpecies.placeRootyDirtBlock(world, rootPos, fertility);

			blockState = world.getBlockState(rootPos);
			dirt = TreeHelper.getRooty(blockState);

			if (dirt.getSpecies(blockState, world, rootPos) == megaSpecies) {
				TreeHelper.treeParticles(world, rootPos, ParticleTypes.DRAGON_BREATH, 8);
				WailaOther.invalidateWailaPosition();
				return true;
			}
		}

		return false;
	}

	@Override
	public String getName() {
		return "mega";
	}

	@Override
	public boolean isLingering() {
		return false;
	}

}
