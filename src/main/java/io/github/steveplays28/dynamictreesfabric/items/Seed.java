package io.github.steveplays28.dynamictreesfabric.items;

import java.util.List;

import javax.annotation.Nullable;

import io.github.steveplays28.dynamictreesfabric.blocks.PottedSaplingBlock;
import io.github.steveplays28.dynamictreesfabric.event.SeedVoluntaryPlantEvent;
import io.github.steveplays28.dynamictreesfabric.init.DTConfigs;
import io.github.steveplays28.dynamictreesfabric.init.DTRegistries;
import io.github.steveplays28.dynamictreesfabric.trees.Species;
import io.github.steveplays28.dynamictreesfabric.util.SafeChunkBounds;
import io.github.steveplays28.dynamictreesfabric.worldgen.BiomeDatabases;
import io.github.steveplays28.dynamictreesfabric.worldgen.JoCode;
import net.minecraftforge.common.IPlantable;
import net.minecraftforge.common.MinecraftForge;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.FlowerPotBlock;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.stat.Stats;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;

// TODO: Make compostable via ComposterBlock#registerCompostable
public class Seed extends Item implements IPlantable {

	public static final String LIFESPAN_TAG = "lifespan";
	private final Species species;//The tree this seed creates

	public Seed() {
		super(new Item.Settings());
		// TODO: Set null name? Is this still used? -SizableShrimp
		// this.setRegistryName("null");
		this.species = Species.NULL_SPECIES;
	}

	public Seed(Species species) {
		super(new Item.Settings().tab(DTRegistries.ITEM_GROUP));
		this.species = species;
	}

	public Species getSpecies() {
		return species;
	}

	@Override
	public boolean onEntityItemUpdate(ItemStack stack, ItemEntity entityItem) {
		if (entityItem.lifespan == 6000) { // 6000 (5 minutes) is the default lifespan for an entity item
			entityItem.lifespan = getTimeToLive(entityItem.getStack()) + 20; // override default lifespan with new value + 20 ticks (1 second)
			if (entityItem.lifespan == 6000) {
				entityItem.lifespan = 6001; // Ensure this isn't run again
			}
		}

		if (entityItem.age >= entityItem.lifespan - 20) {//Perform this action 20 ticks(1 second) before dying
			final World world = entityItem.world;
			if (!world.isClient) {//Server side only
				final ItemStack seedStack = entityItem.getStack();
				final BlockPos pos = new BlockPos(entityItem.getBlockPos());
				final SeedVoluntaryPlantEvent seedVolEvent = new SeedVoluntaryPlantEvent(entityItem, this.getSpecies().selfOrLocationOverride(world, pos), pos, this.shouldPlant(world, pos, seedStack));
				MinecraftForge.EVENT_BUS.post(seedVolEvent);
				if (!seedVolEvent.isCanceled() && seedVolEvent.getWillPlant()) {
					this.doPlanting(world, pos, null, seedStack);
				}
				seedStack.setCount(0);
			}
			entityItem.kill();
		}

		return false;
	}

	public boolean doPlanting(World world, BlockPos pos, @Nullable PlayerEntity planter, ItemStack seedStack) {
		final Species species = this.getSpecies().selfOrLocationOverride(world, pos);
		if (species.plantSapling(world, pos, this.getSpecies() != species)) { // Do the planting
			String joCode = getCode(seedStack);
			if (!joCode.isEmpty()) {
				world.removeBlock(pos, false); // Remove the newly created dynamic sapling
				species.getJoCode(joCode).setCareful(true).generate(world, world, species, pos.down(), world.getBiome(pos), planter != null ? planter.getHorizontalFacing() : Direction.NORTH, 8, SafeChunkBounds.ANY, false);
			}
			return true;
		}
		return false;
	}

	public boolean shouldPlant(World world, BlockPos pos, ItemStack seedStack) {

		if (hasForcePlant(seedStack)) {
			return true;
		}

		if (!world.isSkyVisibleAllowingSea(pos)) {
			return false;
		}

		float plantChance = (float) (getSpecies().biomeSuitability(world, pos) * DTConfigs.SEED_PLANT_RATE.get());

		if (DTConfigs.SEED_ONLY_FOREST.get()) {
			plantChance *= BiomeDatabases.getDimensionalOrDefault(world.getRegistryKey().getValue())
					.getForestness(world.getBiome(pos));
		}

		float accum = 1.0f;
		int count = seedStack.getCount();
		while (count-- > 0) {
			accum *= 1.0f - plantChance;
		}
		plantChance = 1.0f - accum;

		return plantChance > world.random.nextFloat();
	}

	public boolean hasForcePlant(ItemStack seedStack) {
		boolean forcePlant = false;
		if (seedStack.hasNbt()) {
			NbtCompound nbtData = seedStack.getNbt();
			assert nbtData != null;
			forcePlant = nbtData.getBoolean("forceplant");
		}
		return forcePlant;
	}

	public int getTimeToLive(ItemStack seedStack) {
		int lifespan = DTConfigs.SEED_TIME_TO_LIVE.get();//1 minute by default(helps with lag)
		if (seedStack.hasNbt()) {
			NbtCompound nbtData = seedStack.getNbt();
			assert nbtData != null;
			if (nbtData.contains("lifespan")) {
				lifespan = nbtData.getInt("lifespan");
			}
		}
		return lifespan;
	}

	public String getCode(ItemStack seedStack) {
		String joCode = "";
		if (seedStack.hasNbt()) {
			NbtCompound nbtData = seedStack.getNbt();
			assert nbtData != null;
			joCode = nbtData.getString("code");
		}
		return joCode;
	}

	public ActionResult onItemUseFlowerPot(ItemUsageContext context) {
		final World world = context.getWorld();
		final BlockPos pos = context.getBlockPos();
		final BlockState emptyPotState = world.getBlockState(pos);
		final Block emptyPotBlock = emptyPotState.getBlock();

		if (!(emptyPotBlock instanceof FlowerPotBlock) || emptyPotState != emptyPotBlock.getDefaultState() ||
				((FlowerPotBlock) emptyPotBlock).getContent() != Blocks.AIR) {
			return ActionResult.PASS;
		}

		final PottedSaplingBlock pottingSapling = this.getSpecies().getPottedSapling();
		world.setBlockState(pos, pottingSapling.getDefaultState());

		if (pottingSapling.setSpecies(world, pos, pottingSapling.getDefaultState(), this.getSpecies()) && pottingSapling.setPotState(world, emptyPotState, pos)) {
			final PlayerEntity player = context.getPlayer();

			if (player != null) {
				context.getPlayer().incrementStat(Stats.POT_FLOWER);
				if (!context.getPlayer().getAbilities().creativeMode) {
					context.getStack().decrement(1);
				}
			}

			return ActionResult.SUCCESS;
		}

		return ActionResult.PASS;
	}

	public ActionResult onItemUsePlantSeed(ItemUsageContext context) {

		BlockState state = context.getWorld().getBlockState(context.getBlockPos());
		BlockPos pos = context.getBlockPos();
		Direction facing = context.getSide();
		if (state.getMaterial().isReplaceable()) {
			pos = pos.down();
			facing = Direction.UP;
		}

		if (facing == Direction.UP) {//Ensure this seed is only used on the top side of a block
			if (context.getPlayer() != null && context.getPlayer().canPlaceOn(pos, facing, context.getStack()) && context.getPlayer().canPlaceOn(pos.up(), facing, context.getStack())) {//Ensure permissions to edit block
				if (doPlanting(context.getWorld(), pos.up(), context.getPlayer(), context.getStack())) {
					context.getStack().decrement(1);
					return ActionResult.SUCCESS;
				}
			}
		}

		return ActionResult.PASS;
	}

	@Override
	public ActionResult onItemUseFirst(ItemStack stack, ItemUsageContext context) {
		// Handle flower pot interaction (flower pot cancels on item use so this must be done first).
		if (onItemUseFlowerPot(context) == ActionResult.SUCCESS) {
			return ActionResult.SUCCESS;
		}

		return ActionResult.PASS;
	}

	@Override
	public ActionResult useOnBlock(ItemUsageContext context) {
		// Handle planting seed interaction.
		if (onItemUsePlantSeed(context) == ActionResult.SUCCESS) {
			return ActionResult.SUCCESS;
		}

		return ActionResult.PASS;
	}

	@Override
	public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext flagIn) {
		super.appendTooltip(stack, world, tooltip, flagIn);

		if (stack.hasNbt()) {
			final String joCode = this.getCode(stack);
			if (!joCode.isEmpty()) {
				tooltip.add(Text.translatable("tooltip.dynamictrees.jo_code", new JoCode(joCode).getTextComponent()));
			}
			if (this.hasForcePlant(stack)) {
				tooltip.add(Text.translatable("tooltip.dynamictrees.force_planting",
						Text.translatable("tooltip.dynamictrees.enabled")
								.styled(style -> style.withColor(Formatting.DARK_AQUA)))
				);
			}
			final NbtCompound nbtData = stack.getNbt();
			assert nbtData != null;

			if (nbtData.contains(LIFESPAN_TAG)) {
				tooltip.add(Text.translatable("tooltip.dynamictrees.seed_life_span" +
						Text.literal(String.valueOf(nbtData.getInt(LIFESPAN_TAG)))
								.styled(style -> style.withColor(Formatting.DARK_AQUA)))
				);
			}
		}
	}


	///////////////////////////////////////////
	//IPlantable Interface
	///////////////////////////////////////////

	@Override
	public BlockState getPlant(BlockView world, BlockPos pos) {
		return getSpecies().getSapling().map(Block::getDefaultState).orElse(Blocks.AIR.getDefaultState());
	}

}
