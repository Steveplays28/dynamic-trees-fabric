package io.github.steveplays28.dynamictreesfabric.systems;

import io.github.steveplays28.dynamictreesfabric.util.function.TetraFunction;
import javax.annotation.Nullable;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockView;
import java.util.HashMap;
import java.util.Map;

/**
 * Allows branches connect to non-tree blocks
 *
 * @author Max Hyper
 */
public class BranchConnectables {

    private static final Map<Block, TetraFunction<BlockState, BlockView, BlockPos, Direction, Integer>> connectablesMap = new HashMap<>();

    //Direction can be null
    public static void makeBlockConnectable(Block block, TetraFunction<BlockState, BlockView, BlockPos, Direction, Integer> radiusFunction) {
        connectablesMap.putIfAbsent(block, radiusFunction);
    }

    public static boolean isBlockConnectable(Block block) {
        return connectablesMap.containsKey(block);
    }

    public static int getConnectionRadiusForBlock(BlockState state, BlockView world, BlockPos pos, @Nullable Direction side) {
        final Block block = state.getBlock();
        return isBlockConnectable(block) ? connectablesMap.get(block).apply(state, world, pos, side) : 0;
    }

}
