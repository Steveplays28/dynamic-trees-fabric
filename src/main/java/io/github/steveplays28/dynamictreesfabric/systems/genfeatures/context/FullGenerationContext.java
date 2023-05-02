package io.github.steveplays28.dynamictreesfabric.systems.genfeatures.context;

import io.github.steveplays28.dynamictreesfabric.trees.Species;
import io.github.steveplays28.dynamictreesfabric.util.SafeChunkBounds;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.biome.Biome;

/**
 * @author Harley O'Connor
 */
public final class FullGenerationContext extends GenerationContext<LevelAccessor> {

    private final Holder<Biome> biome;
    private final int radius;
    private final SafeChunkBounds bounds;

    public FullGenerationContext(LevelAccessor world, BlockPos rootPos, Species species, Holder<Biome> biome, int radius, SafeChunkBounds bounds) {
        super(world, rootPos, species);
        this.biome = biome;
        this.radius = radius;
        this.bounds = bounds;
    }

    public Holder<Biome> biome() {
        return biome;
    }

    public int radius() {
        return radius;
    }

    public SafeChunkBounds bounds() {
        return bounds;
    }

}
