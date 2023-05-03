package io.github.steveplays28.dynamictreesfabric.systems.nodemappers;

import io.github.steveplays28.dynamictreesfabric.api.TreeHelper;
import io.github.steveplays28.dynamictreesfabric.api.network.NodeInspector;
import io.github.steveplays28.dynamictreesfabric.init.DTClient;

import net.minecraft.block.BlockState;
import net.minecraft.particle.DefaultParticleType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.WorldAccess;

public class TwinkleNode implements NodeInspector {

	private final DefaultParticleType particleType;
	private final int numParticles;

	public TwinkleNode(DefaultParticleType type, int num) {
		particleType = type;
		numParticles = num;
	}

	@Override
	public boolean run(BlockState blockState, WorldAccess world, BlockPos pos, Direction fromDir) {
		if (world.isClient() && TreeHelper.isBranch(blockState)) {
			DTClient.spawnParticles(world, this.particleType, pos.getX(), pos.getY(), pos.getZ(), this.numParticles, world.getRandom());
		}
		return false;
	}

	@Override
	public boolean returnRun(BlockState blockState, WorldAccess world, BlockPos pos, Direction fromDir) {
		return false;
	}

}
