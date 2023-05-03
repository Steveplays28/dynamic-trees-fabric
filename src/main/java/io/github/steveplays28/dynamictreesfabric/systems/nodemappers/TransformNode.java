package io.github.steveplays28.dynamictreesfabric.systems.nodemappers;

import io.github.steveplays28.dynamictreesfabric.api.TreeHelper;
import io.github.steveplays28.dynamictreesfabric.api.network.NodeInspector;
import io.github.steveplays28.dynamictreesfabric.blocks.branches.BranchBlock;
import io.github.steveplays28.dynamictreesfabric.blocks.leaves.DynamicLeavesBlock;
import io.github.steveplays28.dynamictreesfabric.trees.Species;
import io.github.steveplays28.dynamictreesfabric.util.SimpleVoxmap;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.WorldAccess;

public class TransformNode implements NodeInspector {

    private final Species fromSpecies;
    private final Species toSpecies;

    public TransformNode(Species fromTree, Species toTree) {
        this.fromSpecies = fromTree;
        this.toSpecies = toTree;
    }

    @Override
    public boolean run(BlockState blockState, WorldAccess world, BlockPos pos, Direction fromDir) {
        BranchBlock branch = TreeHelper.getBranch(blockState);

        if (branch != null && fromSpecies.getFamily() == branch.getFamily()) {
            int radius = branch.getRadius(blockState);
            if (radius > 0) {
                BranchBlock newBranchBlock = toSpecies.getFamily().getBranch().orElse(null);

                // If the branch is stripped, make the replacement branch stripped.
                if (fromSpecies.getFamily().getStrippedBranch().orElse(null) == branch) {
                    newBranchBlock = toSpecies.getFamily().getStrippedBranch().get();
                }

                newBranchBlock.setRadius(world, pos, radius, null);
                if (radius == 1) {
                    transformSurroundingLeaves(world, pos);
                }
            }
        }

        return true;
    }

    @Override
    public boolean returnRun(BlockState blockState, WorldAccess world, BlockPos pos, Direction fromDir) {
        return false;
    }

    private static final int TEST_LEAVES_RADIUS = 3;

    public void transformSurroundingLeaves(WorldAccess world, BlockPos twigPos) {
        if (world.isClient()) {
            return;
        }

        final SimpleVoxmap leafCluster = this.fromSpecies.getLeavesProperties().getCellKit().getLeafCluster();
        final int xBound = leafCluster.getLenX();
        final int yBound = leafCluster.getLenY();
        final int zBound = leafCluster.getLenZ();

        BlockPos.stream(twigPos.add(-xBound, -yBound, -zBound), twigPos.add(xBound, yBound, zBound)).forEach(testPos -> {
            // We're only interested in where leaves could possibly be.
            if (this.fromSpecies.getLeavesProperties().getCellKit().getLeafCluster().getVoxel(twigPos, testPos) == 0) {
                return;
            }

            final BlockState state = world.getBlockState(testPos);
            if (fromSpecies.getFamily().isCompatibleGenericLeaves(this.fromSpecies, state, world, testPos)) {
                final int hydro = state.getBlock() instanceof DynamicLeavesBlock ? state.get(DynamicLeavesBlock.DISTANCE) : 2;
                world.setBlockState(testPos, toSpecies.getLeavesProperties().getDynamicLeavesState(hydro), 3);
            }
        });
    }

}
