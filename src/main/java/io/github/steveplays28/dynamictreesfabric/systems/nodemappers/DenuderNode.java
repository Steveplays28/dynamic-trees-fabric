package io.github.steveplays28.dynamictreesfabric.systems.nodemappers;

import io.github.steveplays28.dynamictreesfabric.api.TreeHelper;
import io.github.steveplays28.dynamictreesfabric.api.network.NodeInspector;
import io.github.steveplays28.dynamictreesfabric.blocks.branches.BranchBlock;
import io.github.steveplays28.dynamictreesfabric.trees.Family;
import io.github.steveplays28.dynamictreesfabric.trees.Species;
import io.github.steveplays28.dynamictreesfabric.util.BlockStates;
import io.github.steveplays28.dynamictreesfabric.util.SimpleVoxmap;

import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.WorldAccess;

/**
 * @author Harley O'Connor
 */
public class DenuderNode implements NodeInspector {

	private final Species species;
	private final Family family;

	public DenuderNode(final Species species, final Family family) {
		this.species = species;
		this.family = family;
	}

	@Override
	public boolean run(BlockState state, WorldAccess world, BlockPos pos, Direction fromDir) {
		final BranchBlock branch = TreeHelper.getBranch(state);

		if (branch == null || this.family.getBranch().map(other -> branch != other).orElse(false)) {
			return false;
		}

		final int radius = branch.getRadius(state);

		branch.stripBranch(state, world, pos, radius);

		if (radius <= this.family.getPrimaryThickness()) {
			this.removeSurroundingLeaves(world, pos);
		}

		return true;
	}

	@Override
	public boolean returnRun(BlockState blockState, WorldAccess world, BlockPos pos, Direction fromDir) {
		return false;
	}

	public void removeSurroundingLeaves(WorldAccess world, BlockPos twigPos) {
		if (world.isClient()) {
			return;
		}

		final SimpleVoxmap leafCluster = this.species.getLeavesProperties().getCellKit().getLeafCluster();
		final int xBound = leafCluster.getLenX();
		final int yBound = leafCluster.getLenY();
		final int zBound = leafCluster.getLenZ();

		BlockPos.stream(twigPos.add(-xBound, -yBound, -zBound), twigPos.add(xBound, yBound, zBound)).forEach(testPos -> {
			// We're only interested in where leaves could possibly be.
			if (leafCluster.getVoxel(twigPos, testPos) == 0) {
				return;
			}

			final BlockState state = world.getBlockState(testPos);
			if (this.family.isCompatibleGenericLeaves(this.species, state, world, testPos)) {
				world.setBlockState(testPos, BlockStates.AIR, 3);
			}
		});
	}

}
