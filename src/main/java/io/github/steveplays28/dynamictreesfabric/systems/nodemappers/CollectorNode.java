package io.github.steveplays28.dynamictreesfabric.systems.nodemappers;

import io.github.steveplays28.dynamictreesfabric.api.network.NodeInspector;
import java.util.Set;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.WorldAccess;

/**
 * @author ferreusveritas
 */
public class CollectorNode implements NodeInspector {

    private final Set<BlockPos> nodeSet;

    public CollectorNode(Set<BlockPos> nodeSet) {
        this.nodeSet = nodeSet;
    }

    @Override
    public boolean run(BlockState blockState, WorldAccess world, BlockPos pos, Direction fromDir) {
        nodeSet.add(pos);
        return false;
    }

    @Override
    public boolean returnRun(BlockState blockState, WorldAccess world, BlockPos pos, Direction fromDir) {
        return false;
    }

    public boolean contains(BlockPos pos) {
        return nodeSet.contains(pos);
    }

}
