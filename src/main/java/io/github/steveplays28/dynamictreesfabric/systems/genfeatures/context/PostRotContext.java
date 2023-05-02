package io.github.steveplays28.dynamictreesfabric.systems.genfeatures.context;

import io.github.steveplays28.dynamictreesfabric.trees.Species;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelAccessor;

/**
 * @author Harley O'Connor
 */
public class PostRotContext extends GenerationContext<LevelAccessor> {

    private final int radius;
    private final int neighbourCount;
    private final int fertility;
    private final boolean rapid;

    /**
     * Instantiates a {@link PostRotContext} object.
     *
     * @param world          The {@link IWorld} object.
     * @param pos            The {@link BlockPos} of the branch that rot.
     * @param species        The {@link Species} of the tree that rotted.
     * @param radius         The radius of the rotted branch.
     * @param neighbourCount The number of neighbours.
     * @param fertility      The fertility of the tree.
     * @param rapid          {@code true} if this rot is happening during generation as opposed to natural tree
     *                       updates.
     */
    public PostRotContext(LevelAccessor world, BlockPos pos, Species species, int radius, int neighbourCount, int fertility, boolean rapid) {
        super(world, pos, species);
        this.radius = radius;
        this.neighbourCount = neighbourCount;
        this.fertility = fertility;
        this.rapid = rapid;
    }

    public int radius() {
        return radius;
    }

    public int neighbourCount() {
        return neighbourCount;
    }

    public int fertility() {
        return fertility;
    }

    public boolean rapid() {
        return rapid;
    }

}
