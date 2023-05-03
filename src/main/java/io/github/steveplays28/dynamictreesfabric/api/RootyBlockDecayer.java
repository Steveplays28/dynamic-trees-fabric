package io.github.steveplays28.dynamictreesfabric.api;

import io.github.steveplays28.dynamictreesfabric.blocks.rootyblocks.RootyBlock;
import io.github.steveplays28.dynamictreesfabric.trees.Species;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/**
 * Implementations of this {@code interface} allow for custom logic when decaying {@link RootyBlock}s after a tree has
 * fallen.
 *
 * <p>The implementation should be registered via
 * {@link TreeHelper#setCustomRootBlockDecay(RootyBlockDecayer)}.</p>
 *
 * @author ferreusveritas
 */
@FunctionalInterface
public interface RootyBlockDecayer {

    /**
     * Implementations perform their custom {@link RootyBlock} decay logic.
     *
     * @param world      The {@link World} instance.
     * @param rootPos    The {@link BlockPos} of the {@link RootyBlock}.
     * @param rootyState The {@link BlockState} of the {@link RootyBlock}.
     * @param species    The {@link Species} of the tree that was removed.
     * @return {@code true} if handled; otherwise {@code false} to run the default decay algorithm.
     */
    boolean decay(World world, BlockPos rootPos, BlockState rootyState, Species species);

}
