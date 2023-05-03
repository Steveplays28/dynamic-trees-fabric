package io.github.steveplays28.dynamictreesfabric.worldgen.biomemodifiers;

import com.mojang.serialization.Codec;
import io.github.steveplays28.dynamictreesfabric.api.worldgen.BiomePropertySelectors;
import io.github.steveplays28.dynamictreesfabric.api.worldgen.FeatureCanceller;
import io.github.steveplays28.dynamictreesfabric.init.DTConfigs;
import io.github.steveplays28.dynamictreesfabric.init.DTRegistries;
import io.github.steveplays28.dynamictreesfabric.worldgen.BiomeDatabase;
import io.github.steveplays28.dynamictreesfabric.worldgen.FeatureCancellationRegistry;
import net.minecraftforge.common.world.BiomeGenerationSettingsBuilder;
import net.minecraftforge.common.world.BiomeModifier;
import net.minecraftforge.common.world.ModifiableBiomeInfo;

import net.minecraft.registry.Registry;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.feature.PlacedFeature;

public class RunFeatureCancellersBiomeModifier implements BiomeModifier {
	public static final TagKey<PlacedFeature> FEATURE_CANCELLER_EXCLUSIONS_KEY = TagKey.of(Registry.PLACED_FEATURE_REGISTRY,
			new Identifier(io.github.steveplays28.dynamictreesfabric.DynamicTreesFabric.MOD_ID, "feature_canceller_exclusions"));

	@Override
	public void modify(RegistryEntry<Biome> biome, Phase phase, ModifiableBiomeInfo.BiomeInfo.Builder builder) {
		if (phase == Phase.REMOVE && DTConfigs.WORLD_GEN.get()) {
			BiomeGenerationSettingsBuilder generationSettings = builder.getGenerationSettings();

			BiomePropertySelectors.FeatureCancellations featureCancellations = new BiomePropertySelectors.FeatureCancellations();

			for (FeatureCancellationRegistry.Entry entry : FeatureCancellationRegistry.getCancellations()) {
				if (entry.biomes().contains(biome)) {
					if (entry.operation() == BiomeDatabase.Operation.REPLACE)
						featureCancellations.reset();
					featureCancellations.addAllFrom(entry.cancellations());
				}
			}

			// final ResourceLocation biomeName = biome.unwrapKey().map(ResourceKey::location).orElse(null);
			//
			// if (biomeName == null) {
			//     return;
			// }
			//
			// final BiomePropertySelectors.FeatureCancellations featureCancellations = BiomeDatabases.getDefault().getEntry(biomeName).getFeatureCancellations();

			featureCancellations.getStages().forEach(stage -> generationSettings.getFeatures(stage).removeIf(placedFeatureHolder -> {
				// If you want a placed feature to be entirely excluded from cancellation by any feature cancellers,
				// add it to the dynamictrees:tags/worldgen/placed_feature/feature_canceller_exclusions tag.
				if (placedFeatureHolder.is(FEATURE_CANCELLER_EXCLUSIONS_KEY))
					return false;

				PlacedFeature placedFeature = placedFeatureHolder.value();

				return placedFeature.getFeatures().anyMatch(configuredFeature -> {
					for (FeatureCanceller featureCanceller : featureCancellations.getFeatureCancellers()) {
						if (featureCanceller.shouldCancel(configuredFeature, featureCancellations)) {
							return true;
						}
					}

					return false;
				});
			}));
		}
	}

	@Override
	public Codec<? extends BiomeModifier> codec() {
		return DTRegistries.RUN_FEATURE_CANCELLERS_BIOME_MODIFIER.get();
	}
}
