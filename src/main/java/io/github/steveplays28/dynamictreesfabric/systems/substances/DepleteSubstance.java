package io.github.steveplays28.dynamictreesfabric.systems.substances;

import io.github.steveplays28.dynamictreesfabric.api.TreeHelper;
import io.github.steveplays28.dynamictreesfabric.api.substances.SubstanceEffect;
import io.github.steveplays28.dynamictreesfabric.blocks.rootyblocks.RootyBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.level.Level;

public class DepleteSubstance implements SubstanceEffect {

    int amount;

    @Override
    public boolean apply(Level world, BlockPos rootPos) {
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
