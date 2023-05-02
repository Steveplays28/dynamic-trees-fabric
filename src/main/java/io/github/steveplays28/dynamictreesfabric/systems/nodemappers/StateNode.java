package io.github.steveplays28.dynamictreesfabric.systems.nodemappers;

import io.github.steveplays28.dynamictreesfabric.api.TreeHelper;
import io.github.steveplays28.dynamictreesfabric.api.network.NodeInspector;
import io.github.steveplays28.dynamictreesfabric.blocks.branches.BranchBlock;
import io.github.steveplays28.dynamictreesfabric.util.BranchConnectionData;
import io.github.steveplays28.dynamictreesfabric.util.Connections;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;

import java.util.HashMap;
import java.util.Map;

/**
 * Makes a BlockPos -> BlockState map for all of the branches
 *
 * @author ferreusveritas
 */
public class StateNode implements NodeInspector {

    private final Map<BlockPos, BranchConnectionData> map = new HashMap<>();
    private final BlockPos origin;

    public StateNode(BlockPos origin) {
        this.origin = origin;
    }

    public Map<BlockPos, BranchConnectionData> getBranchConnectionMap() {
        return map;
    }

    @Override
    public boolean run(BlockState blockState, LevelAccessor world, BlockPos pos, Direction fromDir) {
        BranchBlock branch = TreeHelper.getBranch(blockState);

        if (branch != null) {
            Connections connData = branch.getConnectionData(world, pos, blockState);
            map.put(pos.subtract(origin), new BranchConnectionData(blockState, connData));
        }

        return true;
    }

    @Override
    public boolean returnRun(BlockState blockState, LevelAccessor world, BlockPos pos, Direction fromDir) {
        return false;
    }

}
