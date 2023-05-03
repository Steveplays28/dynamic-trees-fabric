package io.github.steveplays28.dynamictreesfabric.api;

import io.github.steveplays28.dynamictreesfabric.util.SafeChunkBounds;

import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.WorldAccess;

@FunctionalInterface
public interface Ageable {

	/**
	 * @param world The world
	 * @param pos   the position of this block that is being aged
	 * @param state the state of this block
	 * @param rand  random number generator
	 * @return -1 if block was destroyed after the ageing, otherwise the hydro value of the block
	 */
	int age(WorldAccess world, BlockPos pos, BlockState state, Random rand, SafeChunkBounds safeBounds);

}
