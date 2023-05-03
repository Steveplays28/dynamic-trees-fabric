package io.github.steveplays28.dynamictreesfabric.api;

import io.github.steveplays28.dynamictreesfabric.api.network.MapSignal;
import io.github.steveplays28.dynamictreesfabric.api.treedata.TreePart;
import io.github.steveplays28.dynamictreesfabric.blocks.NullTreePart;
import io.github.steveplays28.dynamictreesfabric.blocks.branches.BranchBlock;
import io.github.steveplays28.dynamictreesfabric.blocks.branches.TrunkShellBlock;
import io.github.steveplays28.dynamictreesfabric.blocks.leaves.DynamicLeavesBlock;
import io.github.steveplays28.dynamictreesfabric.blocks.rootyblocks.RootyBlock;
import io.github.steveplays28.dynamictreesfabric.init.DTClient;
import io.github.steveplays28.dynamictreesfabric.systems.nodemappers.TwinkleNode;
import io.github.steveplays28.dynamictreesfabric.trees.Species;
import io.github.steveplays28.dynamictreesfabric.util.SafeChunkBounds;
import io.github.steveplays28.dynamictreesfabric.util.SimpleVoxmap;
import io.github.steveplays28.dynamictreesfabric.worldgen.JoCode;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.particle.DefaultParticleType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;

import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class TreeHelper {

	public static final TreePart NULL_TREE_PART = new NullTreePart();

	///////////////////////////////////////////
	//CONVENIENCE METHODS
	///////////////////////////////////////////

	/**
	 * Convenience method to pulse a single growth cycle and age the cuboid volume. Used by growth potions, fertilizers
	 * and the dendrocoil.
	 *
	 * @param world
	 * @param rootPos
	 */
	public static void growPulse(World world, BlockPos rootPos) {
		BlockState rootyState = world.getBlockState(rootPos);
		RootyBlock dirt = TreeHelper.getRooty(rootyState);
		if (dirt != null) {
			dirt.updateTree(rootyState, world, rootPos, world.random, false);
			ageVolume(world, rootPos, 8, 32, 1, SafeChunkBounds.ANY);//blindly age a cuboid volume
		}
	}

	/**
	 * Pulses an entire leafMap volume of blocks each with an age signal. Warning: CPU intensive and should be used
	 * sparingly.
	 *
	 * @param world      The {@link WorldAccess} instance.
	 * @param leafMap    The voxel map of hydro values to use as an iterator.
	 * @param iterations The number of times to age the volume.
	 */
	public static void ageVolume(WorldAccess world, SimpleVoxmap leafMap, int iterations, SafeChunkBounds safeBounds) {

		//The iterMap is the voxmap we will use as a discardable.  The leafMap must survive for snow
		SimpleVoxmap iterMap = leafMap != null ? new SimpleVoxmap(leafMap) : null;
		Iterable<BlockPos.Mutable> iterable = iterMap.getAllNonZero();

		for (int i = 0; i < iterations; i++) {
			for (BlockPos.Mutable iPos : iterable) {
				BlockState blockState = world.getBlockState(iPos);
				Block block = blockState.getBlock();
				if (block instanceof DynamicLeavesBlock) {//Special case for leaves
					int prevHydro = leafMap.getVoxel(iPos);//The leafMap should contain accurate hydro data
					int newHydro = ((Ageable) block).age(world, iPos, blockState, world.getRandom(), safeBounds);//Get new values from neighbors
					if (newHydro == -1) {
						//Leaf block died.  Take it out of the leafMap and iterMap
						leafMap.setVoxel(iPos, (byte) 0);
						iterMap.setVoxel(iPos, (byte) 0);
					} else {
						//Leaf did not die so the block is still leaves
						if (prevHydro == newHydro) { //But it didn't change
							iterMap.setVoxel(iPos, (byte) 0); //Stop iterating over it if it's not changing
						} else {//Oh wait.. it did change
							//Update both maps with this new hydro value
							leafMap.setVoxel(iPos, (byte) newHydro);
							iterMap.setVoxel(iPos, (byte) newHydro);
							//Copy all the surrounding values from the leafMap to the iterMap since they now also have potential to change
							for (Direction dir : Direction.values()) {
								BlockPos dPos = iPos.offset(dir);
								iterMap.setVoxel(dPos, leafMap.getVoxel(dPos));
							}
						}
					}
				} else if (block instanceof Ageable) {//Treat as just a regular ageable block
					((Ageable) block).age(world, iPos, blockState, world.getRandom(), safeBounds);
				} else {//You're not supposed to be here
					leafMap.setVoxel(iPos, (byte) 0);
					iterMap.setVoxel(iPos, (byte) 0);
				}
			}
		}

	}

	/**
	 * Pulses an entire cuboid volume of blocks each with an age signal. Warning: CPU intensive and should be used
	 * sparingly
	 *
	 * @param world      The world
	 * @param treePos    The position of the bottom most block of a trees trunk
	 * @param halfWidth  The "radius" of the cuboid volume
	 * @param height     The height of the cuboid volume
	 * @param iterations The number of times to age the volume
	 */
	public static void ageVolume(WorldAccess world, BlockPos treePos, int halfWidth, int height, int iterations, SafeChunkBounds safeBounds) {
		//Slow and dirty iteration over a cuboid volume.  Try to avoid this by using a voxmap if you can
		Iterable<BlockPos> iterable = BlockPos.iterate(treePos.add(new BlockPos(-halfWidth, 0, -halfWidth)), treePos.add(new BlockPos(halfWidth, height, halfWidth)));
		for (int i = 0; i < iterations; i++) {
			for (BlockPos iPos : iterable) {
				BlockState blockState = world.getBlockState(iPos);
				Block block = blockState.getBlock();
				if (block instanceof Ageable) {
					((Ageable) block).age(world, iPos, blockState, world.getRandom(), safeBounds);//Treat as just a regular ageable block
				}
			}
		}

	}

	public static Optional<JoCode> getJoCode(World world, BlockPos pos) {
		return getJoCode(world, pos, Direction.SOUTH);
	}

	public static Optional<JoCode> getJoCode(World world, BlockPos pos, Direction direction) {
		if (pos == null) {
			return Optional.empty();
		}
		pos = dereferenceTrunkShell(world, pos);
		BlockPos rootPos = TreeHelper.findRootNode(world, pos);
		return rootPos != BlockPos.ORIGIN ? Optional.of(new JoCode(world, rootPos, direction)) : Optional.empty();
	}

	public static BlockPos dereferenceTrunkShell(World world, BlockPos pos) {

		BlockState blockState = world.getBlockState(pos);

		if (blockState.getBlock() instanceof TrunkShellBlock) {
			TrunkShellBlock.ShellMuse muse = ((TrunkShellBlock) blockState.getBlock()).getMuse(world, blockState, pos);
			if (muse != null) {
				return muse.pos;
			}
		}

		return pos;
	}

	public static Species getCommonSpecies(World world, BlockPos pos) {
		pos = dereferenceTrunkShell(world, pos);
		BlockState state = world.getBlockState(pos);
		if (state.getBlock() instanceof BranchBlock) {
			BranchBlock branch = (BranchBlock) state.getBlock();
			return branch.getFamily().getCommonSpecies();
		}

		return Species.NULL_SPECIES;
	}

	/**
	 * This is resource intensive.  Use only for interaction code. Only the root node can determine the exact species
	 * and it has to be found by mapping the branch network.
	 *
	 * @param world The {@link World} instance.
	 * @param pos   The {@link BlockPos} to find the {@link Species} at.
	 * @return The {@link Species}, or {@link Species#NULL_SPECIES} if one couldn't be found.
	 */
	public static Species getExactSpecies(World world, BlockPos pos) {
		BlockPos rootPos = findRootNode(world, pos);

		if (rootPos != BlockPos.ORIGIN) {
			BlockState rootyState = world.getBlockState(rootPos);
			return TreeHelper.getRooty(rootyState).getSpecies(rootyState, world, rootPos);
		}
		return Species.NULL_SPECIES;
	}

	/**
	 * This is resource intensive.  Use only for interaction code. Only the root node can determine the exact species
	 * and it has to be found by mapping the branch network.  Tries to find the exact species and if that fails tries to
	 * find the common species.
	 *
	 * @param world
	 * @param pos
	 * @return
	 */
	public static Species getBestGuessSpecies(World world, BlockPos pos) {
		Species species = getExactSpecies(world, pos);
		return species == Species.NULL_SPECIES ? getCommonSpecies(world, pos) : species;
	}

	/**
	 * Find the root node of a tree.
	 *
	 * @param world The world
	 * @param pos   The position being analyzed
	 * @return The position of the root node of the tree or BlockPos.ZERO if nothing was found.
	 */
	public static BlockPos findRootNode(World world, BlockPos pos) {

		pos = dereferenceTrunkShell(world, pos);
		BlockState state = world.getBlockState(pos);
		TreePart treePart = TreeHelper.getTreePart(world.getBlockState(pos));

		switch (treePart.getTreePartType()) {
			case BRANCH:
				MapSignal signal = treePart.analyse(state, world, pos, null, new MapSignal());// Analyze entire tree network to find root node
				if (signal.foundRoot) {
					return signal.root;
				}
				break;
			case ROOT:
				return pos;
			default:
				return BlockPos.ORIGIN;
		}

		return BlockPos.ORIGIN;
	}

	/**
	 * Provided as a means for an implementation to chain the handlers.
	 *
	 * @return The currently defined {@link RootyBlockDecayer} handler.
	 */
	public static RootyBlockDecayer getCustomRootBlockDecay() {
		return RootyBlock.rootyBlockDecayer;
	}

	/**
	 * Sets a custom rooty block decay (what dirt it becomes when the tree is gone) algorithm for mods that have special
	 * requirements.
	 *
	 * @param decay The {@link RootyBlockDecayer} implementation.
	 */
	public static void setCustomRootBlockDecay(RootyBlockDecayer decay) {
		RootyBlock.rootyBlockDecayer = decay;
	}

	/**
	 * Convenience function that spawns particles all over the tree branches
	 *
	 * @param world
	 * @param rootPos
	 * @param type
	 * @param num
	 */
	public static void treeParticles(World world, BlockPos rootPos, DefaultParticleType type, int num) {
		if (world.isClient) {
			startAnalysisFromRoot(world, rootPos, new MapSignal(new TwinkleNode(type, num)));
		}
	}

	public static void rootParticles(World world, BlockPos rootPos, Direction offset, DefaultParticleType type, int num) {
		if (world.isClient) {
			if (world.isClient() && world.getBlockState(rootPos).getBlock() instanceof RootyBlock) {
				final BlockPos particlePos = rootPos.add(offset.getVector());
				DTClient.spawnParticles(world, type, particlePos.getX(), particlePos.getY(), particlePos.getZ(), num, world.getRandom());
			}
		}
	}

	/**
	 * Convenience function that verifies an analysis is starting from the root node before commencing.
	 *
	 * @param world   The world
	 * @param rootPos The position of the rootyBlock
	 * @param signal  The signal carrying the inspectors
	 * @return true if a root block was found.
	 */
	public static boolean startAnalysisFromRoot(WorldAccess world, BlockPos rootPos, MapSignal signal) {
		RootyBlock dirt = TreeHelper.getRooty(world.getBlockState(rootPos));
		if (dirt != null) {
			dirt.startAnalysis(world, rootPos, signal);
			return true;
		}
		return false;
	}

	//Treeparts

	public static boolean isTreePart(Block block) {
		return block instanceof TreePart;
	}

	public static boolean isTreePart(BlockState blockState) {
		return isTreePart(blockState.getBlock());
	}

	public static boolean isTreePart(WorldAccess blockAccess, BlockPos pos) {
		return isTreePart(blockAccess.getBlockState(pos).getBlock());
	}

	public static TreePart getTreePart(Block block) {
		return isTreePart(block) ? (TreePart) block : NULL_TREE_PART;
	}

	public static TreePart getTreePart(BlockState blockState) {
		return getTreePart(blockState.getBlock());
	}


	//Branches

	public static boolean isBranch(Block block) {
		return block instanceof BranchBlock;//Oh shuddap you java purists.. this is minecraft!
	}

	public static boolean isBranch(@Nullable final BlockState state) {
		return state != null && isBranch(state.getBlock());
	}

	@Nullable
	public static BranchBlock getBranch(Block block) {
		return isBranch(block) ? (BranchBlock) block : null;
	}

	@Nullable
	public static BranchBlock getBranch(TreePart treepart) {
		return treepart instanceof BranchBlock ? (BranchBlock) treepart : null;
	}

	@Nullable
	public static BranchBlock getBranch(BlockState state) {
		return getBranch(state.getBlock());
	}

	public static int getRadius(BlockView access, BlockPos pos) {
		BlockState state = access.getBlockState(pos);
		return getTreePart(state).getRadius(state);
	}

	public static Optional<BranchBlock> getBranchOpt(Block block) {
		return isBranch(block) ? Optional.of((BranchBlock) block) : Optional.empty();
	}

	public static Optional<BranchBlock> getBranchOpt(BlockState state) {
		final Block block = state.getBlock();
		return isBranch(block) ? Optional.of((BranchBlock) block) : Optional.empty();
	}

	public static Optional<BranchBlock> getBranchOpt(TreePart treepart) {
		return treepart instanceof BranchBlock ? Optional.of((BranchBlock) treepart) : Optional.empty();
	}

	public static Optional<RootyBlock> getRootyOpt(BlockState blockState) {
		Block block = blockState.getBlock();
		return isRooty(block) ? Optional.of((RootyBlock) block) : Optional.empty();
	}

	//Leaves

	public static boolean isLeaves(Block block) {
		return block instanceof DynamicLeavesBlock;
	}

	public static boolean isLeaves(BlockState blockState) {
		return isLeaves(blockState.getBlock());
	}

	@Nullable
	public static DynamicLeavesBlock getLeaves(Block block) {
		return isLeaves(block) ? (DynamicLeavesBlock) block : null;
	}

	@Nullable
	public static DynamicLeavesBlock getLeaves(TreePart treepart) {
		return treepart instanceof DynamicLeavesBlock ? (DynamicLeavesBlock) treepart : null;
	}

	@Nullable
	public static DynamicLeavesBlock getLeaves(BlockState state) {
		return getLeaves(state.getBlock());
	}

	//Rooty

	public static boolean isRooty(Block block) {
		return block instanceof RootyBlock;
	}

	public static boolean isRooty(BlockState blockState) {
		return isRooty(blockState.getBlock());
	}

	@Nullable
	public static RootyBlock getRooty(Block block) {
		return isRooty(block) ? (RootyBlock) block : null;
	}

	@Nullable
	public static RootyBlock getRooty(TreePart treepart) {
		return treepart instanceof RootyBlock ? (RootyBlock) treepart : null;
	}

	@Nullable
	public static RootyBlock getRooty(BlockState blockState) {
		return getRooty(blockState.getBlock());
	}

}
