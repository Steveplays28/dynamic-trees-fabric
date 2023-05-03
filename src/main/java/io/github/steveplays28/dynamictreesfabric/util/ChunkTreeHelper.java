package io.github.steveplays28.dynamictreesfabric.util;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import javax.annotation.Nullable;

import io.github.steveplays28.dynamictreesfabric.api.TreeHelper;
import io.github.steveplays28.dynamictreesfabric.api.network.MapSignal;
import io.github.steveplays28.dynamictreesfabric.blocks.DynamicCocoaBlock;
import io.github.steveplays28.dynamictreesfabric.blocks.FruitBlock;
import io.github.steveplays28.dynamictreesfabric.blocks.branches.BranchBlock;
import io.github.steveplays28.dynamictreesfabric.blocks.branches.SurfaceRootBlock;
import io.github.steveplays28.dynamictreesfabric.blocks.rootyblocks.RootyBlock;
import io.github.steveplays28.dynamictreesfabric.entities.FallingTreeEntity;
import io.github.steveplays28.dynamictreesfabric.systems.nodemappers.CollectorNode;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.SnowBlock;
import net.minecraft.block.VineBlock;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import net.minecraft.world.chunk.ChunkSection;
import net.minecraft.world.chunk.WorldChunk;

/**
 * @author ferreusveritas
 */
public class ChunkTreeHelper {

	private static final int CHUNK_WIDTH = 16;
	private static final byte NONE = (byte) 0;
	private static final byte TREE = (byte) 1;
	private static final byte SURR = (byte) 2;

	/**
	 * Removes floating little bits of tree that have somehow lost connection with their parent root system.
	 *
	 * @param chunkPos the chunk position where the effect is intended
	 * @param radius   radius of effect in chunk width units
	 */
	public static int removeOrphanedBranchNodes(World level, @Nullable ChunkPos chunkPos, int radius) {
		if (chunkPos == null) {
			throw new NullPointerException("Null chunk position");
		}

		Set<BlockPos> found = new HashSet<>(); // This is used to track branches that are already proven
		final BlockBounds bounds = getEffectiveBlockBounds(level, chunkPos, radius);
		int orphansCleared = 0;

		for (BlockPos pos : bounds) {
			final BlockState state = level.getBlockState(pos);
			final Optional<BranchBlock> branchBlock = TreeHelper.getBranchOpt(state);

			if (branchBlock.isEmpty()) {
				continue; // No branch block found at this position.  Move on
			}

			// Test if the branch has a root node attached to it
			BlockPos rootPos = TreeHelper.findRootNode(level, pos);
			if (rootPos == BlockPos.ORIGIN) { // If the root position is the ORIGIN object it means that no root block was found
				// If the root node isn't found then all nodes are orphan.  Destroy the entire network.
				doTreeDestroy(level, branchBlock.get(), pos);
				orphansCleared++;
				continue;
			}

			// There is at least one root block in the network
			BlockState rootyState = level.getBlockState(rootPos);
			Optional<RootyBlock> rootyBlock = TreeHelper.getRootyOpt(rootyState);
			if (rootyBlock.isEmpty()) {
				continue; // This theoretically shouldn't ever happen
			}

			// Rooty block confirmed, build details about the trunk coming out of it
			Direction trunkDir = rootyBlock.get().getTrunkDirection(level, rootPos);
			BlockPos trunkPos = rootPos.offset(trunkDir);
			BlockState trunkState = level.getBlockState(trunkPos);
			Optional<BranchBlock> trunk = TreeHelper.getBranchOpt(trunkState);

			if (trunk.isEmpty()) {
				continue; // This theoretically shouldn't ever happen
			}

			// There's a trunk coming out of the rooty block, that's kinda expected.  But is it the only rooty block in the network?
			MapSignal signal = new MapSignal();
			signal.destroyLoopedNodes = false;
			trunk.get().analyse(trunkState, level, trunkPos, null, signal);
			if (signal.multiroot || signal.overflow) { // We found multiple root nodes.  This can't be resolved. Destroy the entire network
				doTreeDestroy(level, branchBlock.get(), pos);
				orphansCleared++;
			} else { // Tree appears healthy with only a single attached root block
				trunk.get().analyse(trunkState, level, trunkPos, null, new MapSignal(new CollectorNode(found)));
			}
		}

		return orphansCleared;
	}

	public static int removeAllBranchesFromChunk(World level, @Nullable ChunkPos chunkPos, int radius) {
		if (chunkPos == null) {
			throw new NullPointerException("Null chunk position");
		}

		final BlockBounds bounds = getEffectiveBlockBounds(level, chunkPos, radius);
		final AtomicInteger treesCleared = new AtomicInteger();

		for (BlockPos pos : bounds) {
			BlockState state = level.getBlockState(pos);
			TreeHelper.getBranchOpt(state).ifPresent(branchBlock -> {
				doTreeDestroy(level, branchBlock, pos);
				treesCleared.getAndIncrement();
			});
		}

		return treesCleared.get();
	}

	public static BlockBounds getEffectiveBlockBounds(World level, ChunkPos chunkPos, int radius) {
		WorldChunk chunk = level.getChunk(chunkPos.x, chunkPos.z);
		BlockBounds bounds = new BlockBounds(level, chunkPos);

		bounds.shrink(Direction.UP, (level.getHeight() - 1) - (getTopFilledSegment(chunk) + 16));
		for (Direction dir : Direction.Type.HORIZONTAL.stream().toList()) {
			bounds.expand(dir, radius * CHUNK_WIDTH);
		}

		return bounds;
	}

	private static int getTopFilledSegment(final WorldChunk chunk) {
		final ChunkSection lastChunkSection = getLastSection(chunk);
		return lastChunkSection == null ? 0 : lastChunkSection.getYOffset();
	}

	@Nullable
	private static ChunkSection getLastSection(final WorldChunk chunk) {
		final ChunkSection[] sections = chunk.getSectionArray();

		for (int i = sections.length - 1; i >= 0; i--) {
			if (sections[i] != null && !sections[i].isEmpty()) {
				return sections[i];
			}
		}

		return null;
	}

	private static void doTreeDestroy(World level, BranchBlock branchBlock, BlockPos pos) {
		BranchDestructionData destroyData = branchBlock.destroyBranchFromNode(level, pos, Direction.DOWN, true, null);
		destroyData.leavesDrops.clear(); // Prevent dropped seeds from planting themselves again
		FallingTreeEntity.dropTree(level, destroyData, new ArrayList<>(0),
				FallingTreeEntity.DestroyType.ROOT); // Destroy the tree client side without fancy effects
		cleanupNeighbors(level, destroyData);
	}

	public static void cleanupNeighbors(World level, BranchDestructionData destroyData) {

		// Only run on the server since the block updates will come from the server anyway
		if (level.isClient) {
			return;
		}

		// Get the bounds of the tree, all leaves and branches but not the rooty block
		BlockBounds treeBounds = new BlockBounds(destroyData.cutPos);
		destroyData.getPositions(BranchDestructionData.PosType.LEAVES, true).forEach(treeBounds::union);
		destroyData.getPositions(BranchDestructionData.PosType.BRANCHES, true).forEach(treeBounds::union);
		treeBounds.expand(1); // Expand by one to contain the 3d "outline" of the voxels

		// Mark voxels for leaves or branch blocks
		SimpleVoxmap treeVoxmap = new SimpleVoxmap(treeBounds);
		destroyData.getPositions(BranchDestructionData.PosType.LEAVES, true)
				.forEach(pos -> treeVoxmap.setVoxel(pos, TREE));
		destroyData.getPositions(BranchDestructionData.PosType.BRANCHES, true)
				.forEach(pos -> treeVoxmap.setVoxel(pos, TREE));

		// Set voxels in the outline map for any adjacent voxels from the source tree map
		SimpleVoxmap outlineVoxmap = new SimpleVoxmap(treeVoxmap);
		treeVoxmap.getAllNonZero(TREE).forEach(pos -> {
			for (Direction dir : Direction.values()) {
				outlineVoxmap.setVoxel(pos.move(dir.getVector()), SURR);
			}
		});

		// Clear out the original positions of the leaves and branch blocks since they've already been deleted
		treeVoxmap.getAllNonZero(TREE).forEach(pos -> outlineVoxmap.setVoxel(pos, NONE));

		// Finally use this map for cleaning up marked block positions
		outlineVoxmap.getAllNonZero(SURR).forEach(pos -> cleanupBlock(level, pos));
	}

	/**
	 * Cleanup blocks that are attached(or setting on) various parts of the tree
	 */
	public static void cleanupBlock(World level, BlockPos pos) {
		BlockState state = level.getBlockState(pos);
		if (state.getBlock() == Blocks.AIR) { // This is the most likely case so bail early
			return;
		}

		Block block = state.getBlock();

		// Cleanup snow layers, hanging fruit(apples), trunk fruit(cocoa), and surface roots.
		if (block instanceof SnowBlock || block instanceof FruitBlock || block instanceof DynamicCocoaBlock ||
				block instanceof SurfaceRootBlock) {
			level.setBlockState(pos, BlockStates.AIR, Block.NOTIFY_LISTENERS);
		} else if (block instanceof VineBlock) {
			// Cleanup vines
			cleanupVines(level, pos);
		}
	}

	/**
	 * Cleanup vines starting the the top and moving down until a vine block is no longer found
	 */
	public static void cleanupVines(World level, BlockPos pos) {
		BlockPos.Mutable mblock = pos.mutableCopy(); // Mutable because ZOOM!
		while (level.getBlockState(mblock)
				.getBlock() instanceof VineBlock) {// BlockVine instance helps with modded vine types
			level.setBlockState(mblock, BlockStates.AIR, Block.NOTIFY_LISTENERS);
			mblock.move(Direction.DOWN);
		}
	}

}
