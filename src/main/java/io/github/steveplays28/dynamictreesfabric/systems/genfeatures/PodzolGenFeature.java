package io.github.steveplays28.dynamictreesfabric.systems.genfeatures;

import io.github.steveplays28.dynamictreesfabric.api.TreeHelper;
import io.github.steveplays28.dynamictreesfabric.api.network.MapSignal;
import io.github.steveplays28.dynamictreesfabric.blocks.branches.BranchBlock;
import io.github.steveplays28.dynamictreesfabric.blocks.rootyblocks.RootyBlock;
import io.github.steveplays28.dynamictreesfabric.init.DTConfigs;
import io.github.steveplays28.dynamictreesfabric.systems.genfeatures.context.PostGrowContext;
import io.github.steveplays28.dynamictreesfabric.systems.nodemappers.FindEndsNode;
import io.github.steveplays28.dynamictreesfabric.util.BlockStates;
import io.github.steveplays28.dynamictreesfabric.util.CoordUtils;
import java.util.List;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.FernBlock;
import net.minecraft.block.FlowerBlock;
import net.minecraft.block.LeavesBlock;
import net.minecraft.block.MushroomPlantBlock;
import net.minecraft.block.TallPlantBlock;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.LightType;
import net.minecraft.world.World;

public class PodzolGenFeature extends GenFeature {

    public PodzolGenFeature(Identifier registryName) {
        super(registryName);
    }

    @Override
    protected void registerProperties() {
    }

    @Override
    protected boolean postGrow(GenFeatureConfiguration configuration, PostGrowContext context) {
        if (!DTConfigs.PODZOL_GEN.get()) {
            return false;
        }

        final World world = context.world();
        final FindEndsNode endFinder = new FindEndsNode();
        TreeHelper.startAnalysisFromRoot(world, context.pos(), new MapSignal(endFinder));
        final List<BlockPos> endPoints = endFinder.getEnds();

        if (endPoints.isEmpty()) {
            return false;
        }

        final Random random = context.random();
        final BlockPos pos = endPoints.get(random.nextInt(endPoints.size()));

        final int x = pos.getX() + random.nextInt(5) - 2;
        final int z = pos.getZ() + random.nextInt(5) - 2;

        final int darkThreshold = 4;

        for (int i = 0; i < 32; i++) {
            final BlockPos offPos = new BlockPos(x, pos.getY() - 1 - i, z);

            if (!world.isAir(offPos)) {
                final Block block = world.getBlockState(offPos).getBlock();

                // Skip past Mushrooms and branches on the way down.
                if (block instanceof BranchBlock || block instanceof MushroomPlantBlock || block instanceof LeavesBlock) {
                    continue;
                } else if (block instanceof FlowerBlock || block instanceof FernBlock || block instanceof TallPlantBlock) {
                    // Kill plants.
                    if (world.getLightLevel(LightType.SKY, offPos) <= darkThreshold) {
                        world.setBlockState(pos, Blocks.AIR.getDefaultState());
                    }
                    continue;
                } else if (block == Blocks.DIRT || block == Blocks.GRASS) {
                    // Convert grass or dirt to podzol.
                    if (world.getLightLevel(LightType.SKY, offPos.up()) <= darkThreshold) {
                        world.setBlockState(offPos, BlockStates.PODZOL);
                    } else {
                        spreadPodzol(world, pos);
                    }
                }
                break;
            }
        }
        return true;
    }

    public static void spreadPodzol(World world, BlockPos pos) {
        int podzolish = 0;

        for (Direction dir : CoordUtils.HORIZONTALS) {
            BlockPos deltaPos = pos.offset(dir);
            Block testBlock = world.getBlockState(deltaPos).getBlock();
            podzolish += (testBlock == Blocks.PODZOL) ? 1 : 0;
            podzolish += testBlock instanceof RootyBlock ? 1 : 0;
            if (podzolish >= 3) {
                world.setBlockState(pos, BlockStates.PODZOL);
                break;
            }
        }
    }

}
