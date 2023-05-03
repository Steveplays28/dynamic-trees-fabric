package io.github.steveplays28.dynamictreesfabric.blocks.branches;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import io.github.steveplays28.dynamictreesfabric.api.TreeHelper;
import io.github.steveplays28.dynamictreesfabric.trees.Family;
import io.github.steveplays28.dynamictreesfabric.util.CoordUtils;
import io.github.steveplays28.dynamictreesfabric.util.RootConnections;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.Material;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.Waterloggable;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.IntProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.function.BooleanBiFunction;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockRenderView;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.tick.OrderedTick;

@SuppressWarnings("deprecation")
public class SurfaceRootBlock extends Block implements Waterloggable {

	public static final int MAX_RADIUS = 8;
	public static final BooleanProperty GROUNDED = BooleanProperty.of("grounded");
	public static final BooleanProperty WATERLOGGED = Properties.WATERLOGGED;
	protected static final IntProperty RADIUS = IntProperty.of("radius", 1, MAX_RADIUS);
	private final Family family;

	public SurfaceRootBlock(Family family) {
		this(Material.WOOD, family);
		setDefaultState(getDefaultState().with(WATERLOGGED, false));
	}

	public SurfaceRootBlock(Material material, Family family) {
		super(Block.Properties.of(material)
//                .harvestTool(ToolType.AXE)
//                .harvestLevel(0)
				.strength(2.5f, 1.0F)
				.sounds(BlockSoundGroup.WOOD));

		this.family = family;
	}

	public Family getFamily() {
		return family;
	}

	@Override
	public ItemStack getCloneItemStack(BlockState state, HitResult target, BlockView world, BlockPos pos, PlayerEntity player) {
		return this.family.getBranchItem().map(ItemStack::new).orElse(ItemStack.EMPTY);
	}

	@Override
	protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
		builder.add(RADIUS, GROUNDED, WATERLOGGED);
	}

	///////////////////////////////////////////
	// BLOCK STATES
	///////////////////////////////////////////

	public int getRadius(BlockState blockState) {
		return blockState.getBlock() == this ? blockState.get(RADIUS) : 0;
	}

	public int setRadius(WorldAccess world, BlockPos pos, int radius, int flags) {
		boolean replacingWater = world.getBlockState(pos).getFluidState() == Fluids.WATER.getStill(false);
		world.setBlockState(pos, this.getStateForRadius(radius).with(WATERLOGGED, replacingWater), flags);
		return radius;
	}

	public BlockState getStateForRadius(int radius) {
		return this.getDefaultState().with(RADIUS, MathHelper.clamp(radius, 0, getMaxRadius()));
	}

	public int getMaxRadius() {
		return MAX_RADIUS;
	}

	public int getRadialHeight(int radius) {
		return radius * 2;
	}

	@Override
	public FluidState getFluidState(BlockState state) {
		return state.get(WATERLOGGED) ? Fluids.WATER.getStill(false) : super.getFluidState(state);
	}

	///////////////////////////////////////////
	// WATER LOGGING
	///////////////////////////////////////////

	@Override
	public BlockState getStateForNeighborUpdate(BlockState stateIn, Direction facing, BlockState facingState, WorldAccess worldIn, BlockPos currentPos, BlockPos facingPos) {
		if (stateIn.get(WATERLOGGED)) {
			worldIn.getFluidTickScheduler().scheduleTick(new OrderedTick<>(Fluids.WATER, currentPos, Fluids.WATER.getTickRate(worldIn), 1));
		}
		return super.getStateForNeighborUpdate(stateIn, facing, facingState, worldIn, currentPos, facingPos);
	}

	public RootConnections getConnectionData(final BlockRenderView world, final BlockPos pos) {
		final RootConnections connections = new RootConnections();

		for (Direction dir : CoordUtils.HORIZONTALS) {
			final RootConnection connection = this.getSideConnectionRadius(world, pos, dir);

			if (connection == null) {
				continue;
			}

			connections.setRadius(dir, connection.radius);
			connections.setConnectionLevel(dir, connection.level);
		}

		return connections;
	}

	///////////////////////////////////////////
	// RENDERING
	///////////////////////////////////////////

	@NotNull
	@Override
	public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
		boolean connectionMade = false;
		final int thisRadius = getRadius(state);

		VoxelShape shape = VoxelShapes.empty();

		for (Direction dir : CoordUtils.HORIZONTALS) {
			final RootConnection conn = this.getSideConnectionRadius(world, pos, dir);

			if (conn == null) {
				continue;
			}

			connectionMade = true;
			final int r = MathHelper.clamp(conn.radius, 1, thisRadius);
			final double radius = r / 16.0;
			final double radialHeight = getRadialHeight(r) / 16.0;
			final double gap = 0.5 - radius;

			Box aabb = new Box(-radius, 0, -radius, radius, radialHeight, radius);
			aabb = aabb.stretch(dir.getOffsetX() * gap, 0, dir.getOffsetZ() * gap).offset(0.5, 0.0, 0.5);
			shape = VoxelShapes.combine(shape, VoxelShapes.cuboid(aabb), BooleanBiFunction.OR);
		}

		if (!connectionMade) {
			double radius = thisRadius / 16.0;
			double radialHeight = getRadialHeight(thisRadius) / 16.0;
			Box aabb = new Box(0.5 - radius, 0, 0.5 - radius, 0.5 + radius, radialHeight, 0.5 + radius);
			shape = VoxelShapes.combine(shape, VoxelShapes.cuboid(aabb), BooleanBiFunction.OR);
		}

		return shape;
	}


	///////////////////////////////////////////
	// PHYSICAL BOUNDS
	///////////////////////////////////////////

	private boolean isAirOrWater(BlockState state) {
		return state.getBlock() == Blocks.AIR || state.getBlock() == Blocks.WATER;
	}

	@Nullable
	protected RootConnection getSideConnectionRadius(BlockView blockReader, BlockPos pos, Direction side) {
		if (!side.getAxis().isHorizontal()) {
			return null;
		}

		BlockPos dPos = pos.offset(side);
		BlockState state = CoordUtils.getStateSafe(blockReader, dPos);
		final BlockState upState = CoordUtils.getStateSafe(blockReader, pos.up());

		final RootConnections.ConnectionLevel level = (upState != null && isAirOrWater(upState) && state != null && state.isSolidBlock(blockReader, dPos)) ?
				RootConnections.ConnectionLevel.HIGH : (state != null && isAirOrWater(state) ? RootConnections.ConnectionLevel.LOW : RootConnections.ConnectionLevel.MID);

		if (level != RootConnections.ConnectionLevel.MID) {
			dPos = dPos.up(level.getYOffset());
			state = CoordUtils.getStateSafe(blockReader, dPos);
		}

		if (state != null && state.getBlock() instanceof SurfaceRootBlock) {
			return new RootConnection(level, ((SurfaceRootBlock) state.getBlock()).getRadius(state));
		} else if (level == RootConnections.ConnectionLevel.MID && TreeHelper.isBranch(state) && TreeHelper.getTreePart(state).getRadius(state) >= 8) {
			return new RootConnection(RootConnections.ConnectionLevel.MID, 8);
		}

		return null;
	}

	@Override
	public boolean onDestroyedByPlayer(BlockState state, World world, BlockPos pos, PlayerEntity player, boolean willHarvest, FluidState fluid) {
		final BlockState upstate = world.getBlockState(pos.up());

		if (upstate.getBlock() instanceof TrunkShellBlock) {
			world.setBlockState(pos, upstate);
		}

		for (Direction dir : CoordUtils.HORIZONTALS) {
			final BlockPos dPos = pos.offset(dir).down();
			world.getBlockState(dPos).neighborUpdate(world, dPos, this, pos, false);
		}

		return super.onDestroyedByPlayer(state, world, pos, player, willHarvest, fluid);
	}

	@Override
	public void neighborUpdate(BlockState state, World world, BlockPos pos, Block blockIn, BlockPos fromPos, boolean isMoving) {
		if (!canBlockStay(world, pos, state)) {
			world.removeBlock(pos, false);
		}
	}

	protected boolean canBlockStay(World world, BlockPos pos, BlockState state) {
		final BlockPos below = pos.down();
		final BlockState belowState = world.getBlockState(below);

		final int radius = getRadius(state);

		if (belowState.isSolidBlock(world, below)) { // If a root is sitting on a solid block.
			for (Direction dir : CoordUtils.HORIZONTALS) {
				final RootConnection conn = this.getSideConnectionRadius(world, pos, dir);

				if (conn != null && conn.radius > radius) {
					return true;
				}
			}
		} else { // If the root has no solid block under it.
			boolean connections = false;

			for (Direction dir : CoordUtils.HORIZONTALS) {
				final RootConnection conn = this.getSideConnectionRadius(world, pos, dir);

				if (conn == null) {
					continue;
				}

				if (conn.level == RootConnections.ConnectionLevel.MID) {
					return false;
				}

				if (conn.radius > radius) {
					connections = true;
				}
			}

			return connections;
		}

		return false;
	}

	public static class RootConnection {
		public RootConnections.ConnectionLevel level;
		public int radius;

		public RootConnection(RootConnections.ConnectionLevel level, int radius) {
			this.level = level;
			this.radius = radius;
		}

		@Override
		public String toString() {
			return super.toString() + " Level: " + this.level.toString() + " Radius: " + this.radius;
		}
	}

}
