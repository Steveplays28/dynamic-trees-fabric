package io.github.steveplays28.dynamictreesfabric.worldgen.cancellers;

import java.util.stream.Stream;

import io.github.steveplays28.dynamictreesfabric.api.worldgen.BiomePropertySelectors;
import io.github.steveplays28.dynamictreesfabric.api.worldgen.FeatureCanceller;
import net.minecraftforge.registries.ForgeRegistries;

import net.minecraft.util.Identifier;
import net.minecraft.world.gen.feature.ConfiguredFeature;
import net.minecraft.world.gen.feature.FeatureConfig;
import net.minecraft.world.gen.feature.RandomBooleanFeatureConfig;


public class MushroomFeatureCanceller<T extends FeatureConfig> extends FeatureCanceller {
	private final Class<T> mushroomFeatureConfigClass;

	public MushroomFeatureCanceller(final Identifier registryName, final Class<T> mushroomFeatureConfigClass) {
		super(registryName);
		this.mushroomFeatureConfigClass = mushroomFeatureConfigClass;
	}

	@Override
	public boolean shouldCancel(final ConfiguredFeature<?, ?> configuredFeature, final BiomePropertySelectors.FeatureCancellations featureCancellations) {
		final Identifier featureRegistryName = ForgeRegistries.FEATURES.getKey(configuredFeature.feature());

		if (featureRegistryName == null) {
			return false;
		}

		// Mushrooms come in RandomBooleanFeatureConfiguration to select between brown and red.
		if (!(configuredFeature.config() instanceof RandomBooleanFeatureConfig randomBooleanFeatureConfiguration)) {
			return false;
		}

		return getConfigs(randomBooleanFeatureConfiguration).anyMatch(this.mushroomFeatureConfigClass::isInstance) &&
				featureCancellations.shouldCancelNamespace(featureRegistryName.getNamespace());
	}

	private Stream<FeatureConfig> getConfigs(final RandomBooleanFeatureConfig twoFeatureConfig) {
		return twoFeatureConfig.getDecoratedFeatures().map(ConfiguredFeature::config);
	}
}
