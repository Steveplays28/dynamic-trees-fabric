package io.github.steveplays28.dynamictreesfabric.blocks;

import io.github.steveplays28.dynamictreesfabric.api.cells.Cell;
import io.github.steveplays28.dynamictreesfabric.api.cells.CellNull;
import io.github.steveplays28.dynamictreesfabric.api.network.MapSignal;
import io.github.steveplays28.dynamictreesfabric.api.treedata.TreePart;
import io.github.steveplays28.dynamictreesfabric.blocks.branches.BranchBlock;
import io.github.steveplays28.dynamictreesfabric.blocks.leaves.LeavesProperties;
import io.github.steveplays28.dynamictreesfabric.systems.BranchConnectables;
import io.github.steveplays28.dynamictreesfabric.systems.GrowSignal;
import io.github.steveplays28.dynamictreesfabric.trees.Family;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;

public class NullTreePart implements TreePart {

    //This is a safe dump for blocks that aren't tree parts
    //Handles some vanilla blocks

    @Override
    public Cell getHydrationCell(BlockView reader, BlockPos pos, BlockState state, Direction dir, LeavesProperties leavesTree) {
        return CellNull.NULL_CELL;
    }

    @Override
    public GrowSignal growSignal(World world, BlockPos pos, GrowSignal signal) {
        return signal;
    }

    @Override
    public int getRadiusForConnection(BlockState state, BlockView reader, BlockPos pos, BranchBlock from, Direction side, int fromRadius) {
        // Connectable blocks such as bee nests and shroomlight will be handled here.
        if (BranchConnectables.isBlockConnectable(state.getBlock())) {
            int rad = BranchConnectables.getConnectionRadiusForBlock(state, reader, pos, side);
            if (rad > 0) {
                return rad;
            }
        }

        return 0;
    }

    @Override
    public int probabilityForBlock(BlockState state, BlockView reader, BlockPos pos, BranchBlock from) {
        return state.isAir() ? 1 : 0;
    }

    @Override
    public int getRadius(BlockState state) {
        return 0;
    }

    @Override
    public boolean shouldAnalyse(BlockState state, BlockView reader, BlockPos pos) {
        return BranchConnectables.isBlockConnectable(state.getBlock());
    }

    @Override
    public MapSignal analyse(BlockState state, WorldAccess world, BlockPos pos, Direction fromDir, MapSignal signal) {
        signal.run(state, world, pos, fromDir);
        return signal;
    }

    @Override
    public int branchSupport(BlockState state, BlockView reader, BranchBlock branch, BlockPos pos, Direction dir, int radius) {
        return 0;
    }

    @Override
    public Family getFamily(BlockState state, BlockView reader, BlockPos pos) {
        return Family.NULL_FAMILY;
    }

    public final TreePartType getTreePartType() {
        return TreePartType.NULL;
    }
}
