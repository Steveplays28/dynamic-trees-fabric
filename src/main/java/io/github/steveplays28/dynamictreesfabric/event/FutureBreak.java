package io.github.steveplays28.dynamictreesfabric.event;

import java.util.LinkedList;
import java.util.List;

import io.github.steveplays28.dynamictreesfabric.api.FutureBreakable;

import net.minecraft.block.BlockState;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class FutureBreak {

	public static final List<FutureBreak> FUTURE_BREAKS = new LinkedList<>();

	public final BlockState state;
	public final World world;
	public final BlockPos pos;
	public final LivingEntity entity;
	public int ticks;

	public FutureBreak(BlockState state, World world, BlockPos pos, LivingEntity entity, int ticks) {
		this.state = state;
		this.world = world;
		this.pos = pos;
		this.entity = entity;
		this.ticks = ticks;
	}

	public static void add(FutureBreak fb) {
		if (!fb.world.isClient) {
			FUTURE_BREAKS.add(fb);
		}
	}

	public static void process(World world) {
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

