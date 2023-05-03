package io.github.steveplays28.dynamictreesfabric.systems.genfeatures.context;

import io.github.steveplays28.dynamictreesfabric.trees.Species;
import io.github.steveplays28.dynamictreesfabric.util.SafeChunkBounds;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.biome.Biome;

/**
 * @author Harley O'Connor
 */
public final class FullGenerationContext extends GenerationContext<WorldAccess> {

    private final RegistryEntry<Biome> biome;
    private final int radius;
    private final SafeChunkBounds bounds;

    public FullGenerationContext(WorldAccess world, BlockPos rootPos, Species species, RegistryEntry<Biome> biome, int radius, SafeChunkBounds bounds) {
        super(world, rootPos, species);
        this.biome = biome;
        this.radius = radius;
        this.bounds = bounds;
    }

    public RegistryEntry<Biome> biome() {
        return biome;
    }

    public int radius() {
        return radius;
    }

    public SafeChunkBounds bounds() {
        return bounds;
    }

}
