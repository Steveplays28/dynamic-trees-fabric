package io.github.steveplays28.dynamictreesfabric.blocks;

import static io.github.steveplays28.dynamictreesfabric.util.ShapeUtils.createFruitShape;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

import io.github.steveplays28.dynamictreesfabric.trees.Species;
import io.github.steveplays28.dynamictreesfabric.util.BlockStates;
import net.minecraftforge.common.ForgeHooks;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.Fertilizable;
import net.minecraft.block.LeavesBlock;
import net.minecraft.block.Material;
import net.minecraft.block.ShapeContext;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.context.LootContext;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.IntProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.WorldView;

@SuppressWarnings({"deprecation", "unused"})
public class FruitBlock extends Block implements Fertilizable {
	public static final IntProperty AGE = Properties.AGE_3;
	private static final Map<Species, Set<FruitBlock>> SPECIES_FRUIT_MAP = new HashMap<>();
	/**
	 * Default shapes for the apple fruit, each element is the shape for each growth stage.
	 */
	protected Box[] FRUIT_AABB = new Box[]{
			createFruitShape(1, 1, 0, 16),
			createFruitShape(1, 2, 0, 16),
			createFruitShape(2.5f, 5, 0),
			createFruitShape(2.5f, 5, 1.25f)
	};
	protected ItemStack droppedFruit = ItemStack.EMPTY;
	protected Supplier<Boolean> canBoneMeal = () -> false; // Q: Does dusting an apple with bone dust make it grow faster? A: Not by default.
	protected Vec3d itemSpawnOffset = new Vec3d(0.5, 0.6, 0.5);
	private Species species;

	public FruitBlock() {
		super(Settings.of(Material.PLANT)
				.ticksRandomly()
				.strength(0.3f));
	}

	@NotNull
	public static Set<FruitBlock> getFruitBlocksForSpecies(Species species) {
		return SPECIES_FRUIT_MAP.getOrDefault(species, new HashSet<>());
	}

	public FruitBlock setCanBoneMeal(boolean canBoneMeal) {
		return this.setCanBoneMeal(() -> canBoneMeal);
	}

	public FruitBlock setCanBoneMeal(Supplier<Boolean> canBoneMeal) {
		this.canBoneMeal = canBoneMeal;
		return this;
	}

	public void setItemSpawnOffset(float x, float y, float z) {
		this.itemSpawnOffset = new Vec3d(Math.min(Math.max(x, 0), 1), Math.min(Math.max(y, 0), 1), Math.min(Math.max(z, 0), 1));
	}

	public Species getSpecies() {
		return this.species == null ? Species.NULL_SPECIES : this.species;
	}

	public void setSpecies(Species species) {
		if (SPECIES_FRUIT_MAP.containsKey(species)) {
			SPECIES_FRUIT_MAP.get(species).add(this);
		} else {
			Set<FruitBlock> set = new HashSet<>();
			set.add(this);
			SPECIES_FRUIT_MAP.put(species, set);
		}
		this.species = species;
	}

	public float getMinimumSeasonalValue() {
		return 0.3f;
	}

	@Override
	public void scheduledTick(BlockState state, ServerWorld world, BlockPos pos, Random rand) {
		this.doTick(state, world, pos, rand);
	}

	public void doTick(BlockState state, World world, BlockPos pos, Random rand) {
		if (this.shouldBlockDrop(world, pos, state)) {
			this.dropBlock(world, state, pos);
			return;
		}

		final int age = state.get(AGE);
		// TODO: FABRIC PORT: Fabric Seasons compat should be re-added here
//		final Float season = SeasonHelper.getSeasonValue(world, pos);
		final Float season = null;
		final Species species = this.getSpecies();

		if (season != null && species.isValid()) { // Non-Null means we are season capable.
			if (species.seasonalFruitProductionFactor(world, pos) < getMinimumSeasonalValue()) {
				this.outOfSeasonAction(world, pos); // Destroy the block or similar action.
				return;
			}
		}

		// TODO: FABRIC PORT: Figure out how to replace crop grow Forge hooks
		if (age < 3) {
			final boolean doGrow = rand.nextFloat() < this.getGrowthChance(world, pos);
			final boolean eventGrow = ForgeHooks.onCropsGrowPre(world, pos, state, doGrow);
			if (season != null ? doGrow || eventGrow : eventGrow) { // Prevent a seasons mod from canceling the growth, we handle that ourselves.
				world.setBlockState(pos, state.with(AGE, age + 1), 2);
				ForgeHooks.onCropsGrowPost(world, pos, state);
			}
		} else {
			if (age == 3) {
				switch (this.matureAction(world, pos, state, rand)) {
					case NOTHING:
					case CUSTOM:
						break;
					case DROP:
						this.dropBlock(world, state, pos);
						break;
					case ROT:
						world.setBlockState(pos, BlockStates.AIR);
						break;
				}
			}
		}
	}

	protected float getGrowthChance(World world, BlockPos blockPos) {
		return 0.2f;
	}

	/**
	 * Override this to make the fruit do something once it's mature.
	 *
	 * @param world The world
	 * @param pos   The position of the fruit block
	 * @param state The current blockstate of the fruit
	 * @param rand  A random number generator
	 * @return MatureFruitAction action to take
	 */
	protected MatureFruitAction matureAction(World world, BlockPos pos, BlockState state, Random rand) {
		return MatureFruitAction.NOTHING;
	}

	protected void outOfSeasonAction(World world, BlockPos pos) {
		world.setBlockState(pos, BlockStates.AIR);
	}

	@Override
	public void neighborUpdate(BlockState state, World world, BlockPos pos, Block block, BlockPos neighbor, boolean isMoving) {
		this.onNeighborChange(state, world, pos, neighbor);
	}

	public void onNeighborChange(BlockState state, WorldView world, BlockPos pos, BlockPos neighbor) {
		if (this.shouldBlockDrop(world, pos, state)) {
			this.dropBlock((World) world, state, pos);
		}
	}

	@Override
	public ActionResult onUse(BlockState state, World worldIn, BlockPos pos, PlayerEntity player, Hand handIn, BlockHitResult hit) {
		if (state.get(AGE) >= 3) {
			this.dropBlock(worldIn, state, pos);
			return ActionResult.SUCCESS;
		}

		return ActionResult.FAIL;
	}

	protected void dropBlock(World worldIn, BlockState state, BlockPos pos) {
		worldIn.setBlockState(pos, Blocks.AIR.getDefaultState(), 3);
		if (state.get(AGE) >= 3) {
			worldIn.spawnEntity(new ItemEntity(worldIn, pos.getX() + itemSpawnOffset.x, pos.getY() + itemSpawnOffset.y, pos.getZ() + itemSpawnOffset.z, this.getFruitDrop(fruitDropCount(state, worldIn, pos))));
		}
	}

	// TODO: FABRIC PORT: More ItemStack stuff that needs testing
//	@Override
//	public ItemStack getCloneItemStack(BlockState state, HitResult target, BlockView world, BlockPos pos, PlayerEntity player) {
//		return this.getFruitDrop(1);
//	}

	/**
	 * Checks if Leaves of any kind are above this block. Not picky.
	 *
	 * @param world
	 * @param pos
	 * @param state
	 * @return True if it should drop (leaves are not above).
	 */
	public boolean shouldBlockDrop(BlockView world, BlockPos pos, BlockState state) {
		return !(world.getBlockState(pos.up()).getBlock() instanceof LeavesBlock);
	}

	@Override
	public boolean isFertilizable(WorldView world, BlockPos pos, BlockState state, boolean isClient) {
		return state.get(AGE) < 3;
	}

	///////////////////////////////////////////
	// BONEMEAL
	///////////////////////////////////////////

	@Override
	public boolean canGrow(World world, Random rand, BlockPos pos, BlockState state) {
		return this.canBoneMeal.get();
	}

	@Override
	public void grow(ServerWorld world, Random rand, BlockPos pos, BlockState state) {
		final int age = state.get(AGE);
		final int newAge = MathHelper.clamp(age + 1, 0, 3);
		if (newAge != age) {
			world.setBlockState(pos, state.with(AGE, newAge), 2);
		}
	}

	@Override
	public List<ItemStack> getDroppedStacks(BlockState state, LootContext.Builder builder) {
		// If a loot table has been added load those drops instead (until drop creators).
		if (builder.getWorld().getServer().getLootManager().getTableIds().contains(this.getLootTableId())) {
			return super.getDroppedStacks(state, builder);
		}

		final List<ItemStack> drops = new ArrayList<>();

		if (state.get(AGE) >= 3) {
			final ItemStack toDrop = this.getFruitDrop(fruitDropCount(state, builder.getWorld(), BlockPos.ORIGIN));
			if (!toDrop.isEmpty()) {
				drops.add(toDrop);
			}
		}

		return drops;
	}

	///////////////////////////////////////////
	//DROPS
	///////////////////////////////////////////

	public FruitBlock setDroppedItem(ItemStack stack) {
		this.droppedFruit = stack;
		return this;
	}

	//Override this for a custom item drop
	public ItemStack getFruitDrop(int count) {
		ItemStack stack = droppedFruit.copy();
		stack.setCount(count);
		return stack;
	}

	//pos could be BlockPos.ZERO
	protected int fruitDropCount(BlockState state, World world, BlockPos pos) {
		return 1;
	}

	public FruitBlock setShape(int stage, Box boundingBox) {
		FRUIT_AABB[stage] = boundingBox;
		return this;
	}

	///////////////////////////////////////////
	// BOUNDARIES
	///////////////////////////////////////////

	public FruitBlock setShape(Box[] boundingBox) {
		FRUIT_AABB = boundingBox;
		return this;
	}

	@Override
	public VoxelShape getOutlineShape(BlockState state, BlockView worldIn, BlockPos pos, ShapeContext context) {
		return VoxelShapes.cuboid(FRUIT_AABB[state.get(AGE)]);
	}

	@Override
	protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
		builder.add(AGE);
	}

	///////////////////////////////////////////
	// BLOCKSTATE
	///////////////////////////////////////////

	public BlockState getStateForAge(int age) {
		return this.getDefaultState().with(AGE, age);
	}

	public int getAgeForSeasonalWorldGen(WorldAccess world, BlockPos pos, @Nullable Float seasonValue) {
		if (seasonValue == null) {
			return 3;
		}

		if (this.getSpecies().testFlowerSeasonHold(seasonValue)) {
			return 0; // Fruit is as the flower stage.
		}

		return Math.min(world.getRandom().nextInt(6), 3); // Half the time the fruit is fully mature.
	}

	enum MatureFruitAction {
		NOTHING,
		DROP,
		ROT,
		CUSTOM
	}
}
