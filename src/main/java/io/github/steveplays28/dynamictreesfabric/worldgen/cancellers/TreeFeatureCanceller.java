package io.github.steveplays28.dynamictreesfabric.worldgen.cancellers;

import io.github.steveplays28.dynamictreesfabric.api.worldgen.BiomePropertySelectors;
import io.github.steveplays28.dynamictreesfabric.api.worldgen.FeatureCanceller;
import io.github.steveplays28.dynamictreesfabric.init.DTRegistries;
import net.minecraftforge.registries.ForgeRegistries;

import net.minecraft.util.Identifier;
import net.minecraft.world.gen.feature.ConfiguredFeature;
import net.minecraft.world.gen.feature.FeatureConfig;
import net.minecraft.world.gen.feature.PlacedFeature;
import net.minecraft.world.gen.feature.RandomFeatureConfig;
import net.minecraft.world.gen.feature.RandomFeatureEntry;
import net.minecraft.world.gen.feature.TreeFeature;
import net.minecraft.world.gen.feature.TreeFeatureConfig;


public class TreeFeatureCanceller<T extends FeatureConfig> extends FeatureCanceller {

	private final Class<T> treeFeatureConfigClass;

	public TreeFeatureCanceller(final Identifier registryName, Class<T> treeFeatureConfigClass) {
		super(registryName);
		this.treeFeatureConfigClass = treeFeatureConfigClass;
	}

	@Override
	public boolean shouldCancel(ConfiguredFeature<?, ?> configuredFeature, BiomePropertySelectors.FeatureCancellations featureCancellations) {
		final FeatureConfig featureConfig = configuredFeature.config();

        /*  The following code removes vanilla trees from the biome's generator.
            There may be some problems as MultipleRandomFeatures can store other features too,
            so these are currently removed from world gen too. The list is immutable so they can't be removed individually,
            but one (unclean) solution may be to add the non-tree features back to the generator. */

		if (featureConfig instanceof RandomFeatureConfig) {
			// Removes configuredFeature if it contains trees.
			return this.doesContainTrees((RandomFeatureConfig) featureConfig, featureCancellations);
		} else if (featureConfig instanceof TreeFeatureConfig) {
			String nameSpace = "";
			final ConfiguredFeature<?, ?> nextConfiguredFeature = configuredFeature.getDecoratedFeatures().findFirst().get();
			final FeatureConfig nextFeatureConfig = nextConfiguredFeature.config();
			final Identifier featureRegistryName = ForgeRegistries.FEATURES.getKey(nextConfiguredFeature.feature());
			if (featureRegistryName != null) {
				nameSpace = featureRegistryName.getNamespace();
			}
			if (this.treeFeatureConfigClass.isInstance(nextFeatureConfig) && !nameSpace.equals("") &&
					featureCancellations.shouldCancelNamespace(nameSpace)) {
				return true; // Removes any individual trees.
			} else if (nextFeatureConfig instanceof RandomFeatureConfig) {
				// Removes configuredFeature if it contains trees.
				return this.doesContainTrees((RandomFeatureConfig) nextFeatureConfig, featureCancellations);
			}
		}
		if (configuredFeature == DTRegistries.DYNAMIC_TREE_CONFIGURED_FEATURE.get()) {
			return false;
		}

		return configuredFeature.getDecoratedFeatures().filter(abc -> abc.feature() instanceof TreeFeature).count() > 0;
	}


	private boolean doesContainTrees(RandomFeatureConfig featureConfig, BiomePropertySelectors.FeatureCancellations featureCancellations) {
		for (RandomFeatureEntry feature : featureConfig.features) {
			final PlacedFeature currentConfiguredFeature = feature.feature.value();
			final Identifier featureRegistryName = ForgeRegistries.FEATURES.getKey(currentConfiguredFeature.getDecoratedFeatures().findFirst().get().feature());

			if (this.treeFeatureConfigClass.isInstance(currentConfiguredFeature.placementModifiers()) && featureRegistryName != null &&
					featureCancellations.shouldCancelNamespace(featureRegistryName.getNamespace())) {
				return true;
			}
		}
		return false;
	}

}
