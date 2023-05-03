package io.github.steveplays28.dynamictreesfabric.worldgen;

import io.github.steveplays28.dynamictreesfabric.util.SafeChunkBounds;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.gen.feature.DefaultFeatureConfig;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.util.FeatureContext;

/**
 * @author Harley O'Connor
 */
public final class DynamicTreeFeature extends Feature<DefaultFeatureConfig> {

    public DynamicTreeFeature() {
        super(DefaultFeatureConfig.CODEC);
    }

    @Override
    public boolean generate(FeatureContext<DefaultFeatureConfig> pContext) {
        // final long startTime = System.nanoTime();
        final TreeGenerator treeGenerator = TreeGenerator.getTreeGenerator();
        final ServerWorld serverWorld = pContext.getWorld().toServerWorld();
        final Identifier dimensionLocation = serverWorld.getRegistryKey().getValue();

        // Do not generate if the current dimension is blacklisted.
        if (BiomeDatabases.isBlacklisted(dimensionLocation)) {
            return false;
        }

        // Grab biome data base for dimension.
        final BiomeDatabase biomeDatabase = BiomeDatabases.getDimensionalOrDefault(dimensionLocation);

        // Get chunk pos and create safe bounds, which ensure we do not try to generate in an unloaded chunk.
        final ChunkPos chunkPos = pContext.getWorld().getChunk(pContext.getOrigin()).getPos();
        final SafeChunkBounds chunkBounds = new SafeChunkBounds(pContext.getWorld(), chunkPos);

        // Generate trees.
        treeGenerator.getCircleProvider().getPoissonDiscs(serverWorld, pContext.getWorld(), chunkPos)
                .forEach(c -> treeGenerator.makeTrees(pContext.getWorld(), biomeDatabase, c, chunkBounds));

        // final long endTime = System.nanoTime();
        // final long duration = (endTime - startTime) / 1000000;
        // LogManager.getLogger().debug("Dynamic trees at chunk " + chunkPos + " took " + duration + " ms to generate.");
        return true;
    }

}
