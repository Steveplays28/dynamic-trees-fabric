package io.github.steveplays28.dynamictreesfabric.systems.nodemappers;

import io.github.steveplays28.dynamictreesfabric.api.TreeHelper;
import io.github.steveplays28.dynamictreesfabric.api.network.NodeInspector;
import io.github.steveplays28.dynamictreesfabric.blocks.branches.BranchBlock;
import io.github.steveplays28.dynamictreesfabric.init.DTRegistries;
import io.github.steveplays28.dynamictreesfabric.util.CoordUtils;
import net.minecraft.block.BlockState;
import net.minecraft.block.CocoaBlock;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.WorldAccess;

public class CocoaFruitNode implements NodeInspector {

    private boolean finished = false;
    private boolean worldGen = false;

    public CocoaFruitNode setWorldGen(boolean worldGen) {
        this.worldGen = worldGen;
        return this;
    }

    @Override
    public boolean run(BlockState blockState, WorldAccess world, BlockPos pos, Direction fromDir) {

        if (!finished) {
            int hashCode = CoordUtils.coordHashCode(pos, 1);
            if ((hashCode % 97) % 29 == 0) {
                BranchBlock branch = TreeHelper.getBranch(blockState);
                if (branch != null && branch.getRadius(blockState) == 8) {
                    int side = (hashCode % 4) + 2;
                    Direction dir = Direction.byId(side);
                    BlockPos deltaPos = pos.offset(dir);
                    if (world.isAir(deltaPos)) {
                        if (!dir.getAxis().isHorizontal()) {
                            dir = Direction.NORTH;
                        }
                        world.setBlockState(deltaPos, DTRegistries.COCOA_FRUIT.get().getDefaultState().with(CocoaBlock.FACING, dir.getOpposite()).with(CocoaBlock.AGE, worldGen ? 2 : 0), 2);
                    }
                } else {
                    finished = true;
                }
            }
        }
        return false;
    }

    @Override
    public boolean returnRun(BlockState blockState, WorldAccess world, BlockPos pos, Direction fromDir) {
        return false;
    }

}
