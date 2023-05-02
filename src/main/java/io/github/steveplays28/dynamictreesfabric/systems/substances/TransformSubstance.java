package io.github.steveplays28.dynamictreesfabric.systems.substances;

import io.github.steveplays28.dynamictreesfabric.api.TreeHelper;
import io.github.steveplays28.dynamictreesfabric.api.network.MapSignal;
import io.github.steveplays28.dynamictreesfabric.api.substances.SubstanceEffect;
import io.github.steveplays28.dynamictreesfabric.blocks.rootyblocks.RootyBlock;
import io.github.steveplays28.dynamictreesfabric.systems.nodemappers.TransformNode;
import io.github.steveplays28.dynamictreesfabric.trees.Species;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public class TransformSubstance implements SubstanceEffect {

    private final Species toSpecies;

    public TransformSubstance(final Species toTree) {
        this.toSpecies = toTree;
    }

    @Override
    public boolean apply(Level world, BlockPos rootPos) {

        final BlockState rootyState = world.getBlockState(rootPos);
        final RootyBlock dirt = TreeHelper.getRooty(rootyState);

        if (dirt != null && this.toSpecies.isValid()) {
            Species fromSpecies = dirt.getSpecies(rootyState, world, rootPos);
            if (fromSpecies.isTransformable() && fromSpecies != this.toSpecies) {
                if (world.isClientSide) {
                    TreeHelper.treeParticles(world, rootPos, ParticleTypes.FIREWORK, 8);
                } else {
                    dirt.startAnalysis(world, rootPos, new MapSignal(new TransformNode(fromSpecies, toSpecies)));
                }
                return true;
            }
        }


        return false;
    }

    @Override
    public String getName() {
        return "transform";
    }

    @Override
    public boolean isLingering() {
        return false;
    }

}
