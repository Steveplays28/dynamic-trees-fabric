package io.github.steveplays28.dynamictreesfabric.init;

import java.util.List;
import java.util.stream.Collectors;

import io.github.steveplays28.dynamictreesfabric.api.TreeRegistry;
import io.github.steveplays28.dynamictreesfabric.api.registry.Registries;
import io.github.steveplays28.dynamictreesfabric.api.registry.Registry;
import io.github.steveplays28.dynamictreesfabric.api.registry.SimpleRegistry;
import io.github.steveplays28.dynamictreesfabric.api.registry.TypeRegistryEvent;
import io.github.steveplays28.dynamictreesfabric.api.worldgen.FeatureCanceller;
import io.github.steveplays28.dynamictreesfabric.blocks.leaves.LeavesProperties;
import io.github.steveplays28.dynamictreesfabric.blocks.leaves.PalmLeavesProperties;
import io.github.steveplays28.dynamictreesfabric.blocks.leaves.SolidLeavesProperties;
import io.github.steveplays28.dynamictreesfabric.blocks.leaves.WartProperties;
import io.github.steveplays28.dynamictreesfabric.blocks.rootyblocks.SoilProperties;
import io.github.steveplays28.dynamictreesfabric.blocks.rootyblocks.SpreadableSoilProperties;
import io.github.steveplays28.dynamictreesfabric.blocks.rootyblocks.WaterSoilProperties;
import io.github.steveplays28.dynamictreesfabric.deserialisation.JsonDeserialisers;
import io.github.steveplays28.dynamictreesfabric.resources.Resources;
import io.github.steveplays28.dynamictreesfabric.trees.Family;
import io.github.steveplays28.dynamictreesfabric.trees.Mushroom;
import io.github.steveplays28.dynamictreesfabric.trees.Species;
import io.github.steveplays28.dynamictreesfabric.trees.families.NetherFungusFamily;
import io.github.steveplays28.dynamictreesfabric.trees.species.NetherFungusSpecies;
import io.github.steveplays28.dynamictreesfabric.trees.species.PalmSpecies;
import io.github.steveplays28.dynamictreesfabric.trees.species.SwampOakSpecies;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.NewRegistryEvent;
import net.minecraftforge.registries.RegisterEvent;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.util.Identifier;
import net.minecraft.util.collection.Weighted;
import net.minecraft.world.gen.feature.NetherConfiguredFeatures;
import net.minecraft.world.gen.feature.NetherForestVegetationFeatureConfig;
import net.minecraft.world.gen.stateprovider.WeightedBlockStateProvider;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class DTTrees {

	public static final Identifier NULL = io.github.steveplays28.dynamictreesfabric.DynamicTreesFabric.resLoc("null");

	public static final Identifier OAK = io.github.steveplays28.dynamictreesfabric.DynamicTreesFabric.resLoc("oak");
	public static final Identifier BIRCH = io.github.steveplays28.dynamictreesfabric.DynamicTreesFabric.resLoc("birch");
	public static final Identifier SPRUCE = io.github.steveplays28.dynamictreesfabric.DynamicTreesFabric.resLoc("spruce");
	public static final Identifier JUNGLE = io.github.steveplays28.dynamictreesfabric.DynamicTreesFabric.resLoc("jungle");
	public static final Identifier DARK_OAK = io.github.steveplays28.dynamictreesfabric.DynamicTreesFabric.resLoc("dark_oak");
	public static final Identifier ACACIA = io.github.steveplays28.dynamictreesfabric.DynamicTreesFabric.resLoc("acacia");
	public static final Identifier CRIMSON = io.github.steveplays28.dynamictreesfabric.DynamicTreesFabric.resLoc("crimson");
	public static final Identifier WARPED = io.github.steveplays28.dynamictreesfabric.DynamicTreesFabric.resLoc("warped");

	@SubscribeEvent
	public static void registerSpecies(final io.github.steveplays28.dynamictreesfabric.api.registry.RegistryEvent<Species> event) {
		// Registers fake species for generating mushrooms.
		event.getRegistry().registerAll(new Mushroom(true), new Mushroom(false));
	}

	@SubscribeEvent
	public static void registerSoilProperties(final io.github.steveplays28.dynamictreesfabric.api.registry.RegistryEvent<SoilProperties> event) {
		event.getRegistry().registerAll(
				//SoilHelper.registerSoil(DynamicTrees.resLoc("dirt"),Blocks.DIRT, SoilHelper.DIRT_LIKE, ),//new SpreadableSoilProperties.SpreadableRootyBlock(Blocks.DIRT, 9, Blocks.GRASS_BLOCK, Blocks.MYCELIUM)
				//SoilHelper.registerSoil(DynamicTrees.resLoc("netherrack"),Blocks.NETHERRACK, SoilHelper.NETHER_LIKE, new SpreadableSoilProperties.SpreadableRootyBlock(Blocks.NETHERRACK, Items.BONE_MEAL, Blocks.CRIMSON_NYLIUM, Blocks.WARPED_NYLIUM))
		);
	}

	@SubscribeEvent
	public static void registerLeavesPropertiesTypes(final TypeRegistryEvent<LeavesProperties> event) {
		event.registerType(io.github.steveplays28.dynamictreesfabric.DynamicTreesFabric.resLoc("solid"), SolidLeavesProperties.TYPE);
		event.registerType(io.github.steveplays28.dynamictreesfabric.DynamicTreesFabric.resLoc("wart"), WartProperties.TYPE);
		event.registerType(io.github.steveplays28.dynamictreesfabric.DynamicTreesFabric.resLoc("palm"), PalmLeavesProperties.TYPE);
	}

	@SubscribeEvent
	public static void registerFamilyTypes(final TypeRegistryEvent<Family> event) {
		event.registerType(io.github.steveplays28.dynamictreesfabric.DynamicTreesFabric.resLoc("nether_fungus"), NetherFungusFamily.TYPE);
	}

	@SubscribeEvent
	public static void registerSpeciesTypes(final TypeRegistryEvent<Species> event) {
		event.registerType(io.github.steveplays28.dynamictreesfabric.DynamicTreesFabric.resLoc("nether_fungus"), NetherFungusSpecies.TYPE);
		event.registerType(io.github.steveplays28.dynamictreesfabric.DynamicTreesFabric.resLoc("swamp_oak"), SwampOakSpecies.TYPE);
		event.registerType(io.github.steveplays28.dynamictreesfabric.DynamicTreesFabric.resLoc("palm"), PalmSpecies.TYPE);
	}

	@SubscribeEvent
	public static void registerSoilPropertiesTypes(final TypeRegistryEvent<SoilProperties> event) {
		event.registerType(io.github.steveplays28.dynamictreesfabric.DynamicTreesFabric.resLoc("water"), WaterSoilProperties.TYPE);
		event.registerType(io.github.steveplays28.dynamictreesfabric.DynamicTreesFabric.resLoc("spreadable"), SpreadableSoilProperties.TYPE);
	}

	@SubscribeEvent
	public static void newRegistry(NewRegistryEvent event) {
		final List<SimpleRegistry<?>> registries = Registries.REGISTRIES.stream()
				.filter(registry -> registry instanceof SimpleRegistry)
				.map(registry -> (SimpleRegistry<?>) registry)
				.collect(Collectors.toList());

		// Post registry events.
		registries.forEach(SimpleRegistry::postRegistryEvent);

		Resources.setupTreesResourceManager();

		// Register Forge registry entry getters and add-on Json object getters.
		JsonDeserialisers.registerForgeEntryGetters();
		JsonDeserialisers.postRegistryEvent();

		// Register feature cancellers.
		FeatureCanceller.REGISTRY.postRegistryEvent();
		FeatureCanceller.REGISTRY.lock();
	}

	@SubscribeEvent
	public static void onRegisterBlocks(RegisterEvent event) {
		event.register(ForgeRegistries.Keys.BLOCKS, registerHelper -> {
			// Register any registry entries from Json files.
			Resources.MANAGER.load();

			// Lock all the registries.
			Registries.REGISTRIES.stream()
					.filter(registry -> registry instanceof SimpleRegistry)
					.forEach(Registry::lock);
		});
	}

	public static void replaceNyliumFungiFeatures() {
		TreeRegistry.findSpecies(CRIMSON).getSapling().ifPresent(crimsonSapling ->
				TreeRegistry.findSpecies(WARPED).getSapling().ifPresent(warpedSapling -> {
					replaceFeatureConfigs(((WeightedBlockStateProvider) new NetherForestVegetationFeatureConfig(NetherConfiguredFeatures.CRIMSON_VEGETATION_PROVIDER, 8, 4).stateProvider), crimsonSapling, warpedSapling);
					replaceFeatureConfigs(((WeightedBlockStateProvider) new NetherForestVegetationFeatureConfig(NetherConfiguredFeatures.WARPED_VEGETATION_PROVIDER, 8, 4).stateProvider), crimsonSapling, warpedSapling);
				})
		);
	}

	private static void replaceFeatureConfigs(WeightedBlockStateProvider featureConfig, Block crimsonSapling, Block warpedSapling) {
		for (final Weighted.Present<BlockState> entry : featureConfig.weightedList.items) {
			if (entry.getData().getBlock() == Blocks.CRIMSON_FUNGUS) {
				entry.data = crimsonSapling.getDefaultState();
			}
			if (entry.data.getBlock() == Blocks.WARPED_FUNGUS) {
				entry.data = warpedSapling.getDefaultState();
			}
		}
	}

}
