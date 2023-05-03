package io.github.steveplays28.dynamictreesfabric.blocks.branches;

import java.util.function.Consumer;

import org.jetbrains.annotations.Nullable;

import io.github.steveplays28.dynamictreesfabric.blocks.BlockWithDynamicHardness;
import io.github.steveplays28.dynamictreesfabric.util.CoordUtils;
import io.github.steveplays28.dynamictreesfabric.util.CoordUtils.Surround;
import io.github.steveplays28.dynamictreesfabric.util.Null;
import net.minecraftforge.client.extensions.common.IClientBlockExtensions;

import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.Material;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.Waterloggable;
import net.minecraft.block.piston.PistonBehavior;
import net.minecraft.client.particle.ParticleManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.pathing.NavigationType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.BlockStateParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.WorldView;
import net.minecraft.world.explosion.Explosion;
import net.minecraft.world.tick.OrderedTick;
import net.minecraft.world.tick.TickPriority;

@SuppressWarnings("deprecation")
public class TrunkShellBlock extends BlockWithDynamicHardness implements Waterloggable {

	public static final EnumProperty<Surround> CORE_DIR = EnumProperty.of("coredir", Surround.class);
	public static final BooleanProperty WATERLOGGED = Properties.WATERLOGGED;

	public TrunkShellBlock() {
		super(Block.Properties.of(Material.WOOD));
		setDefaultState(getDefaultState().with(WATERLOGGED, false));
	}

	protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
		builder.add(CORE_DIR).add(WATERLOGGED);
	}

	///////////////////////////////////////////
	// BLOCKSTATE
	///////////////////////////////////////////

	@Override
	public void scheduledTick(BlockState state, ServerWorld worldIn, BlockPos pos, Random random) {
		ShellMuse muse = this.getMuseUnchecked(worldIn, state, pos);
		if (!isValid(muse)) {
			if (state.get(WATERLOGGED)) {
				worldIn.setBlockState(pos, Blocks.WATER.getDefaultState());
			} else {
				worldIn.removeBlock(pos, false);
			}
		}
	}

	@Override
	public boolean onDestroyedByPlayer(BlockState state, World world, BlockPos pos, PlayerEntity player, boolean willHarvest, FluidState fluid) {
		return Null.applyIfNonnull(this.getMuse(world, state, pos), muse -> muse.state.getBlock().onDestroyedByPlayer(muse.state, world, muse.pos, player, willHarvest, world.getFluidState(pos)), false);
	}

	///////////////////////////////////////////
	// INTERACTION
	///////////////////////////////////////////

	@Override
	public float calcBlockBreakingDelta(BlockState state, PlayerEntity player, BlockView worldIn, BlockPos pos) {
		return Null.applyIfNonnull(this.getMuse(worldIn, state, pos), muse -> muse.state.getBlock().calcBlockBreakingDelta(muse.state, player, worldIn, muse.pos), 0f);
	}

	@Override
	public float getHardness(BlockState state, BlockView world, BlockPos pos) {
		return Null.applyIfNonnull(this.getMuse(world, pos), muse -> ((BlockWithDynamicHardness) muse.state.getBlock()).getHardness(state, world, muse.pos), super.getHardness(state, world, pos));
	}

	@Override
	public BlockSoundGroup getSoundType(BlockState state, WorldView world, BlockPos pos, @Nullable Entity entity) {
		return Null.applyIfNonnull(this.getMuse(world, state, pos), muse -> muse.state.getBlock().getSoundGroup(muse.state, world, muse.pos, entity), BlockSoundGroup.WOOD);
	}

	@Override
	public float getExplosionResistance(BlockState state, BlockView world, BlockPos pos, Explosion explosion) {
		return Null.applyIfNonnull(this.getMuse(world, pos), muse -> muse.state.getBlock().getBlastResistance(world.getBlockState(pos), world, muse.pos, explosion), 0f);
	}

	@Override
	public boolean canReplace(BlockState state, ItemPlacementContext useContext) {
		final World world = useContext.getWorld();
		final BlockPos clickedPos = useContext.getBlockPos();
		if (this.museDoesNotExist(world, state, clickedPos)) {
			this.scheduleUpdateTick(world, clickedPos);
			return false;
		}
		return false;
	}

	public Surround getMuseDir(BlockState state, BlockPos pos) {
		return state.get(CORE_DIR);
	}

	public boolean museDoesNotExist(BlockView world, BlockState state, BlockPos pos) {
		final BlockPos musePos = pos.add(this.getMuseDir(state, pos).getOffset());
		return CoordUtils.getStateSafe(world, musePos) == null;
	}

	@Nullable
	public ShellMuse getMuseUnchecked(BlockView access, BlockPos pos) {
		return this.getMuseUnchecked(access, access.getBlockState(pos), pos);
	}

	@Nullable
	public ShellMuse getMuseUnchecked(BlockView access, BlockState state, BlockPos pos) {
		return this.getMuseUnchecked(access, state, pos, pos);
	}

	@Nullable
	public ShellMuse getMuseUnchecked(BlockView access, BlockState state, BlockPos pos, BlockPos originalPos) {
		final Surround museDir = getMuseDir(state, pos);
		final BlockPos musePos = pos.add(museDir.getOffset());
		final BlockState museState = CoordUtils.getStateSafe(access, musePos);

		if (museState == null) {
			return null;
		}

		final Block block = museState.getBlock();
		if (block instanceof Musable && ((Musable) block).isMusable(access, museState, musePos)) {
			return new ShellMuse(museState, musePos, museDir, musePos.subtract(originalPos));
		} else if (block instanceof TrunkShellBlock) { // If its another trunkshell, then this trunkshell is on another layer. IF they share a common direction, we return that shell's muse.
			final Vec3i offset = ((TrunkShellBlock) block).getMuseDir(museState, musePos).getOffset();
			if (new Vec3d(offset.getX(), offset.getY(), offset.getZ()).add(new Vec3d(museDir.getOffset().getX(), museDir.getOffset().getY(), museDir.getOffset().getZ())).lengthSquared() > 2.25) {
				return (((TrunkShellBlock) block).getMuseUnchecked(access, museState, musePos, originalPos));
			}
		}
		return null;
	}

	@Nullable
	public ShellMuse getMuse(BlockView access, BlockPos pos) {
		return this.getMuse(access, access.getBlockState(pos), pos);
	}

	@Nullable
	public ShellMuse getMuse(BlockView access, BlockState state, BlockPos pos) {
		final ShellMuse muse = this.getMuseUnchecked(access, state, pos);

		// Check the muse for validity.
		if (!isValid(muse)) {
			this.scheduleUpdateTick(access, pos);
		}

		return muse;
	}

	protected boolean isValid(@Nullable ShellMuse muse) {
		return muse != null && muse.getRadius() > 8;
	}

	public void scheduleUpdateTick(BlockView access, BlockPos pos) {
		if (!(access instanceof WorldAccess)) {
			return;
		}

		((WorldAccess) access).getBlockTickScheduler().scheduleTick(new OrderedTick<Block>(this, pos.toImmutable(), 0, TickPriority.HIGH, 0));
	}

	@Override
	public void neighborUpdate(BlockState state, World world, BlockPos pos, Block neighborBlock, BlockPos neighborPos, boolean p_220069_6_) {
		this.scheduleUpdateTick(world, pos);
	}

	@Override
	public VoxelShape getOutlineShape(BlockState state, BlockView reader, BlockPos pos, ShapeContext context) {
		return Null.applyIfNonnull(this.getMuse(reader, state, pos), muse -> VoxelShapes.cuboid(muse.state.getOutlineShape(reader, muse.pos).getBoundingBox().offset(muse.museOffset)), VoxelShapes.empty());
	}

	@Override
	public ItemStack getCloneItemStack(BlockState state, HitResult target, BlockView world, BlockPos pos, PlayerEntity player) {
		return Null.applyIfNonnull(this.getMuse(world, state, pos), muse -> muse.state.getBlock().getPickStack(muse.state, target, world, muse.pos, player), ItemStack.EMPTY);
	}

	@Override
	public void onBlockExploded(BlockState state, World world, BlockPos pos, Explosion explosion) {
		Null.consumeIfNonnull(this.getMuse(world, state, pos), muse -> muse.state.getBlock().onBlockExploded(muse.state, world, muse.pos, explosion));
	}

//    @Override
//    protected boolean isAir(BlockState state) {
//        if (this.museDoesNotExist(access, state, state.getBl)) {
//            this.scheduleUpdateTick(access, pos);
//            return false;
//        }
//        return false;
//    }

	//TODO: This may not even be necessary
	@Nullable
	protected Surround findDetachedMuse(World world, BlockPos pos) {
		for (Surround s : Surround.values()) {
			final BlockState state = world.getBlockState(pos.add(s.getOffset()));

			if (state.getBlock() instanceof Musable) {
				return s;
			}
		}
		return null;
	}

	//TODO: This may not even be necessary
	@Override
	public void onBroken(WorldAccess world, BlockPos pos, BlockState state) {
		final BlockState newState = world.getBlockState(pos);

		if (newState.getBlock() != Blocks.AIR) {
			return;
		}

		Null.consumeIfNonnull(this.findDetachedMuse((World) world, pos),
				surround -> world.setBlockState(pos, getDefaultState().with(CORE_DIR, surround), 1));
	}

	@Override
	public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity playerIn, Hand hand, BlockHitResult hit) {
		return Null.applyIfNonnull(this.getMuse(world, state, pos), muse -> muse.state.getBlock().onUse(muse.state, world, muse.pos, playerIn, hand, hit), ActionResult.FAIL);
	}

	@Override
	public boolean isFlammable(BlockState state, BlockView world, BlockPos pos, Direction face) {
		return false; // This is the simple solution to the problem.  Maybe I'll work it out later.
	}

	@Override
	public int getFlammability(BlockState state, BlockView world, BlockPos pos, Direction face) {
		return 0; // This is the simple solution to the problem.  Maybe I'll work it out later.
	}

	public boolean isFullBlockShell(BlockView world, BlockPos pos) {
		return isFullBlockShell(getMuse(world, pos));
	}

	public boolean isFullBlockShell(@Nullable ShellMuse muse) {
		return muse != null && isFullBlockShell(muse.getRadius());
	}

	public boolean isFullBlockShell(int radius) {
		return (radius - 8) % 16 == 0;
	}

	@Override
	public boolean canPathfindThrough(BlockState state, BlockView world, BlockPos pos, NavigationType pathType) {
		return false;
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
			worldIn.getFluidTickScheduler().scheduleTick(new OrderedTick<>(Fluids.WATER, currentPos, Fluids.WATER.getTickRate(worldIn), 0));
		}
		return super.getStateForNeighborUpdate(stateIn, facing, facingState, worldIn, currentPos, facingPos);
	}

	@Override
	public boolean canFillWithFluid(BlockView world, BlockPos pos, BlockState state, Fluid fluid) {
		if (isFullBlockShell(world, pos)) {
			return false;
		}
		return Waterloggable.super.canFillWithFluid(world, pos, state, fluid);
	}

	protected boolean isWaterLogged(BlockState state) {
		return state.contains(WATERLOGGED) && state.get(WATERLOGGED);
	}

	@Override
	public BlockRenderType getRenderType(BlockState state) {
		return BlockRenderType.INVISIBLE;
	}

	///////////////////////////////////////////
	// RENDERING
	///////////////////////////////////////////

	@Override
	public PistonBehavior getPistonBehavior(BlockState state) {
		return PistonBehavior.BLOCK;
	}

	@Override
	public boolean addLandingEffects(BlockState state1, ServerWorld worldserver, BlockPos pos, BlockState state2, LivingEntity entity, int numberOfParticles) {
		return false;
	}

	@Override
	public void initializeClient(Consumer<IClientBlockExtensions> consumer) {
		consumer.accept(new IClientBlockExtensions() {
			@Override
			public boolean addHitEffects(BlockState state, World world, HitResult target, ParticleManager manager) {
				BlockPos shellPos;
				if (target instanceof BlockHitResult) {
					shellPos = ((BlockHitResult) target).getBlockPos();
				} else {
					return false;
				}

				if (state.getBlock() instanceof TrunkShellBlock) {
					final ShellMuse muse = ((TrunkShellBlock) state.getBlock()).getMuseUnchecked(world, state, shellPos);

					if (muse == null) {
						return true;
					}

					final BlockState museState = muse.state;
					final BlockPos musePos = muse.pos;
					final Random rand = world.random;

					int x = musePos.getX();
					int y = musePos.getY();
					int z = musePos.getZ();
					Box axisalignedbb = museState.getSidesShape(world, musePos).getBoundingBox();
					double d0 = x + rand.nextDouble() * (axisalignedbb.maxX - axisalignedbb.minX - 0.2D) + 0.1D + axisalignedbb.minX;
					double d1 = y + rand.nextDouble() * (axisalignedbb.maxY - axisalignedbb.minY - 0.2D) + 0.1D + axisalignedbb.minY;
					double d2 = z + rand.nextDouble() * (axisalignedbb.maxZ - axisalignedbb.minZ - 0.2D) + 0.1D + axisalignedbb.minZ;

					switch (((BlockHitResult) target).getSide()) {
						case DOWN:
							d1 = y + axisalignedbb.minY - 0.1D;
							break;
						case UP:
							d1 = y + axisalignedbb.maxY + 0.1D;
							break;
						case NORTH:
							d2 = z + axisalignedbb.minZ - 0.1D;
							break;
						case SOUTH:
							d2 = z + axisalignedbb.maxZ + 0.1D;
							break;
						case WEST:
							d0 = x + axisalignedbb.minX - 0.1D;
							break;
						case EAST:
							d0 = x + axisalignedbb.maxX + 0.1D;
							break;
					}

					// Safe to spawn particles here since this is a client side only member function.
					world.addParticle(new BlockStateParticleEffect(ParticleTypes.BLOCK, museState), d0, d1, d2, 0, 0, 0);
				}

				return true;
			}

			@Override
			public boolean addDestroyEffects(BlockState state, World level, BlockPos pos, ParticleManager manager) {
				if (state.getBlock() instanceof TrunkShellBlock) {
					final ShellMuse muse = ((TrunkShellBlock) state.getBlock()).getMuseUnchecked(level, state, pos);

					if (muse == null) {
						return true;
					}

					final BlockState museState = muse.state;
					final BlockPos musePos = muse.pos;

					manager.addBlockBreakParticles(musePos, museState);
				}
				return true;
			}
		});
	}

	public static class ShellMuse {
		public final BlockState state;
		public final BlockPos pos;
		public final BlockPos museOffset;
		public final Surround dir;

		public ShellMuse(BlockState state, BlockPos pos, Surround dir, BlockPos museOffset) {
			this.state = state;
			this.pos = pos;
			this.dir = dir;
			this.museOffset = museOffset;
		}

		public int getRadius() {
			final Block block = this.state.getBlock();
			return block instanceof BranchBlock ? ((BranchBlock) block).getRadius(state) : 0;
		}
	}

}
