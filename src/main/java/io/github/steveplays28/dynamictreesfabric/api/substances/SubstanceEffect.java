package io.github.steveplays28.dynamictreesfabric.api.substances;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/**
 * A substance effect is like a potion effect but for trees.
 *
 * @author ferreusveritas
 */
public interface SubstanceEffect {

    /**
     * For an instant effect.
     *
     * @param world
     * @param rootPos
     * @return true for success.  false otherwise
     */
    boolean apply(World world, BlockPos rootPos);

    /**
     * For a continuously updating effect.
     *
     * @param world
     * @param rootPos
     * @param deltaTicks
     * @return true to stay alive. false to kill effector
     */
    default boolean update(World world, BlockPos rootPos, int deltaTicks, int fertility) {
        return false;
    }

    /**
     * Get the name of the effect.  Used to compare existing effects in the environment.
     *
     * @return the name of the effect.
     */
    String getName();

    /**
     * Determines if the effect is continuous or instant
     *
     * @return true if continuous, false if instant
     */
    boolean isLingering();

}
