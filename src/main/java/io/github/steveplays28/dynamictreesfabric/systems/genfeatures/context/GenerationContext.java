package io.github.steveplays28.dynamictreesfabric.systems.genfeatures.context;

import io.github.steveplays28.dynamictreesfabric.trees.Species;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelAccessor;

/**
 * @author Harley O'Connor
 */
public abstract class GenerationContext<W extends LevelAccessor> {

    private final W world;
    private final BlockPos pos;
    private final Species species;

    public GenerationContext(W world, BlockPos pos, Species species) {
        this.world = world;
        this.pos = pos;
        this.species = species;
    }

    public W world() {
        return world;
    }

    public BlockPos pos() {
        return pos;
    }

    public Species species() {
        return species;
    }

    public final RandomSource random() {
        return this.world.getRandom();
    }

}
