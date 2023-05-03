package io.github.steveplays28.dynamictreesfabric;

import io.github.steveplays28.dynamictreesfabric.api.GatherDataHelper;
import io.github.steveplays28.dynamictreesfabric.api.registry.RegistryHandler;
import io.github.steveplays28.dynamictreesfabric.blocks.leaves.LeavesProperties;
import io.github.steveplays28.dynamictreesfabric.blocks.rootyblocks.SoilProperties;
import io.github.steveplays28.dynamictreesfabric.command.DTArgumentTypes;
import io.github.steveplays28.dynamictreesfabric.event.handlers.EventHandlers;
import io.github.steveplays28.dynamictreesfabric.init.DTClient;
import io.github.steveplays28.dynamictreesfabric.init.DTConfigs;
import io.github.steveplays28.dynamictreesfabric.init.DTRegistries;
import io.github.steveplays28.dynamictreesfabric.init.DTTrees;
import io.github.steveplays28.dynamictreesfabric.resources.Resources;
import io.github.steveplays28.dynamictreesfabric.trees.Family;
import io.github.steveplays28.dynamictreesfabric.trees.Species;
import io.github.steveplays28.dynamictreesfabric.util.CommonSetup;
import io.github.steveplays28.dynamictreesfabric.worldgen.TreeGenerator;
import net.minecraftforge.data.event.GatherDataEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

import net.minecraft.util.Identifier;

import net.fabricmc.api.ModInitializer;

public final class DynamicTreesFabric implements ModInitializer {

	public static final String MOD_ID = "dynamic-trees-fabric";
	public static final String NAME = "Dynamic Trees Fabric";

	public static final String MINECRAFT = "minecraft";
	public static final String FABRIC_API = "fabric";

	@Override
	public void onInitialize() {
		final IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
		final ModLoadingContext loadingContext = ModLoadingContext.get();

		loadingContext.registerConfig(ModConfig.Type.SERVER, DTConfigs.SERVER_CONFIG);
		loadingContext.registerConfig(ModConfig.Type.COMMON, DTConfigs.COMMON_CONFIG);
		loadingContext.registerConfig(ModConfig.Type.CLIENT, DTConfigs.CLIENT_CONFIG);

//        DistExecutor.safeRunWhenOn(Dist.CLIENT, () -> DTClient::clientStart);

		TreeGenerator.setup();

		RegistryHandler.setup(MOD_ID);

		DTRegistries.setup();

		modEventBus.addListener(this::clientSetup);
		modEventBus.addListener(this::onCommonSetup);
		modEventBus.addListener(this::gatherData);
		modEventBus.addListener(CommonSetup::onCommonSetup);

		EventHandlers.registerCommon();
		DTArgumentTypes.ARGUMENT_TYPES.register(modEventBus);
	}

	public static Identifier resLoc(final String path) {
		return new Identifier(MOD_ID, path);
	}

	private void clientSetup(final FMLClientSetupEvent event) {
		DTClient.setup();
	}

	private void onCommonSetup(final FMLCommonSetupEvent event) {
		// Clears and locks registry handlers to free them from memory.
		RegistryHandler.REGISTRY.clear();

		DTRegistries.DENDRO_POTION.get().registerRecipes();

		Resources.MANAGER.setup();

		if (DTConfigs.REPLACE_NYLIUM_FUNGI.get()) {
			DTTrees.replaceNyliumFungiFeatures();
		}
	}

	private void gatherData(final GatherDataEvent event) {
		Resources.MANAGER.gatherData();
		GatherDataHelper.gatherAllData(MOD_ID, event, SoilProperties.REGISTRY, Family.REGISTRY, Species.REGISTRY, LeavesProperties.REGISTRY);
	}

	public enum AxeDamage {
		VANILLA, THICKNESS, VOLUME
	}

	public enum DestroyMode {
		IGNORE, SLOPPY, SET_RADIUS, HARVEST, ROT, OVERFLOW
	}

	public enum SwampOakWaterState {
		ROOTED, SUNK, DISABLED
	}

}
