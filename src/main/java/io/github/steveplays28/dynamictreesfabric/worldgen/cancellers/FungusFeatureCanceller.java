package io.github.steveplays28.dynamictreesfabric.worldgen.cancellers;

import io.github.steveplays28.dynamictreesfabric.api.worldgen.BiomePropertySelectors;
import io.github.steveplays28.dynamictreesfabric.api.worldgen.FeatureCanceller;
import net.minecraftforge.registries.ForgeRegistries;

import net.minecraft.util.Identifier;
import net.minecraft.world.gen.feature.ConfiguredFeature;
import net.minecraft.world.gen.feature.FeatureConfig;

/**
 * This class is an alternate version of {@link TreeFeatureCanceller} specifically made for cancelling fungus features.
 * It cancels any features that have a config that extends the given class.
 *
 * @param <T> An {@link FeatureConfig} which should be cancelled.
 * @author Harley O'Connor
 */
public class FungusFeatureCanceller<T extends FeatureConfig> extends FeatureCanceller {
	private final Class<T> fungusFeatureConfigClass;

	public FungusFeatureCanceller(final Identifier registryName, final Class<T> fungusFeatureConfigClass) {
		super(registryName);
		this.fungusFeatureConfigClass = fungusFeatureConfigClass;
	}

	@Override
	public boolean shouldCancel(ConfiguredFeature<?, ?> configuredFeature, BiomePropertySelectors.FeatureCancellations featureCancellations) {
		final Identifier featureRegistryName = ForgeRegistries.FEATURES.getKey(configuredFeature.feature());

		return featureRegistryName != null && this.fungusFeatureConfigClass.isInstance(configuredFeature.config()) &&
				featureCancellations.shouldCancelNamespace(featureRegistryName.getNamespace());
	}
}
