package io.github.steveplays28.dynamictreesfabric.blocks.branches;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import io.github.steveplays28.dynamictreesfabric.api.TreeHelper;
import io.github.steveplays28.dynamictreesfabric.api.cells.Cell;
import io.github.steveplays28.dynamictreesfabric.api.cells.CellNull;
import io.github.steveplays28.dynamictreesfabric.api.network.MapSignal;
import io.github.steveplays28.dynamictreesfabric.api.treedata.TreePart;
import io.github.steveplays28.dynamictreesfabric.blocks.leaves.DynamicLeavesBlock;
import io.github.steveplays28.dynamictreesfabric.blocks.leaves.LeavesProperties;
import io.github.steveplays28.dynamictreesfabric.cells.MetadataCell;
import io.github.steveplays28.dynamictreesfabric.growthlogic.context.DirectionSelectionContext;
import io.github.steveplays28.dynamictreesfabric.init.DTConfigs;
import io.github.steveplays28.dynamictreesfabric.systems.GrowSignal;
import io.github.steveplays28.dynamictreesfabric.trees.Family;
import io.github.steveplays28.dynamictreesfabric.trees.Species;
import io.github.steveplays28.dynamictreesfabric.util.CoordUtils;

import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.Material;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.Waterloggable;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.IntProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.WorldView;

@SuppressWarnings("deprecation")
public class BasicBranchBlock extends BranchBlock implements Waterloggable {

	public static final BooleanProperty WATERLOGGED = Properties.WATERLOGGED;
	protected static final IntProperty RADIUS = IntProperty.of("radius", 1, MAX_RADIUS);
	/**
	 * Stores a cache of the {@link BlockState}s for rapid lookup. Created by {@link
	 * #createBranchStates(IntProperty, int)}.
	 */
	protected final BlockState[] branchStates;
	private final int maxRadiusForWaterLogging = 7; //the maximum radius for a branch to be allowed to be water logged
	private int flammability = 5; // Mimic vanilla logs
	private int fireSpreadSpeed = 5; // Mimic vanilla logs

	public BasicBranchBlock(Material material) {
		this(AbstractBlock.Settings.of(material).sounds(BlockSoundGroup.WOOD), RADIUS, MAX_RADIUS);
	}

	public BasicBranchBlock(Settings properties) {
		this(properties, RADIUS, MAX_RADIUS);
	}

	// Useful for more unique subclasses
	public BasicBranchBlock(AbstractBlock.Settings properties, final IntProperty radiusProperty, final int maxRadius) {
		super(properties);

		// Create branch state cache.
		this.branchStates = this.createBranchStates(radiusProperty, maxRadius);
	}

	/**
	 * Creates a cache of {@link BlockState}s for the given {@link IntProperty} up to the given {@code maxRadius}.
	 *
	 * @param radiusProperty The {@link IntProperty} for the radius.
	 * @param maxRadius      The maximum radius (must be the same as max for {@link IntProperty}.
	 * @return The {@code array} cache of {@link BlockState}s.
	 */
	public BlockState[] createBranchStates(final IntProperty radiusProperty, final int maxRadius) {
		this.setDefaultState(this.stateManager.getDefaultState().with(radiusProperty, 1).with(WATERLOGGED, false));

		final BlockState[] branchStates = new BlockState[maxRadius + 1];

		// Cache the branch blocks states for rapid lookup.
		branchStates[0] = Blocks.AIR.getDefaultState();

		for (int radius = 1; radius <= maxRadius; radius++) {
			branchStates[radius] = getDefaultState().with(radiusProperty, radius);
		}

		return branchStates;
	}

	@Override
	public BlockSoundGroup getSoundType(BlockState state, WorldView world, BlockPos pos, @Nullable Entity entity) {
		return getFamily().getBranchSoundType(state, world, pos, entity);
	}

	///////////////////////////////////////////
	// BLOCKSTATES
	///////////////////////////////////////////

	protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
		builder.add(RADIUS).add(WATERLOGGED);
	}

	///////////////////////////////////////////
	// TREE INFORMATION
	///////////////////////////////////////////

	@Override
	public int branchSupport(BlockState state, BlockView reader, BranchBlock branch, BlockPos pos, Direction dir, int radius) {
		return isSameTree(branch) ? BasicBranchBlock.setSupport(1, 1) : 0;// Other branches of the same type are always valid support.
	}

	@Override
	public boolean canFall() {
		return true;
	}


	///////////////////////////////////////////
	// WORLD UPDATE
	///////////////////////////////////////////

	@Override
	public boolean checkForRot(WorldAccess world, BlockPos pos, Species species, int fertility, int radius, Random rand, float chance, boolean rapid) {

		if (!rapid && (chance == 0.0f || rand.nextFloat() > chance)) {
			return false;//Bail out if not in rapid mode and the postRot chance fails
		}

		// Rooty dirt below the block counts as a branch in this instance
		// Rooty dirt below for saplings counts as 2 neighbors if the soil is not infertile
		int neigh = 0;// High Nybble is count of branches, Low Nybble is any reinforcing treepart(including branches)

		for (Direction dir : Direction.values()) {
			BlockPos deltaPos = pos.offset(dir);
			BlockState deltaBlockState = world.getBlockState(deltaPos);
			neigh += TreeHelper.getTreePart(deltaBlockState).branchSupport(deltaBlockState, world, this, deltaPos, dir, radius);
			if (getBranchSupport(neigh) >= 1 && getLeavesSupport(neigh) >= 2) {// Need two neighbors.. one of which must be another branch
				return false;// We've proven that this branch is reinforced so there is no need to continue
			}
		}

		boolean didRot = species.rot(world, pos, neigh & 0x0F, radius, fertility, rand, rapid, fertility > 0); // Unreinforced branches are destroyed.

		if (rapid && didRot) {// Speedily postRot back dead branches if this block rotted
			for (Direction dir : Direction.values()) {// The logic here is that if this block rotted then
				BlockPos neighPos = pos.offset(dir);// the neighbors might be rotted too.
				BlockState neighState = world.getBlockState(neighPos);
				if (neighState.getBlock() == this) { // Only check blocks logs that are the same as this one
					this.checkForRot(world, neighPos, species, fertility, getRadius(neighState), rand, 1.0f, true);
				}
			}
		}

		return didRot;
	}

	///////////////////////////////////////////
	// WATER LOGGING
	///////////////////////////////////////////

	@Override
	public FluidState getFluidState(BlockState state) {
		return state.get(WATERLOGGED) ? Fluids.WATER.getStill(false) : super.getFluidState(state);
	}

	@Override
	public BlockState getStateForNeighborUpdate(BlockState stateIn, Direction facing, BlockState facingState, WorldAccess worldIn, BlockPos currentPos, BlockPos facingPos) {
		if (stateIn.get(WATERLOGGED)) {
			worldIn.scheduleFluidTick(currentPos, Fluids.WATER, Fluids.WATER.getTickRate(worldIn));
		}
		return super.getStateForNeighborUpdate(stateIn, facing, facingState, worldIn, currentPos, facingPos);
	}

	@Override
	public boolean canFillWithFluid(BlockView world, BlockPos pos, BlockState state, Fluid fluid) {
		if (getRadius(state) > maxRadiusForWaterLogging) {
			return false;
		}
		return Waterloggable.super.canFillWithFluid(world, pos, state, fluid);
	}

	///////////////////////////////////////////
	// PHYSICAL PROPERTIES
	///////////////////////////////////////////

	@Override
	public float getHardness(BlockState state, BlockView worldIn, BlockPos pos) {
		final int radius = this.getRadius(worldIn.getBlockState(pos));
		final float hardness = this.getFamily().getPrimitiveLog().orElse(Blocks.AIR).getDefaultState()
				.getHardness(worldIn, pos) * (radius * radius) / 64.0f * 8.0f;
		return (float) Math.min(hardness, DTConfigs.MAX_TREE_HARDNESS.get()); // So many youtube let's plays start with "OMG, this is taking so long to break this tree!"
	}

	@Override
	public int getFireSpreadSpeed(BlockState state, BlockView world, BlockPos pos, Direction face) {
		int radius = getRadius(world.getBlockState(pos));
		return (fireSpreadSpeed * radius) / 8;
	}

	@Override
	public int getFlammability(BlockState state, BlockView world, BlockPos pos, Direction face) {
		return flammability;
	}

	public BasicBranchBlock setFlammability(int flammability) {
		this.flammability = flammability;
		return this;
	}

	public BasicBranchBlock setFireSpreadSpeed(int fireSpreadSpeed) {
		this.fireSpreadSpeed = fireSpreadSpeed;
		return this;
	}

	///////////////////////////////////////////
	// GROWTH
	///////////////////////////////////////////

	@Override
	public Cell getHydrationCell(BlockView reader, BlockPos pos, BlockState state, Direction dir, LeavesProperties leavesProperties) {
		final Family thisTree = getFamily();

		// The requesting leaves must match the tree for hydration to occur, and the branch must not be stripped.
		if (leavesProperties.getFamily() == thisTree) {
			int radiusAndMeta = thisTree.getRadiusForCellKit(reader, pos, state, dir, this);
			int radius = MetadataCell.getRadius(radiusAndMeta);
			int metadata = MetadataCell.getMeta(radiusAndMeta);
			return leavesProperties.getCellKit().getCellForBranch(radius, metadata);
		} else {
			return CellNull.NULL_CELL;
		}
	}

	@Override
	public int getRadius(BlockState state) {
		return isSameTree(state) ? state.get(RADIUS) : 0;
	}

	@Override
	public int setRadius(WorldAccess world, BlockPos pos, int radius, @Nullable Direction originDir, int flags) {
		destroyMode = io.github.steveplays28.dynamictreesfabric.DynamicTreesFabric.DestroyMode.SET_RADIUS;
		boolean replacingWater = world.getBlockState(pos).getFluidState() == Fluids.WATER.getStill(false);
		boolean setWaterlogged = replacingWater && radius <= maxRadiusForWaterLogging;
		world.setBlockState(pos, getStateForRadius(radius).with(WATERLOGGED, setWaterlogged), flags);
		destroyMode = io.github.steveplays28.dynamictreesfabric.DynamicTreesFabric.DestroyMode.SLOPPY;
		return radius;
	}

	@Override
	public BlockState getStateForRadius(int radius) {
		return branchStates[MathHelper.clamp(radius, 1, getMaxRadius())];
	}

	// Directionless probability grabber
	@Override
	public int probabilityForBlock(BlockState state, BlockView reader, BlockPos pos, BranchBlock from) {
		return isSameTree(from) ? getRadius(state) + 2 : 0;
	}

	public GrowSignal growIntoAir(World world, BlockPos pos, GrowSignal signal, int fromRadius) {
		final Species species = signal.getSpecies();

		final DynamicLeavesBlock leaves = species.getLeavesBlock().orElse(null);
		if (leaves != null) {
			if (fromRadius == getFamily().getPrimaryThickness()) {// If we came from a twig (and we're not a stripped branch) then just make some leaves
				signal.success = leaves.growLeavesIfLocationIsSuitable(world, species.getLeavesProperties(), pos, 0);
			} else {// Otherwise make a proper branch
				return leaves.branchOut(world, pos, signal);
			}
		} else {
			//If the leaves block is null, the branch grows directly without checking for leaves requirements
			if (isNextToBranch(world, pos, signal.dir.getOpposite())) {
				signal.success = false;
				return signal;
			}
			setRadius(world, pos, getFamily().getPrimaryThickness(), null);
			signal.radius = getFamily().getSecondaryThickness();
			signal.success = true;
		}
		return signal;
	}

	@Override
	public GrowSignal growSignal(World world, BlockPos pos, GrowSignal signal) {
		// This is always placed at the beginning of every growSignal function
		if (!signal.step()) {
			return signal;
		}

		final BlockState currBlockState = world.getBlockState(pos);
		final Species species = signal.getSpecies();
		final boolean inTrunk = signal.isInTrunk();

		final Direction originDir = signal.dir.getOpposite();// Direction this signal originated from
		final Direction targetDir = species.getGrowthLogicKit().selectNewDirection( // This must be cached on the stack for proper recursion
				new DirectionSelectionContext(world, pos, species, this, signal)
		);
		signal.doTurn(targetDir);

		{
			final BlockPos deltaPos = pos.offset(targetDir);
			final BlockState deltaState = world.getBlockState(deltaPos);

			// Pass grow signal to next block in path
			final TreePart treepart = TreeHelper.getTreePart(deltaState);
			if (treepart != TreeHelper.NULL_TREE_PART) {
				signal = treepart.growSignal(world, deltaPos, signal);// Recurse
			} else if (world.isAir(deltaPos) || deltaState.getBlock() instanceof TrunkShellBlock) {
				signal = growIntoAir(world, deltaPos, signal, getRadius(currBlockState));
			}
		}

		// Calculate Branch Thickness based on neighboring branches
		float areaAccum = signal.radius * signal.radius;// Start by accumulating the branch we just came from

		for (Direction dir : Direction.values()) {
			if (!dir.equals(originDir) && !dir.equals(targetDir)) {// Don't count where the signal originated from or the branch we just came back from
				BlockPos deltaPos = pos.offset(dir);

				// If it is decided to implement a special block(like a squirrel hole, tree
				// swing, rotting, burned or infested branch, etc) then this new block could be
				// derived from BlockBranch and this works perfectly. Should even work with
				// tileEntity blocks derived from BlockBranch.
				BlockState blockState = world.getBlockState(deltaPos);
				TreePart treepart = TreeHelper.getTreePart(blockState);
				if (isSameTree(treepart)) {
					int branchRadius = treepart.getRadius(blockState);
					areaAccum += branchRadius * branchRadius;
				}
			}
		}

		//Only continue to set radii if the tree growth isn't choked out
		if (!signal.choked) {
			// Ensure that side branches are not thicker than the size of a block.  Also enforce species max thickness
			int maxRadius = inTrunk ? species.getMaxBranchRadius() : Math.min(species.getMaxBranchRadius(), MAX_RADIUS);

			// The new branch should be the square root of all of the sums of the areas of the branches coming into it.
			// But it shouldn't be smaller than it's current size(prevents the instant slimming effect when chopping off branches)
			signal.radius = MathHelper.clamp((float) Math.sqrt(areaAccum) + species.getTapering(), getRadius(currBlockState), maxRadius);// WOW!
			int targetRadius = (int) Math.floor(signal.radius);
			int setRad = setRadius(world, pos, targetRadius, originDir);
			if (setRad < targetRadius) { //We tried to set a radius but it didn't comply because something is in the way.
				signal.choked = true; //If something is in the way then it means that the tree growth is choked
			}
		}

		return signal;
	}


	///////////////////////////////////////////
	// PHYSICAL BOUNDS
	///////////////////////////////////////////

	// This is only so effective because the center of the player must be inside the block that contains the tree trunk.
	// The result is that only thin branches and trunks can be climbed.
	// We do not check if the radius is over 3 since some mods can modify this, and allow you to climb on contact.
	@Override
	public boolean isLadder(BlockState state, WorldView world, BlockPos pos, LivingEntity entity) {
		return DTConfigs.ENABLE_BRANCH_CLIMBING.get() &&
				entity instanceof PlayerEntity &&
				getFamily().branchIsLadder() &&
				(!state.contains(WATERLOGGED) || !state.get(WATERLOGGED));
	}

	@Nonnull
	@Override
	public VoxelShape getOutlineShape(BlockState state, BlockView worldIn, BlockPos pos, ShapeContext context) {
		int thisRadiusInt = getRadius(state);
		double radius = thisRadiusInt / 16.0;
		VoxelShape core = VoxelShapes.cuboid(0.5 - radius, 0.5 - radius, 0.5 - radius, 0.5 + radius, 0.5 + radius, 0.5 + radius);

		for (Direction dir : Direction.values()) {
			int sideRadiusInt = Math.min(getSideConnectionRadius(worldIn, pos, thisRadiusInt, dir), thisRadiusInt);
			double sideRadius = sideRadiusInt / 16.0f;
			if (sideRadius > 0.0f) {
				double gap = 0.5f - sideRadius;
				Box aabb = new Box(0.5 - sideRadius, 0.5 - sideRadius, 0.5 - sideRadius, 0.5 + sideRadius, 0.5 + sideRadius, 0.5 + sideRadius);
				aabb = aabb.stretch(dir.getOffsetX() * gap, dir.getOffsetY() * gap, dir.getOffsetZ() * gap);
				core = VoxelShapes.union(core, VoxelShapes.cuboid(aabb));
			}
		}

		return core;
	}

	@Override
	public int getRadiusForConnection(BlockState state, BlockView reader, BlockPos pos, BranchBlock from, Direction side, int fromRadius) {
		return getRadius(state);
	}

	protected int getSideConnectionRadius(BlockView blockAccess, BlockPos pos, int radius, Direction side) {
		final BlockPos deltaPos = pos.offset(side);
		final BlockState blockState = CoordUtils.getStateSafe(blockAccess, deltaPos);

		// If adjacent block is not loaded assume there is no connection.
		return blockState == null ? 0 : TreeHelper.getTreePart(blockState).getRadiusForConnection(blockState, blockAccess, deltaPos, this, side, radius);
	}


	///////////////////////////////////////////
	// NODE ANALYSIS
	///////////////////////////////////////////

	protected int getMaxSignalDepth() {
		return getFamily().getMaxSignalDepth();
	}

	/**
	 * This is a recursive algorithm used to explore the branch network.  It calls a run() function for the signal on
	 * the way out and a returnRun() on the way back.
	 * <p>
	 * Okay so a little explanation here.. I've been hit up by people who claim that recursion is a bad idea.  The
	 * reason why they think this is because java has to push values on the stack for each level of recursion and then
	 * pop them off as the levels complete.  Many times this can lead to performance issues. Fine, I understand that.
	 * The reason why it doesn't matter here is because of the object oriented nature of how the tree parts function
	 * demand that a different analyze function be called for each object type.  Even if this were rewritten to be
	 * iterative the same number of stack pushes and pops would need to be performed to run the custom function for each
	 * node in the network anyway.  The depth of recursion for this algorithm is less than 32.  So there's no real risk
	 * of a stack overflow.
	 * <p>
	 * The difference being that in an iterative design I would need to maintain a stack array holding all of the values
	 * and push and pop them manually or use a stack index.  This is messy and not something I would want to maintain
	 * for practically non-existent gains. Java does a pretty good job of managing the stack on its own.
	 */
	@Override
	public MapSignal analyse(BlockState blockState, WorldAccess world, BlockPos pos, @Nullable Direction fromDir, MapSignal signal) {
		// Note: fromDir will be null in the origin node

		if (signal.overflow || (signal.trackVisited && signal.doTrackingVisited(pos))) {
			return signal;
		}

		if (signal.depth++ < getMaxSignalDepth()) {// Prevents going too deep into large networks, or worse, being caught in a network loop
			signal.run(blockState, world, pos, fromDir);// Run the inspectors of choice
			for (Direction dir : Direction.values()) {// Spread signal in various directions
				if (dir != fromDir) {// don't count where the signal originated from
					BlockPos deltaPos = pos.offset(dir);

					BlockState deltaState = world.getBlockState(deltaPos);
					TreePart treePart = TreeHelper.getTreePart(deltaState);

					if (treePart.shouldAnalyse(deltaState, world, deltaPos)) {
						signal = treePart.analyse(deltaState, world, deltaPos, dir.getOpposite(), signal);

						// This should only be true for the originating block when the root node is found
						if (signal.foundRoot && signal.localRootDir == null && fromDir == null) {
							signal.localRootDir = dir;
						}
					}
				}
			}
			signal.returnRun(blockState, world, pos, fromDir);
		} else {
			BlockState state = world.getBlockState(pos);
			if (signal.destroyLoopedNodes && state.getBlock() instanceof BranchBlock) {
				BranchBlock branch = (BranchBlock) state.getBlock();
				branch.breakDeliberate(world, pos, io.github.steveplays28.dynamictreesfabric.DynamicTreesFabric.DestroyMode.OVERFLOW);// Destroy one of the offending nodes
			}
			signal.overflow = true;
		}
		signal.depth--;

		return signal;
	}

}
