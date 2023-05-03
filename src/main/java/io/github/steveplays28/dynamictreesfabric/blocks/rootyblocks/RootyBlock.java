package io.github.steveplays28.dynamictreesfabric.blocks.rootyblocks;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.annotation.Nonnull;
import org.jetbrains.annotations.Nullable;

import io.github.steveplays28.dynamictreesfabric.api.RootyBlockDecayer;
import io.github.steveplays28.dynamictreesfabric.api.TreeHelper;
import io.github.steveplays28.dynamictreesfabric.api.cells.Cell;
import io.github.steveplays28.dynamictreesfabric.api.cells.CellNull;
import io.github.steveplays28.dynamictreesfabric.api.network.MapSignal;
import io.github.steveplays28.dynamictreesfabric.api.treedata.TreePart;
import io.github.steveplays28.dynamictreesfabric.blocks.BlockWithDynamicHardness;
import io.github.steveplays28.dynamictreesfabric.blocks.branches.BranchBlock;
import io.github.steveplays28.dynamictreesfabric.blocks.leaves.LeavesProperties;
import io.github.steveplays28.dynamictreesfabric.entities.FallingTreeEntity;
import io.github.steveplays28.dynamictreesfabric.init.DTConfigs;
import io.github.steveplays28.dynamictreesfabric.systems.GrowSignal;
import io.github.steveplays28.dynamictreesfabric.tileentity.SpeciesTileEntity;
import io.github.steveplays28.dynamictreesfabric.trees.Family;
import io.github.steveplays28.dynamictreesfabric.trees.Species;
import io.github.steveplays28.dynamictreesfabric.util.BranchDestructionData;
import io.github.steveplays28.dynamictreesfabric.util.CoordUtils;

import net.minecraft.block.Block;
import net.minecraft.block.BlockEntityProvider;
import net.minecraft.block.BlockState;
import net.minecraft.block.MapColor;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.piston.PistonBehavior;
import net.minecraft.client.color.block.BlockColors;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.context.LootContext;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.IntProperty;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockRenderView;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.WorldView;
import net.minecraft.world.explosion.Explosion;

/**
 * A version of Rooty Dirt block that holds on to a species with a TileEntity.
 * <p>
 * When to use this: You can't determine a species of a tree family by location alone (e.g. Swamp Oak by biome) The
 * species is rare and you don't want to commit all the resources necessary to make a whole tree family(e.g. Apple Oak)
 * <p>
 * This is a great method for creating numerous fruit species(Pam's Harvestcraft) under one {@link Family} family.
 *
 * @author ferreusveritas
 */
@SuppressWarnings("deprecation")
public class RootyBlock extends BlockWithDynamicHardness implements TreePart, BlockEntityProvider {

	public static final IntProperty FERTILITY = IntProperty.of("fertility", 0, 15);
	public static final BooleanProperty IS_VARIANT = BooleanProperty.of("is_variant");
	public static RootyBlockDecayer rootyBlockDecayer = null;
	private final SoilProperties properties;
	//private ConfiguredSoilProperties<SoilProperties> configuredProperties = ConfiguredSoilProperties.NULL_CONFIGURED_SOIL_PROPERTIES;

	public RootyBlock(SoilProperties properties, Settings blockProperties) {
		super(blockProperties.ticksRandomly());
		this.properties = properties;
		setDefaultState(getDefaultState().with(FERTILITY, 0).with(IS_VARIANT, false));
	}

	///////////////////////////////////////////
	// SOIL PROPERTIES
	///////////////////////////////////////////

	public SoilProperties getSoilProperties() {
		return properties;
	}

	public Block getPrimitiveSoilBlock() {
		return properties.getPrimitiveSoilBlock();
	}

	public BlockState getPrimitiveSoilState(BlockState currentSoilState) {
		return properties.getPrimitiveSoilState(currentSoilState);
	}

	///////////////////////////////////////////
	// BLOCK PROPERTIES
	///////////////////////////////////////////

	@Override
	public BlockSoundGroup getSoundType(BlockState state, WorldView world, BlockPos pos, @Nullable Entity entity) {
		return getPrimitiveSoilBlock().getSoundGroup(getDecayBlockState(state, world, pos), world, pos, entity);
	}

	@Override
	public int getLightEmission(BlockState state, BlockView world, BlockPos pos) {
		return getPrimitiveSoilBlock().getLightEmission(getDecayBlockState(state, world, pos), world, pos);
	}

	@Override
	public boolean isTransparent(BlockState state, BlockView world, BlockPos pos) {
		return getPrimitiveSoilBlock().isTransparent(getDecayBlockState(state, world, pos), world, pos);
	}

	@Override
	public int getOpacity(BlockState state, BlockView world, BlockPos pos) {
		return getPrimitiveSoilBlock().getOpacity(getDecayBlockState(state, world, pos), world, pos);
	}

//    @Nullable
//    @Override
//    public ToolType getHarvestTool(BlockState state) {
//        return getPrimitiveSoilBlock().getHarvestTool(getPrimitiveSoilState(state));
//    }
//
//    @Override
//    public int getHarvestLevel(BlockState state) {
//        return getPrimitiveSoilBlock().getHarvestLevel(getPrimitiveSoilState(state));
//    }

	@Override
	public MapColor getDefaultMapColor() {
		return getPrimitiveSoilBlock().getDefaultMapColor();
	}

	@Override
	public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
		return getPrimitiveSoilBlock().getOutlineShape(getDecayBlockState(state, world, pos), world, pos, context);
	}

	@Override
	public float getSlipperiness() {
		return getPrimitiveSoilBlock().getSlipperiness();
	}

	@Override
	public float getExplosionResistance(BlockState state, BlockView world, BlockPos pos, Explosion explosion) {
		return getPrimitiveSoilBlock().getBlastResistance(getDecayBlockState(state, world, pos), world, pos, explosion);
	}

	@Override
	public float getVelocityMultiplier() {
		return getPrimitiveSoilBlock().getVelocityMultiplier();
	}

	@Override
	public float getJumpVelocityMultiplier() {
		return getPrimitiveSoilBlock().getJumpVelocityMultiplier();
	}

	@Override
	public int getFireSpreadSpeed(BlockState state, BlockView world, BlockPos pos, Direction face) {
		return getPrimitiveSoilBlock().getFireSpreadSpeed(getDecayBlockState(state, world, pos), world, pos, face);
	}

	@Override
	public boolean isFireSource(BlockState state, WorldView world, BlockPos pos, Direction side) {
		return getPrimitiveSoilBlock().isFireSource(getDecayBlockState(state, world, pos), world, pos, side);
	}

	@Nonnull
	@Override
	public List<ItemStack> getDroppedStacks(@Nonnull BlockState state, @Nonnull LootContext.Builder builder) {
		return getPrimitiveSoilState(state).getDroppedStacks(builder);
	}

	@Override
	public ItemStack getCloneItemStack(BlockState state, HitResult target, BlockView world, BlockPos pos, PlayerEntity player) {
		return getPrimitiveSoilBlock().getPickStack(getDecayBlockState(state, world, pos), target, world, pos, player);
	}

	@Override
	public float getHardness(BlockState state, BlockView worldIn, BlockPos pos) {
		return (float) (getDecayBlockState(state, worldIn, pos).getHardness(worldIn, pos) * DTConfigs.ROOTY_BLOCK_HARDNESS_MULTIPLIER.get());
	}

	///////////////////////////////////////////
	// BLOCKSTATES
	///////////////////////////////////////////

	@Override
	protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
		builder.add(FERTILITY).add(IS_VARIANT);
	}

	///////////////////////////////////////////
	// INTERACTION
	///////////////////////////////////////////


	@org.jetbrains.annotations.Nullable
	@Override
	public BlockEntity createBlockEntity(BlockPos pPos, BlockState pState) {
//        if (pState.getValue(IS_VARIANT)) {
		return new SpeciesTileEntity(pPos, pState);
//        }
//        return null;
	}
//
//    @Override
//    public boolean hasTileEntity(BlockState state) {
//        return state.getValue(IS_VARIANT);
//    }

	@Override
	public void randomTick(BlockState state, ServerWorld worldIn, BlockPos pos, Random random) {
		if (random.nextInt(DTConfigs.TREE_GROWTH_FOLDING.get()) == 0) {
			updateTree(state, worldIn, pos, random, true);
		}
	}

	public Direction getTrunkDirection(BlockView access, BlockPos rootPos) {
		return Direction.UP;
	}

	public void updateTree(BlockState rootyState, World world, BlockPos rootPos, Random random, boolean natural) {

		if (CoordUtils.isSurroundedByLoadedChunks(world, rootPos)) {

			boolean viable = false;

			Species species = getSpecies(rootyState, world, rootPos);

			if (species.isValid()) {
				BlockPos treePos = rootPos.offset(getTrunkDirection(world, rootPos));
				TreePart treeBase = TreeHelper.getTreePart(world.getBlockState(treePos));
				if (treeBase != TreeHelper.NULL_TREE_PART) {
					viable = species.update(world, this, rootPos, getFertility(rootyState, world, rootPos), treeBase, treePos, random, natural);
				}
			}

			if (!viable) {
				//TODO: Attempt to destroy what's left of the tree before setting rooty to dirt
				world.setBlockState(rootPos, getDecayBlockState(rootyState, world, rootPos), 3);
			}

		}

	}

	/**
	 * This is the state the rooty dirt returns to once it no longer supports a tree structure.
	 *
	 * @param world
	 * @param pos   The position of the {@link RootyBlock}
	 * @return
	 */
	public BlockState getDecayBlockState(BlockState state, BlockView world, BlockPos pos) {
		return getPrimitiveSoilState(state);
	}

	/**
	 * Forces the {@link RootyBlock} to decay if it's there, turning it back to its primitive soil block. Custom decay
	 * logic is also supported, see {@link RootyBlockDecayer} for details.
	 *
	 * @param world      The {@link World} instance.
	 * @param rootPos    The {@link BlockPos} of the {@link RootyBlock}.
	 * @param rootyState The {@link BlockState} of the {@link RootyBlock}.
	 * @param species    The {@link Species} of the tree that was removed.
	 */
	public void doDecay(World world, BlockPos rootPos, BlockState rootyState, Species species) {
		if (world.isClient || !TreeHelper.isRooty(rootyState)) {
			return;
		}

		this.updateTree(rootyState, world, rootPos, world.random, true); // This will turn the rooty dirt back to it's default soil block.
		final BlockState newState = world.getBlockState(rootPos);

		// Make sure we're not still a rooty block and return if custom decay returns true.
		if (TreeHelper.isRooty(newState) || (rootyBlockDecayer != null && rootyBlockDecayer.decay(world, rootPos, rootyState, species))) {
			return;
		}

		final BlockState primitiveDirt = this.getDecayBlockState(rootyState, world, rootPos);

		world.setBlockState(rootPos, primitiveDirt, Block.NOTIFY_ALL);
	}

	@Override
	public boolean hasComparatorOutput(BlockState state) {
		return true;
	}

	@Override
	public int getComparatorOutput(BlockState blockState, World world, BlockPos pos) {
		return getFertility(blockState, world, pos);
	}

	@Override
	public ActionResult onUse(BlockState state, World worldIn, BlockPos pos, PlayerEntity player, Hand handIn, BlockHitResult hit) {
		return getFamily(state, worldIn, pos).onTreeActivated(worldIn, pos, state, player, handIn, player.getStackInHand(handIn), hit) ? ActionResult.SUCCESS : ActionResult.FAIL;
	}

	public void destroyTree(World world, BlockPos rootPos) {
		Optional<BranchBlock> branch = TreeHelper.getBranchOpt(world.getBlockState(rootPos.up()));

		if (branch.isPresent()) {
			BranchDestructionData destroyData = branch.get().destroyBranchFromNode(world, rootPos.up(), Direction.DOWN, true, null);
			FallingTreeEntity.dropTree(world, destroyData, new ArrayList<>(0), FallingTreeEntity.DestroyType.ROOT);
		}
	}

	@Override
	public void onBreak(World world, @Nonnull BlockPos pos, BlockState state, @Nonnull PlayerEntity player) {
		this.destroyTree(world, pos);
		super.onBreak(world, pos, state, player);
	}

	@Override
	public void onBlockExploded(BlockState state, World world, BlockPos pos, Explosion explosion) {
		destroyTree(world, pos);
		super.onBlockExploded(state, world, pos, explosion);
	}


	@Nonnull
	@Override
	public PistonBehavior getPistonBehavior(BlockState state) {
		return PistonBehavior.BLOCK;
	}

	///////////////////////////////////////////
	// TREE STUFF
	///////////////////////////////////////////

	public int getFertility(BlockState blockState, BlockView blockAccess, BlockPos pos) {
		return blockState.get(FERTILITY);
	}

	public void setFertility(World world, BlockPos rootPos, int fertility) {
		final BlockState currentState = world.getBlockState(rootPos);
		final Species species = this.getSpecies(currentState, world, rootPos);

		world.setBlockState(rootPos, currentState.with(FERTILITY, MathHelper.clamp(fertility, 0, 15)), 3);
		world.updateNeighborsAlways(rootPos, this); // Notify all neighbors of NSEWUD neighbors (for comparator).
		this.setSpecies(world, rootPos, species);
	}

	public boolean fertilize(World world, BlockPos pos, int amount) {
		int fertility = this.getFertility(world.getBlockState(pos), world, pos);
		if ((fertility == 0 && amount < 0) || (fertility == 15 && amount > 0)) {
			return false;//Already maxed out
		}
		setFertility(world, pos, fertility + amount);
		return true;
	}

	@Override
	public Cell getHydrationCell(BlockView reader, BlockPos pos, BlockState state, Direction dir, LeavesProperties leavesTree) {
		return CellNull.NULL_CELL;
	}

	@Override
	public GrowSignal growSignal(World world, BlockPos pos, GrowSignal signal) {
		return signal;
	}

	@Override
	public int getRadius(BlockState state) {
		return 8;
	}

	@Override
	public int getRadiusForConnection(BlockState state, BlockView reader, BlockPos pos, BranchBlock from, Direction side, int fromRadius) {
		return 8;
	}

	@Override
	public int probabilityForBlock(BlockState state, BlockView reader, BlockPos pos, BranchBlock from) {
		return 0;
	}

	/**
	 * Analysis typically begins with the root node.  This function allows the rootyBlock to direct the analysis in the
	 * direction of the tree since trees are not always "up" from the rootyBlock
	 *
	 * @param world
	 * @param rootPos
	 * @param signal
	 * @return
	 */
	public MapSignal startAnalysis(WorldAccess world, BlockPos rootPos, MapSignal signal) {
		Direction dir = getTrunkDirection(world, rootPos);
		BlockPos treePos = rootPos.offset(dir);
		BlockState treeState = world.getBlockState(treePos);

		TreeHelper.getTreePart(treeState).analyse(treeState, world, treePos, null, signal);

		return signal;
	}

	@Override
	public boolean shouldAnalyse(BlockState state, BlockView reader, BlockPos pos) {
		return true;
	}

	@Override
	public MapSignal analyse(BlockState state, WorldAccess world, BlockPos pos, @Nullable Direction fromDir, MapSignal signal) {
		signal.run(state, world, pos, fromDir);//Run inspector of choice

		if (signal.root == null) {
			signal.root = pos;
		} else {
			signal.multiroot = true;
		}

		signal.foundRoot = true;

		return signal;
	}

	@Override
	public int branchSupport(BlockState state, BlockView reader, BranchBlock branch, BlockPos pos, Direction dir, int radius) {
		return dir == Direction.DOWN ? BranchBlock.setSupport(1, 1) : 0;
	}

	@Override
	public Family getFamily(BlockState state, BlockView reader, BlockPos rootPos) {
		BlockPos treePos = rootPos.offset(getTrunkDirection(reader, rootPos));
		BlockState treeState = reader.getBlockState(treePos);
		return TreeHelper.isBranch(treeState) ? TreeHelper.getBranch(treeState).getFamily(treeState, reader, treePos) : Family.NULL_FAMILY;
	}

	@Nullable
	private SpeciesTileEntity getTileEntitySpecies(WorldAccess world, BlockPos pos) {
		final BlockEntity blockEntity = world.getBlockEntity(pos);
		if (blockEntity == null)
			return null;
		return blockEntity instanceof SpeciesTileEntity ? (SpeciesTileEntity) blockEntity : null;
	}

	/**
	 * Rooty Dirt can report whatever {@link Family} species it wants to be. We'll use a stored value to determine the
	 * species for the {@link BlockEntity} version. Otherwise we'll just make it report whatever {@link io.github.steveplays28.dynamictreesfabric.DynamicTreesFabric} the
	 * above {@link BranchBlock} says it is.
	 */
	public Species getSpecies(BlockState state, WorldAccess world, BlockPos rootPos) {

		Family tree = getFamily(state, world, rootPos);

		SpeciesTileEntity rootyDirtTE = getTileEntitySpecies(world, rootPos);

		if (rootyDirtTE != null) {
			Species species = rootyDirtTE.getSpecies();
			if (species.getFamily() == tree) {//As a sanity check we should see if the tree and the stored species are a match
				return rootyDirtTE.getSpecies();
			}
		}

		return tree.getSpeciesForLocation(world, rootPos.offset(getTrunkDirection(world, rootPos)));
	}

	public void setSpecies(World world, BlockPos rootPos, Species species) {
		SpeciesTileEntity rootyDirtTE = getTileEntitySpecies(world, rootPos);
		if (rootyDirtTE != null) {
			rootyDirtTE.setSpecies(species);
		}
	}

	public final TreePartType getTreePartType() {
		return TreePartType.ROOT;
	}

	@Override
	public final boolean isRootNode() {
		return true;
	}

	///////////////////////////////////////////
	// RENDERING
	///////////////////////////////////////////

	public int colorMultiplier(BlockColors blockColors, BlockState state, @Nullable BlockRenderView world, @Nullable BlockPos pos, int tintIndex) {
		final int white = 0xFFFFFFFF;
		switch (tintIndex) {
			case 0:
				return blockColors.getColor(getPrimitiveSoilState(state), world, pos, tintIndex);
			case 1:
				return state.getBlock() instanceof RootyBlock ? rootColor(state, world, pos) : white;
			default:
				return white;
		}
	}

	public boolean getColorFromBark() {
		return false;
	}


@Environment(EnvType.CLIENT)
	public int rootColor(BlockState state, BlockView blockAccess, BlockPos pos) {
		return getFamily(state, blockAccess, pos).getRootColor(state, getColorFromBark());
	}

	public boolean fallWithTree(BlockState state, World world, BlockPos pos) {
		return false;
	}

}
