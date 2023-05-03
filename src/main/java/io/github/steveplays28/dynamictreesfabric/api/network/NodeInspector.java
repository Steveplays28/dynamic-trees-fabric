package io.github.steveplays28.dynamictreesfabric.api.network;

import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.WorldAccess;

public interface NodeInspector {

	boolean run(BlockState blockState, WorldAccess world, BlockPos pos, Direction fromDir);

	boolean returnRun(BlockState blockState, WorldAccess world, BlockPos pos, Direction fromDir);

}
