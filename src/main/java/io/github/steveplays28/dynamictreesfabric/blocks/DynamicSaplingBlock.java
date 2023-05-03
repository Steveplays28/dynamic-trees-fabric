package io.github.steveplays28.dynamictreesfabric.blocks;

import io.github.steveplays28.dynamictreesfabric.api.TreeHelper;
import io.github.steveplays28.dynamictreesfabric.init.DTConfigs;
import io.github.steveplays28.dynamictreesfabric.trees.Species;
import io.github.steveplays28.dynamictreesfabric.util.CoordUtils;

import net.minecraft.block.*;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.context.LootContext;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldView;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

@SuppressWarnings("deprecation")
public class DynamicSaplingBlock extends PlantBlock implements Fertilizable {

	protected Species species;

	public DynamicSaplingBlock(Species species) {
		super(Settings.of(Material.PLANT).sounds(BlockSoundGroup.GRASS).ticksRandomly().nonOpaque());
		this.species = species;
	}


	///////////////////////////////////////////
	// TREE INFORMATION
	///////////////////////////////////////////

	public static boolean canSaplingStay(WorldView world, Species species, BlockPos pos) {
		//Ensure there are no adjacent branches or other saplings
		for (Direction dir : CoordUtils.HORIZONTALS) {
			BlockState blockState = world.getBlockState(pos.offset(dir));
			Block block = blockState.getBlock();
			if (TreeHelper.isBranch(block) || block instanceof DynamicSaplingBlock) {
				return false;
			}
		}

		//Air above and acceptable soil below
		return world.isAir(pos.up()) && species.isAcceptableSoil(world, pos.down(), world.getBlockState(pos.down()));
	}

	public Species getSpecies() {
		return species;
	}

	@Override
	public boolean isFertilizable(WorldView world, BlockPos pos, BlockState state, boolean isClient) {
		return this.getSpecies().canSaplingConsumeBoneMeal((World) world, pos);
	}

	///////////////////////////////////////////
	// INTERACTION
	///////////////////////////////////////////

	@Override
	public boolean canGrow(@NotNull World world, @NotNull Random rand, @NotNull BlockPos pos, @NotNull BlockState state) {
		return this.getSpecies().canSaplingGrowAfterBoneMeal(world, rand, pos);
	}

	// TODO: FABRIC PORT: Replace using net.fabricmc.fabric.api.registry.FlammableBlockRegistry
	@Override
	public int getFireSpreadSpeed(BlockState state, BlockView world, BlockPos pos, Direction face) {
		return this.getSpecies().saplingFireSpread();
	}

	@Override
	public int getFlammability(BlockState state, BlockView world, BlockPos pos, Direction face) {
		return this.getSpecies().saplingFlammability();
	}

	@Override
	public void scheduledTick(BlockState state, ServerWorld worldIn, BlockPos pos, Random rand) {
		if (this.getSpecies().canSaplingGrowNaturally(worldIn, pos)) {
			this.grow(worldIn, rand, pos, state);
		}
	}

	@Override
	public boolean canPlaceAt(BlockState state, WorldView world, BlockPos pos) {
		return canSaplingStay(world, this.getSpecies(), pos);
	}

	@Override
	public void grow(@NotNull ServerWorld world, @NotNull Random rand, @NotNull BlockPos pos, @NotNull BlockState state) {
		if (this.canPlaceAt(state, world, pos)) {
			final Species species = this.getSpecies();
			if (species.canSaplingGrow(world, pos)) {
				species.transitionToTree(world, pos);
			}
		} else {
			this.dropBlock(world, state, pos);
		}
	}

	@Override
	public BlockSoundGroup getSoundType(BlockState state, WorldView world, BlockPos pos, @Nullable Entity entity) {
		return this.getSpecies().getSaplingSound();
	}

	///////////////////////////////////////////
	// DROPS
	///////////////////////////////////////////


	@Override
	public void neighborUpdate(BlockState state, World world, BlockPos pos, Block blockIn, BlockPos fromPos, boolean isMoving) {
		if (!this.canPlaceAt(state, world, pos)) {
			this.dropBlock(world, state, pos);
		}
	}

	protected void dropBlock(World world, BlockState state, BlockPos pos) {
		world.spawnEntity(new ItemEntity(world, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, getSpecies().getSeedStack(1)));
		world.removeBlock(pos, false);
	}

	@NotNull
	@Override
	public ItemStack getPickStack(BlockView worldIn, BlockPos pos, BlockState state) {
		return this.getSpecies().getSeedStack(1);
	}

	@NotNull
	@Override
	public List<ItemStack> getDroppedStacks(@NotNull BlockState state, @NotNull LootContext.Builder builder) {
		// If a loot table has been added load those drops instead (until drop creators).
		if (builder.getWorld().getServer().getLootManager().getTableIds().contains(this.getLootTableId())) {
			return super.getDroppedStacks(state, builder);
		}

		return DTConfigs.DYNAMIC_SAPLING_DROPS ?
				Collections.singletonList(this.getSpecies().getSeedStack(1)) :
				Collections.emptyList();
	}

	// TODO: FABRIC PORT: More ItemStack stuff
//	@Override
//	public ItemStack getCloneItemStack(BlockState state, HitResult target, BlockView world, BlockPos pos, PlayerEntity player) {
//		return this.getSpecies().getSeedStack(1);
//	}

	///////////////////////////////////////////
	// PHYSICAL BOUNDS
	///////////////////////////////////////////

	@NotNull
	@Override
	public VoxelShape getOutlineShape(BlockState state, BlockView access, BlockPos pos, ShapeContext context) {
		return this.getSpecies().getSaplingShape();
	}
}
