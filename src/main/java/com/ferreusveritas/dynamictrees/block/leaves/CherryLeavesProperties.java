package com.ferreusveritas.dynamictrees.block.leaves;

import com.ferreusveritas.dynamictrees.api.registry.TypedRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ParticleUtils;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

import org.jetbrains.annotations.NotNull;

public class CherryLeavesProperties extends LeavesProperties {

    public static final TypedRegistry.EntryType<LeavesProperties> TYPE = TypedRegistry.newType(CherryLeavesProperties::new);

    public CherryLeavesProperties(ResourceLocation registryName) {
        super(registryName);
    }

    @Override
    @NotNull
    protected DynamicLeavesBlock createDynamicLeaves(@NotNull BlockBehaviour.Properties properties) {
        return new DynamicLeavesBlock(this, properties) {

            public void animateTick(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos, @NotNull RandomSource random) {
                super.animateTick(state, level, pos, random);
                if (random.nextInt(10) == 0) {
                    BlockPos blockpos = pos.below();
                    BlockState blockstate = level.getBlockState(blockpos);
                    if (!isFaceFull(blockstate.getCollisionShape(level, blockpos), Direction.UP)) {
                        ParticleUtils.spawnParticleBelow(level, pos, random, ParticleTypes.CHERRY_LEAVES);
                    }
                }
            }
        };
    }

}
