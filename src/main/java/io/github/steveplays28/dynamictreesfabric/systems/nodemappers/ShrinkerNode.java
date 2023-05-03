package io.github.steveplays28.dynamictreesfabric.systems.nodemappers;

import io.github.steveplays28.dynamictreesfabric.api.TreeHelper;
import io.github.steveplays28.dynamictreesfabric.api.network.NodeInspector;
import io.github.steveplays28.dynamictreesfabric.blocks.branches.BranchBlock;
import io.github.steveplays28.dynamictreesfabric.trees.Species;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.WorldAccess;

public class ShrinkerNode implements NodeInspector {

    private float radius;
    Species species;

    public ShrinkerNode(Species species) {
        this.species = species;
    }

    @Override
    public boolean run(BlockState blockState, WorldAccess world, BlockPos pos, Direction fromDir) {

        BranchBlock branch = TreeHelper.getBranch(blockState);

        if (branch != null) {
            radius = branch.getRadius(blockState);
            if (radius > BranchBlock.MAX_RADIUS) {
                branch.setRadius(world, pos, BranchBlock.MAX_RADIUS, fromDir);
            }
        }

        return false;
    }

    @Override
    public boolean returnRun(BlockState blockState, WorldAccess world, BlockPos pos, Direction fromDir) {
        return false;
    }

}
