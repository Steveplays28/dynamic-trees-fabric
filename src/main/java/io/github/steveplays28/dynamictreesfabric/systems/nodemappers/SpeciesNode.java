package io.github.steveplays28.dynamictreesfabric.systems.nodemappers;

import io.github.steveplays28.dynamictreesfabric.api.TreeHelper;
import io.github.steveplays28.dynamictreesfabric.api.network.NodeInspector;
import io.github.steveplays28.dynamictreesfabric.api.treedata.TreePart;
import io.github.steveplays28.dynamictreesfabric.trees.Species;

import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.WorldAccess;

public class SpeciesNode implements NodeInspector {

	private Species determination = Species.NULL_SPECIES;

	@Override
	public boolean run(BlockState blockState, WorldAccess world, BlockPos pos, Direction fromDir) {

		TreePart treePart = TreeHelper.getTreePart(blockState);

		switch (treePart.getTreePartType()) {
			case BRANCH:
				if (determination == Species.NULL_SPECIES) {
					determination = TreeHelper.getBranch(treePart).getFamily().getCommonSpecies();
				}
				break;
			case ROOT:
				determination = TreeHelper.getRooty(treePart).getSpecies(world.getBlockState(pos), world, pos);
				break;
			default:
				break;
		}

		return true;
	}

	@Override
	public boolean returnRun(BlockState blockState, WorldAccess world, BlockPos pos, Direction fromDir) {
		return false;
	}

	public Species getSpecies() {
		return determination;
	}

}
