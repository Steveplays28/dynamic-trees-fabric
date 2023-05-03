package io.github.steveplays28.dynamictreesfabric.systems.substances;

import io.github.steveplays28.dynamictreesfabric.api.TreeHelper;
import io.github.steveplays28.dynamictreesfabric.api.substances.SubstanceEffect;
import io.github.steveplays28.dynamictreesfabric.blocks.rootyblocks.RootyBlock;

import net.minecraft.particle.ParticleTypes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class DepleteSubstance implements SubstanceEffect {

	int amount;

	@Override
	public boolean apply(World world, BlockPos rootPos) {
		final RootyBlock dirt = TreeHelper.getRooty(world.getBlockState(rootPos));

		if (dirt.fertilize(world, rootPos, -amount)) {
			TreeHelper.treeParticles(world, rootPos, ParticleTypes.ANGRY_VILLAGER, 8);
			return true;
		}

		return false;
	}

	@Override
	public String getName() {
		return "deplete";
	}

	public DepleteSubstance setAmount(int amount) {
		this.amount = amount;
		return this;
	}

	@Override
	public boolean isLingering() {
		return false;
	}

}
