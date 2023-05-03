package io.github.steveplays28.dynamictreesfabric.growthlogic.context;

import io.github.steveplays28.dynamictreesfabric.trees.Species;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/**
 * @author Harley O'Connor
 */
public class PositionalSpeciesContext {
    private final World world;
    private final BlockPos pos;
    private final Species species;

    public PositionalSpeciesContext(World world, BlockPos pos, Species species) {
        this.world = world;
        this.pos = pos;
        this.species = species;
    }

    public World world() {
        return world;
    }

    public BlockPos pos() {
        return pos;
    }

    public Species species() {
        return species;
    }
}
