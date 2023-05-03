package io.github.steveplays28.dynamictreesfabric.systems.nodemappers;

import java.util.ArrayList;
import java.util.List;

import org.jetbrains.annotations.Nullable;

import io.github.steveplays28.dynamictreesfabric.api.TreeHelper;
import io.github.steveplays28.dynamictreesfabric.api.network.NodeInspector;
import io.github.steveplays28.dynamictreesfabric.blocks.branches.BranchBlock;
import io.github.steveplays28.dynamictreesfabric.systems.BranchConnectables;
import io.github.steveplays28.dynamictreesfabric.trees.Species;
import io.github.steveplays28.dynamictreesfabric.util.BlockStates;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;

/**
 * Destroys all branches on a tree and the surrounding leaves.
 *
 * @author ferreusveritas
 */
public class DestroyerNode implements NodeInspector {

	private final List<BlockPos> endPoints;//We always need to track endpoints during destruction
	Species species;//Destroy any node that's made of the same kind of wood
	private PlayerEntity player = null;

	public DestroyerNode(Species species) {
		this.endPoints = new ArrayList<>(32);
		this.species = species;
	}

	public DestroyerNode setPlayer(PlayerEntity player) {
		this.player = player;
		return this;
	}

	public List<BlockPos> getEnds() {
		return endPoints;
	}

	@Override
	public boolean run(BlockState blockState, WorldAccess world, BlockPos pos, @Nullable Direction fromDir) {
		if (BranchConnectables.getConnectionRadiusForBlock(blockState, world, pos, fromDir == null ? null : fromDir.getOpposite()) > 0) {
			if (player != null && world instanceof World) {
				BlockEntity te = world.getBlockEntity(pos);
				blockState.getBlock().onDestroyedByPlayer(blockState, (World) world, pos, player, true, world.getFluidState(pos));
				blockState.getBlock().afterBreak((World) world, player, pos, blockState, te, player.getMainHandStack());
			} else {
				world.setBlockState(pos, BlockStates.AIR, 0);
			}
			return true;
		}

		BranchBlock branch = TreeHelper.getBranch(blockState);

		if (branch != null && species.getFamily() == branch.getFamily()) {
			boolean waterlogged = blockState.contains(Properties.WATERLOGGED) && blockState.get(Properties.WATERLOGGED);
			if (branch.getRadius(blockState) == species.getFamily().getPrimaryThickness()) {
				endPoints.add(pos);
			}
			world.setBlockState(pos, waterlogged ? Blocks.WATER.getDefaultState() : Blocks.AIR.getDefaultState(), 3);//Destroy the branch and notify the client
		}

		return true;
	}

	@Override
	public boolean returnRun(BlockState blockState, WorldAccess world, BlockPos pos, Direction fromDir) {
		return false;
	}
}
