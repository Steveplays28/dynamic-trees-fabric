package io.github.steveplays28.dynamictreesfabric.event;

import io.github.steveplays28.dynamictreesfabric.api.FutureBreakable;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import java.util.LinkedList;
import java.util.List;

public class FutureBreak {

    public static final List<FutureBreak> FUTURE_BREAKS = new LinkedList<>();

    public final BlockState state;
    public final Level world;
    public final BlockPos pos;
    public final LivingEntity entity;
    public int ticks;

    public FutureBreak(BlockState state, Level world, BlockPos pos, LivingEntity entity, int ticks) {
        this.state = state;
        this.world = world;
        this.pos = pos;
        this.entity = entity;
        this.ticks = ticks;
    }

    public static void add(FutureBreak fb) {
        if (!fb.world.isClientSide) {
            FUTURE_BREAKS.add(fb);
        }
    }

    public static void process(Level world) {
        if (FUTURE_BREAKS.isEmpty()) {
            return;
        }

        for (final FutureBreak futureBreak : new LinkedList<>(FUTURE_BREAKS)) {
            if (world != futureBreak.world) {
                continue;
            }

            if (!(futureBreak.state.getBlock() instanceof FutureBreakable)) {
                FUTURE_BREAKS.remove(futureBreak);
                continue;
            }

            if (futureBreak.ticks-- > 0) {
                continue;
            }

            final FutureBreakable futureBreakable = (FutureBreakable) futureBreak.state.getBlock();
            futureBreakable.futureBreak(futureBreak.state, world, futureBreak.pos, futureBreak.entity);
            FUTURE_BREAKS.remove(futureBreak);
        }
    }

}

