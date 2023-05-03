package io.github.steveplays28.dynamictreesfabric.items;

import org.jetbrains.annotations.Nullable;

import io.github.steveplays28.dynamictreesfabric.init.DTConfigs;
import io.github.steveplays28.dynamictreesfabric.init.DTRegistries;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.stat.Stats;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;

public class DirtBucket extends Item {

	public DirtBucket() {
		super(new Item.Settings().maxCount(1).tab(DTRegistries.ITEM_GROUP));
	}

	@Override
	public boolean hasCraftingRemainingItem(ItemStack stack) {
		return true;
	}

	@Override
	public ItemStack getCraftingRemainingItem(ItemStack itemStack) {
		return itemStack.copy();
	}

	@Override
	public TypedActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {

		final ItemStack itemStack = player.getStackInHand(hand);
		final BlockHitResult blockRayTraceResult;

		{
			blockRayTraceResult = raycast(world, player, RaycastContext.FluidHandling.NONE);
			if (blockRayTraceResult.getType() != HitResult.Type.BLOCK) {
				return new TypedActionResult<>(ActionResult.FAIL, itemStack);
			}
		}

		if (DTConfigs.DIRT_BUCKET_PLACES_DIRT.get()) {
			if (blockRayTraceResult.getType() != HitResult.Type.BLOCK) {
				return new TypedActionResult<>(ActionResult.PASS, itemStack);
			} else {
				final BlockPos pos = blockRayTraceResult.getBlockPos();

				if (!world.canPlayerModifyAt(player, pos)) {
					return new TypedActionResult<>(ActionResult.FAIL, itemStack);
				} else {
					final boolean isReplaceable = world.getBlockState(pos).getMaterial().isReplaceable();
					final BlockPos workingPos = isReplaceable && blockRayTraceResult.getSide() == Direction.UP ? pos : pos.offset(blockRayTraceResult.getSide());

					if (!player.canPlaceOn(workingPos, blockRayTraceResult.getSide(), itemStack)) {
						return new TypedActionResult<>(ActionResult.FAIL, itemStack);
					} else if (this.tryPlaceContainedDirt(player, world, workingPos)) {
						player.incrementStat(Stats.USED.getOrCreateStat(this));
						return !player.getAbilities().creativeMode ? new TypedActionResult<>(ActionResult.SUCCESS, new ItemStack(Items.BUCKET)) : new TypedActionResult<>(ActionResult.SUCCESS, itemStack);
					} else {
						return new TypedActionResult<>(ActionResult.FAIL, itemStack);
					}
				}
			}
		} else {
			return new TypedActionResult<>(ActionResult.PASS, itemStack);
		}
	}

	public boolean tryPlaceContainedDirt(@Nullable PlayerEntity player, World world, BlockPos posIn) {
		BlockState blockState = world.getBlockState(posIn);
		if (blockState.getMaterial().isReplaceable()) {
			if (!world.isClient && !blockState.isAir()) {
				world.breakBlock(posIn, true);
			}

			world.playSound(player, posIn, SoundEvents.BLOCK_GRAVEL_PLACE, SoundCategory.BLOCKS, 1.0F, 0.8F);
			world.setBlockState(posIn, Blocks.DIRT.getDefaultState(), 11);
			return true;
		}

		return false;
	}

}
