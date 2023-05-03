package io.github.steveplays28.dynamictreesfabric.systems.substances;

import io.github.steveplays28.dynamictreesfabric.api.TreeHelper;
import io.github.steveplays28.dynamictreesfabric.api.network.MapSignal;
import io.github.steveplays28.dynamictreesfabric.api.substances.SubstanceEffect;
import io.github.steveplays28.dynamictreesfabric.blocks.rootyblocks.RootyBlock;
import io.github.steveplays28.dynamictreesfabric.systems.nodemappers.DenuderNode;
import io.github.steveplays28.dynamictreesfabric.trees.Family;
import io.github.steveplays28.dynamictreesfabric.trees.Species;

import net.minecraft.block.BlockState;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/**
 * An {@link SubstanceEffect} that "denudes" the tree. This involves stripping all branches and removing all leaves.
 *
 * @author Harley O'Connor
 */
public class DenudeSubstance implements SubstanceEffect {

	@Override
	public boolean apply(World world, BlockPos rootPos) {
		final BlockState rootState = world.getBlockState(rootPos);
		final RootyBlock dirt = TreeHelper.getRooty(rootState);

		if (dirt == null) {
			return false;
		}

		final Species species = dirt.getSpecies(rootState, world, rootPos);
		final Family family = species.getFamily();

		// If the family doesn't have a stripped branch the substance can't be applied.
		if (!family.hasStrippedBranch()) {
			return false;
		}

		// Set fertility to zero so the leaves won't grow back.
		dirt.setFertility(world, rootPos, 0);

		if (world.isClient) {
			TreeHelper.treeParticles(world, rootPos, ParticleTypes.ASH, 8);
		} else {
			dirt.startAnalysis(world, rootPos, new MapSignal(new DenuderNode(species, family)));
		}

		return true;
	}

	@Override
	public String getName() {
		return "denude";
	}

	@Override
	public boolean isLingering() {
		return false;
	}

}
