package io.github.steveplays28.dynamictreesfabric.systems.genfeatures.context;

import io.github.steveplays28.dynamictreesfabric.blocks.rootyblocks.RootyBlock;
import io.github.steveplays28.dynamictreesfabric.systems.poissondisc.PoissonDisc;
import io.github.steveplays28.dynamictreesfabric.trees.Species;
import io.github.steveplays28.dynamictreesfabric.util.SafeChunkBounds;
import io.github.steveplays28.dynamictreesfabric.worldgen.JoCode;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.LevelAccessor;

/**
 * @author Harley O'Connor
 */
public class PreGenerationContext extends GenerationContext<LevelAccessor> {

    private final int radius;
    private final Direction facing;
    private final SafeChunkBounds bounds;
    private final JoCode joCode;

    /**
     * Instantiates a new {@link PreGenerationContext} object.
     *
     * @param world   The {@link LevelAccessor} object.
     * @param rootPos The {@link BlockPos} of the {@link RootyBlock} the generated tree is planted on.
     * @param species The {@link Species} being grown.
     * @param radius  The radius of the {@link PoissonDisc} the tree generated in.
     * @param facing  The {@link Direction} that will be applied to the {@link JoCode} during generation.
     * @param bounds  The {@link SafeChunkBounds} to generate in.
     * @param joCode  The {@link JoCode} generating the tree.
     */
    public PreGenerationContext(LevelAccessor world, BlockPos rootPos, Species species, int radius, Direction facing, SafeChunkBounds bounds, JoCode joCode) {
        super(world, rootPos, species);
        this.radius = radius;
        this.facing = facing;
        this.bounds = bounds;
        this.joCode = joCode;
    }

    public int radius() {
        return radius;
    }

    public Direction facing() {
        return facing;
    }

    public SafeChunkBounds bounds() {
        return bounds;
    }

    public JoCode joCode() {
        return joCode;
    }

    public final boolean isWorldGen() {
        return this.bounds != SafeChunkBounds.ANY;
    }

}
