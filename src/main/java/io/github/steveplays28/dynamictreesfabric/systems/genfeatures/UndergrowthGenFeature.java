package io.github.steveplays28.dynamictreesfabric.systems.genfeatures;

import io.github.steveplays28.dynamictreesfabric.data.DTBlockTags;
import io.github.steveplays28.dynamictreesfabric.systems.genfeatures.context.PostGenerationContext;
import io.github.steveplays28.dynamictreesfabric.trees.Species;
import io.github.steveplays28.dynamictreesfabric.util.CoordUtils;
import io.github.steveplays28.dynamictreesfabric.util.SafeChunkBounds;
import io.github.steveplays28.dynamictreesfabric.util.SimpleVoxmap;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class UndergrowthGenFeature extends GenFeature {

    public UndergrowthGenFeature(ResourceLocation registryName) {
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

        final LevelAccessor world = context.world();
        final BlockPos rootPos = context.pos();
        final SafeChunkBounds bounds = context.bounds();
        final Species species = context.species();

        final Vec3 vTree = new Vec3(rootPos.getX(), rootPos.getY(), rootPos.getZ()).add(0.5, 0.5, 0.5);

        for (int i = 0; i < 2; i++) {

            int rad = Mth.clamp(world.getRandom().nextInt(radius - 2) + 2, 2, radius - 1);
            Vec3 v = vTree.add(new Vec3(1, 0, 0).scale(rad).yRot((float) (world.getRandom().nextFloat() * Math.PI * 2)));
            BlockPos vPos = new BlockPos(v);

            if (!bounds.inBounds(vPos, true)) {
                continue;
            }

            final BlockPos groundPos = CoordUtils.findWorldSurface(world, vPos, true);
            final BlockState soilBlockState = world.getBlockState(groundPos);

            BlockPos pos = groundPos.above();
            if (species.isAcceptableSoil(world, groundPos, soilBlockState)) {
                final int type = world.getRandom().nextInt(2);
                world.setBlock(pos, (type == 0 ? Blocks.OAK_LOG : Blocks.JUNGLE_LOG).defaultBlockState(), 2);
                pos = pos.above(world.getRandom().nextInt(3));

                final BlockState leavesState = (type == 0 ? Blocks.OAK_LEAVES : Blocks.JUNGLE_LEAVES).defaultBlockState().setValue(LeavesBlock.PERSISTENT, true);

                final SimpleVoxmap leafMap = species.getLeavesProperties().getCellKit().getLeafCluster();
                final BlockPos.MutableBlockPos leafPos = new BlockPos.MutableBlockPos();
                for (BlockPos.MutableBlockPos dPos : leafMap.getAllNonZero()) {
                    leafPos.set(pos.getX() + dPos.getX(), pos.getY() + dPos.getY(), pos.getZ() + dPos.getZ());

                    if (bounds.inBounds(leafPos, true) && (CoordUtils.coordHashCode(leafPos, 0) % 5) != 0) {
                        BlockState blockState = world.getBlockState(leafPos);
                        if (blockState.isAir() || blockState.is(DTBlockTags.LEAVES) || blockState.is(DTBlockTags.FOLIAGE)) {
                            world.setBlock(leafPos, leavesState, 2);
                        }
                    }
                }
            }
        }

        return true;
    }

}
