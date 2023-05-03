package io.github.steveplays28.dynamictreesfabric.api.worldgen;

import io.github.steveplays28.dynamictreesfabric.api.registry.RegistryEntry;
import io.github.steveplays28.dynamictreesfabric.api.registry.SimpleRegistry;
import io.github.steveplays28.dynamictreesfabric.init.DTTrees;
import net.minecraft.util.Identifier;
import net.minecraft.world.gen.feature.ConfiguredFeature;

/**
 * @author Harley O'Connor
 */
public abstract class FeatureCanceller extends RegistryEntry<FeatureCanceller> {

    public static final FeatureCanceller NULL_CANCELLER = new FeatureCanceller(DTTrees.NULL) {
        @Override
        public boolean shouldCancel(ConfiguredFeature<?, ?> configuredFeature, BiomePropertySelectors.FeatureCancellations featureCancellations) {
            return false;
        }
    };

    public static final SimpleRegistry<FeatureCanceller> REGISTRY = new SimpleRegistry<>(FeatureCanceller.class, NULL_CANCELLER);

    public FeatureCanceller(final Identifier registryName) {
        super(registryName);
    }

    /**
     * Works out if the configured feature in the given biome should be cancelled or not.
     *
     * @param configuredFeature    The configured feature.
     * @param featureCancellations The tree canceller object.
     * @return True if feature should be cancelled, false if not.
     */
    public abstract boolean shouldCancel(ConfiguredFeature<?, ?> configuredFeature, BiomePropertySelectors.FeatureCancellations featureCancellations);

}
