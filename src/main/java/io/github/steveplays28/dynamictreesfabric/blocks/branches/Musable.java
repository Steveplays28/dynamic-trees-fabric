package io.github.steveplays28.dynamictreesfabric.blocks.branches;

import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;

@FunctionalInterface
public interface Musable {
    boolean isMusable(BlockView world, BlockState state, BlockPos pos);
}
