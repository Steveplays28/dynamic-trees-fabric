package io.github.steveplays28.dynamictreesfabric.systems.substances;

import io.github.steveplays28.dynamictreesfabric.api.TreeHelper;
import io.github.steveplays28.dynamictreesfabric.api.network.MapSignal;
import io.github.steveplays28.dynamictreesfabric.api.substances.SubstanceEffect;
import io.github.steveplays28.dynamictreesfabric.blocks.rootyblocks.RootyBlock;
import io.github.steveplays28.dynamictreesfabric.systems.nodemappers.FreezerNode;
import io.github.steveplays28.dynamictreesfabric.trees.Species;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public class FreezeSubstance implements SubstanceEffect {

    @Override
    public boolean apply(Level world, BlockPos rootPos) {
        final BlockState rootyState = world.getBlockState(rootPos);
        final RootyBlock dirt = TreeHelper.getRooty(rootyState);
        final Species species = dirt.getSpecies(rootyState, world, rootPos);

        if (species != Species.NULL_SPECIES && dirt != null) {
            if (world.isClientSide) {
                TreeHelper.treeParticles(world, rootPos, ParticleTypes.FIREWORK, 8);
            } else {
                dirt.startAnalysis(world, rootPos, new MapSignal(new FreezerNode(species)));
                dirt.fertilize(world, rootPos, -15); // Destroy the fertility so it can no longer grow.
            }
            return true;
        }
        return false;
    }

    @Override
    public String getName() {
        return "freeze";
    }

    @Override
    public boolean isLingering() {
        return false;
    }

}
