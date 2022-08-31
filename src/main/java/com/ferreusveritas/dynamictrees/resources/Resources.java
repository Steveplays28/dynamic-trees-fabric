package com.ferreusveritas.dynamictrees.resources;

import com.ferreusveritas.dynamictrees.DynamicTrees;
import com.ferreusveritas.dynamictrees.api.configurations.ConfigurationTemplateResourceLoader;
import com.ferreusveritas.dynamictrees.api.event.Hooks;
import com.ferreusveritas.dynamictrees.api.resource.ResourceManager;
import com.ferreusveritas.dynamictrees.data.DTRecipes;
import com.ferreusveritas.dynamictrees.growthlogic.GrowthLogicKit;
import com.ferreusveritas.dynamictrees.growthlogic.GrowthLogicKitConfiguration;
import com.ferreusveritas.dynamictrees.init.DTConfigs;
import com.ferreusveritas.dynamictrees.resources.loader.*;
import com.ferreusveritas.dynamictrees.systems.fruit.FruitResourceLoader;
import com.ferreusveritas.dynamictrees.systems.genfeatures.GenFeature;
import com.ferreusveritas.dynamictrees.systems.genfeatures.GenFeatureConfiguration;
import com.ferreusveritas.dynamictrees.systems.pod.PodResourceLoader;
import com.google.common.collect.ImmutableMap;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.IRecipeType;
import net.minecraft.profiler.IProfiler;
import net.minecraft.resources.DataPackRegistries;
import net.minecraft.resources.IFutureReloadListener;
import net.minecraft.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.apache.logging.log4j.LogManager;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

/**
 * Holds and registers data pack entries ({@link IFutureReloadListener} objects).
 *
 * @author Harley O'Connor
 */
@Mod.EventBusSubscriber(modid = DynamicTrees.MOD_ID)
public final class Resources {

    public static final ResourceLocation RESOURCE_LOCATION = DynamicTrees.resLoc("registry_name");

    public static final String TREES = "trees";

    public static final ResourceManager MANAGER = new TreesResourceManager();

    public static final LeavesPropertiesResourceLoader LEAVES_PROPERTIES_LOADER = new LeavesPropertiesResourceLoader();
    public static final SoilPropertiesResourceLoader SOIL_PROPERTIES_LOADER = new SoilPropertiesResourceLoader();
    public static final FamilyResourceLoader FAMILY_LOADER = new FamilyResourceLoader();

    public static final ConfigurationTemplateResourceLoader<GenFeatureConfiguration, GenFeature>
            GEN_FEATURE_TEMPLATE_LOADER = new ConfigurationTemplateResourceLoader<>(
            "gen_features/configurations",
            GenFeature.REGISTRY,
            GenFeatureConfiguration.TEMPLATES
    );

    public static final ConfigurationTemplateResourceLoader<GrowthLogicKitConfiguration, GrowthLogicKit>
            GROWTH_LOGIC_KIT_TEMPLATE_LOADER = new ConfigurationTemplateResourceLoader<>(
            "growth_logic_kits/configurations",
            GrowthLogicKit.REGISTRY,
            GrowthLogicKitConfiguration.TEMPLATES
    );

    public static final FruitResourceLoader FRUIT_LOADER = new FruitResourceLoader();
    public static final PodResourceLoader POD_LOADER = new PodResourceLoader();
    public static final SpeciesResourceLoader SPECIES_LOADER = new SpeciesResourceLoader();


    public static final JoCodeResourceLoader JO_CODE_LOADER = new JoCodeResourceLoader();
    public static final BiomeDatabaseResourceLoader BIOME_DATABASE_LOADER = new BiomeDatabaseResourceLoader();

    public static void setupTreesResourceManager() {
        addDefaultLoaders();
        Hooks.onAddResourceLoaders(MANAGER);
        MANAGER.registerAppliers();

        LogManager.getLogger().debug("Successfully loaded " + MANAGER.listPacks().count() + " tree packs.");
    }

    private static void addDefaultLoaders() {
        MANAGER.addLoaders(
                LEAVES_PROPERTIES_LOADER,
                SOIL_PROPERTIES_LOADER,
                FAMILY_LOADER,
                GEN_FEATURE_TEMPLATE_LOADER,
                GROWTH_LOGIC_KIT_TEMPLATE_LOADER,
                FRUIT_LOADER,
                POD_LOADER,
                SPECIES_LOADER,
                JO_CODE_LOADER,
                BIOME_DATABASE_LOADER
        );
    }

    @SubscribeEvent
    public static void addReloadListeners(final AddReloadListenerEvent event) {
        event.addListener(new ReloadListener(event.getDataPackRegistries()));
    }

    /**
     * Listens for datapack reloads for actions such as reloading the trees resource manager and registering dirt bucket
     * recipes.
     */
    public static final class ReloadListener implements IFutureReloadListener {
        private final DataPackRegistries dataPackRegistries;

        public ReloadListener(DataPackRegistries dataPackRegistries) {
            this.dataPackRegistries = dataPackRegistries;
        }

        @Override
        public CompletableFuture<Void> reload(IStage stage, IResourceManager resourceManager,
                                              IProfiler preparationsProfiler, IProfiler reloadProfiler,
                                              Executor backgroundExecutor, Executor gameExecutor) {
            final CompletableFuture<?>[] futures = MANAGER.prepareReload(gameExecutor, backgroundExecutor);

            // Reload all reload listeners in the trees resource manager and registers dirt bucket recipes.
            return CompletableFuture.allOf(futures)
                    .thenCompose(stage::wait)
                    .thenAcceptAsync(v -> MANAGER.reload(futures), gameExecutor)
                    .thenRunAsync(this::registerDirtBucketRecipes, gameExecutor);
        }

        private void registerDirtBucketRecipes() {
            if (!DTConfigs.GENERATE_DIRT_BUCKET_RECIPES.get()) {
                return;
            }

            final Map<IRecipeType<?>, Map<ResourceLocation, IRecipe<?>>> recipes = new HashMap<>();

            // Put the recipes into the new map and make each type's recipes mutable.
            this.dataPackRegistries.getRecipeManager().recipes.forEach(((recipeType, currentRecipes) ->
                    recipes.put(recipeType, new HashMap<>(currentRecipes))));

            // Register dirt bucket recipes.
            DTRecipes.registerDirtBucketRecipes(recipes.get(IRecipeType.CRAFTING));

            // Revert each type's recipes back to immutable.
            recipes.forEach(
                    ((recipeType, currentRecipes) -> recipes.put(recipeType, ImmutableMap.copyOf(currentRecipes))));

            // Set the new recipes.
            dataPackRegistries.getRecipeManager().recipes = ImmutableMap.copyOf(recipes);
        }
    }

}
