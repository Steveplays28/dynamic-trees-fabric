package io.github.steveplays28.dynamictreesfabric.systems.nodemappers;

import io.github.steveplays28.dynamictreesfabric.api.TreeHelper;
import io.github.steveplays28.dynamictreesfabric.api.network.NodeInspector;
import io.github.steveplays28.dynamictreesfabric.blocks.branches.BranchBlock;
import io.github.steveplays28.dynamictreesfabric.trees.Species;

import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.WorldAccess;

/**
 * Destroys all thin(radius == 1) branches on a tree.. leaving it to postRot.
 *
 * @author ferreusveritas
 */
public class DiseaseNode implements NodeInspector {

	Species species;//Destroy any thin branches made of the same kind of wood.

	public DiseaseNode(Species tree) {
		this.species = tree;
	}

	@Override
	public boolean run(BlockState blockState, WorldAccess world, BlockPos pos, Direction fromDir) {
		BranchBlock branch = TreeHelper.getBranch(blockState);

		if (branch != null && species.getFamily() == branch.getFamily()) {
			if (branch.getRadius(blockState) == 1) {
				world.removeBlock(pos, false);//Destroy the thin branch
			}
		}

		return true;
	}

	@Override
	public boolean returnRun(BlockState blockState, WorldAccess world, BlockPos pos, Direction fromDir) {
		return false;
	}

}
