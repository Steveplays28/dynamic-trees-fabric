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
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DoublePlantBlock;
import net.minecraft.world.level.block.FlowerBlock;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.MushroomBlock;
import net.minecraft.world.level.block.TallGrassBlock;

import java.util.List;

public class PodzolGenFeature extends GenFeature {

    public PodzolGenFeature(ResourceLocation registryName) {
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

        final Level world = context.world();
        final FindEndsNode endFinder = new FindEndsNode();
        TreeHelper.startAnalysisFromRoot(world, context.pos(), new MapSignal(endFinder));
        final List<BlockPos> endPoints = endFinder.getEnds();

        if (endPoints.isEmpty()) {
            return false;
        }

        final RandomSource random = context.random();
        final BlockPos pos = endPoints.get(random.nextInt(endPoints.size()));

        final int x = pos.getX() + random.nextInt(5) - 2;
        final int z = pos.getZ() + random.nextInt(5) - 2;

        final int darkThreshold = 4;

        for (int i = 0; i < 32; i++) {
            final BlockPos offPos = new BlockPos(x, pos.getY() - 1 - i, z);

            if (!world.isEmptyBlock(offPos)) {
                final Block block = world.getBlockState(offPos).getBlock();

                // Skip past Mushrooms and branches on the way down.
                if (block instanceof BranchBlock || block instanceof MushroomBlock || block instanceof LeavesBlock) {
                    continue;
                } else if (block instanceof FlowerBlock || block instanceof TallGrassBlock || block instanceof DoublePlantBlock) {
                    // Kill plants.
                    if (world.getBrightness(LightLayer.SKY, offPos) <= darkThreshold) {
                        world.setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState());
                    }
                    continue;
                } else if (block == Blocks.DIRT || block == Blocks.GRASS) {
                    // Convert grass or dirt to podzol.
                    if (world.getBrightness(LightLayer.SKY, offPos.above()) <= darkThreshold) {
                        world.setBlockAndUpdate(offPos, BlockStates.PODZOL);
                    } else {
                        spreadPodzol(world, pos);
                    }
                }
                break;
            }
        }
        return true;
    }

    public static void spreadPodzol(Level world, BlockPos pos) {
        int podzolish = 0;

        for (Direction dir : CoordUtils.HORIZONTALS) {
            BlockPos deltaPos = pos.relative(dir);
            Block testBlock = world.getBlockState(deltaPos).getBlock();
            podzolish += (testBlock == Blocks.PODZOL) ? 1 : 0;
            podzolish += testBlock instanceof RootyBlock ? 1 : 0;
            if (podzolish >= 3) {
                world.setBlockAndUpdate(pos, BlockStates.PODZOL);
                break;
            }
        }
    }

}
