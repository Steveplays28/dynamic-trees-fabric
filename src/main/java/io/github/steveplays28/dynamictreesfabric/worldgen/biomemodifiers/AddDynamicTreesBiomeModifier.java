package io.github.steveplays28.dynamictreesfabric.worldgen.biomemodifiers;

import com.mojang.serialization.Codec;
import io.github.steveplays28.dynamictreesfabric.init.DTConfigs;
import io.github.steveplays28.dynamictreesfabric.init.DTRegistries;
import net.minecraftforge.common.world.BiomeGenerationSettingsBuilder;
import net.minecraftforge.common.world.BiomeModifier;
import net.minecraftforge.common.world.ModifiableBiomeInfo;

import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.GenerationStep;

public class AddDynamicTreesBiomeModifier implements BiomeModifier {
	@Override
	public void modify(RegistryEntry<Biome> biome, Phase phase, ModifiableBiomeInfo.BiomeInfo.Builder builder) {
		if (phase == Phase.ADD && DTConfigs.WORLD_GEN.get()) {
			BiomeGenerationSettingsBuilder generationSettings = builder.getGenerationSettings();
			generationSettings.addFeature(GenerationStep.Feature.VEGETAL_DECORATION, DTRegistries.DYNAMIC_TREE_PLACED_FEATURE.getHolder().orElseThrow());
		}
	}

	@Override
	public Codec<? extends BiomeModifier> codec() {
		return DTRegistries.ADD_DYNAMIC_TREES_BIOME_MODIFIER.get();
	}
}
