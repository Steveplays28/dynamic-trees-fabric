package io.github.steveplays28.dynamictreesfabric.systems.genfeatures;

import io.github.steveplays28.dynamictreesfabric.data.DTBlockTags;
import io.github.steveplays28.dynamictreesfabric.systems.genfeatures.context.PostGenerationContext;
import io.github.steveplays28.dynamictreesfabric.trees.Species;
import io.github.steveplays28.dynamictreesfabric.util.CoordUtils;
import io.github.steveplays28.dynamictreesfabric.util.SafeChunkBounds;
import io.github.steveplays28.dynamictreesfabric.util.SimpleVoxmap;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.LeavesBlock;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.WorldAccess;

public class UndergrowthGenFeature extends GenFeature {

    public UndergrowthGenFeature(Identifier registryName) {
        super(registryName);
    }

    @Override
    protected void registerProperties() {
    }

    @Override
    protected boolean postGenerate(GenFeatureConfiguration configuration, PostGenerationContext context) {
        final boolean worldGen = context.isWorldGen();
        final int radius = context.radius();

        if (!worldGen || radius <= 2) {
            return false;
        }

        final WorldAccess world = context.world();
        final BlockPos rootPos = context.pos();
        final SafeChunkBounds bounds = context.bounds();
        final Species species = context.species();

        final Vec3d vTree = new Vec3d(rootPos.getX(), rootPos.getY(), rootPos.getZ()).add(0.5, 0.5, 0.5);

        for (int i = 0; i < 2; i++) {

            int rad = MathHelper.clamp(world.getRandom().nextInt(radius - 2) + 2, 2, radius - 1);
            Vec3d v = vTree.add(new Vec3d(1, 0, 0).multiply(rad).rotateY((float) (world.getRandom().nextFloat() * Math.PI * 2)));
            BlockPos vPos = new BlockPos(v);

            if (!bounds.inBounds(vPos, true)) {
                continue;
            }

            final BlockPos groundPos = CoordUtils.findWorldSurface(world, vPos, true);
            final BlockState soilBlockState = world.getBlockState(groundPos);

            BlockPos pos = groundPos.up();
            if (species.isAcceptableSoil(world, groundPos, soilBlockState)) {
                final int type = world.getRandom().nextInt(2);
                world.setBlockState(pos, (type == 0 ? Blocks.OAK_LOG : Blocks.JUNGLE_LOG).getDefaultState(), 2);
                pos = pos.up(world.getRandom().nextInt(3));

                final BlockState leavesState = (type == 0 ? Blocks.OAK_LEAVES : Blocks.JUNGLE_LEAVES).getDefaultState().with(LeavesBlock.PERSISTENT, true);

                final SimpleVoxmap leafMap = species.getLeavesProperties().getCellKit().getLeafCluster();
                final BlockPos.Mutable leafPos = new BlockPos.Mutable();
                for (BlockPos.Mutable dPos : leafMap.getAllNonZero()) {
                    leafPos.set(pos.getX() + dPos.getX(), pos.getY() + dPos.getY(), pos.getZ() + dPos.getZ());

                    if (bounds.inBounds(leafPos, true) && (CoordUtils.coordHashCode(leafPos, 0) % 5) != 0) {
                        BlockState blockState = world.getBlockState(leafPos);
                        if (blockState.isAir() || blockState.isIn(DTBlockTags.LEAVES) || blockState.isIn(DTBlockTags.FOLIAGE)) {
                            world.setBlockState(leafPos, leavesState, 2);
                        }
                    }
                }
            }
        }

        return true;
    }

}
