package io.github.steveplays28.dynamictreesfabric.resources;

import io.github.steveplays28.dynamictreesfabric.api.configurations.ConfigurationTemplateResourceLoader;
import io.github.steveplays28.dynamictreesfabric.api.event.Hooks;
import io.github.steveplays28.dynamictreesfabric.api.resource.TreeResourceManager;
import io.github.steveplays28.dynamictreesfabric.data.DTRecipes;
import io.github.steveplays28.dynamictreesfabric.growthlogic.GrowthLogicKit;
import io.github.steveplays28.dynamictreesfabric.growthlogic.GrowthLogicKitConfiguration;
import io.github.steveplays28.dynamictreesfabric.init.DTConfigs;
import io.github.steveplays28.dynamictreesfabric.resources.loader.*;
import io.github.steveplays28.dynamictreesfabric.systems.dropcreators.DropCreator;
import io.github.steveplays28.dynamictreesfabric.systems.dropcreators.DropCreatorConfiguration;
import io.github.steveplays28.dynamictreesfabric.systems.genfeatures.GenFeature;
import io.github.steveplays28.dynamictreesfabric.systems.genfeatures.GenFeatureConfiguration;
import com.google.common.collect.ImmutableMap;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeType;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.ResourceReloader;
import net.minecraft.server.DataPackContents;
import net.minecraft.util.Identifier;
import net.minecraft.util.profiler.Profiler;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.forgespi.language.IModInfo;
import net.minecraftforge.forgespi.locating.IModFile;
import org.apache.logging.log4j.LogManager;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

/**
 *
 * @author Harley O'Connor
 */
@Mod.EventBusSubscriber(modid = io.github.steveplays28.dynamictreesfabric.DynamicTreesFabric.MOD_ID)
public final class Resources {

    public static final Identifier RESOURCE_LOCATION = io.github.steveplays28.dynamictreesfabric.DynamicTreesFabric.resLoc("registry_name");

    public static final String TREES = "trees";

    public static final TreeResourceManager MANAGER = new TreesResourceManager();

    public static final LeavesPropertiesResourceLoader LEAVES_PROPERTIES_LOADER = new LeavesPropertiesResourceLoader();
    public static final SoilPropertiesResourceLoader SOIL_PROPERTIES_LOADER = new SoilPropertiesResourceLoader();
    public static final FamilyResourceLoader FAMILY_LOADER = new FamilyResourceLoader();

    public static final ConfigurationTemplateResourceLoader<DropCreatorConfiguration, DropCreator>
            DROP_CREATOR_TEMPLATE_LOADER = new ConfigurationTemplateResourceLoader<>(
            "drop_creators/configurations",
            DropCreator.REGISTRY,
            DropCreatorConfiguration.TEMPLATES
    );

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

    public static final SpeciesResourceLoader SPECIES_LOADER = new SpeciesResourceLoader();


    public static final JoCodeResourceLoader JO_CODE_LOADER = new JoCodeResourceLoader();
    public static final BiomeDatabaseResourceLoader BIOME_DATABASE_LOADER = new BiomeDatabaseResourceLoader();
    public static final GlobalDropCreatorResourceLoader GLOBAL_DROP_CREATOR_LOADER =
            new GlobalDropCreatorResourceLoader();

    public static void setupTreesResourceManager() {
        addDefaultLoaders();
        Hooks.onAddResourceLoaders(MANAGER);
        MANAGER.registerAppliers();

        registerModTreePacks();
        registerFlatTreePack();

        LogManager.getLogger().debug("Successfully loaded " + MANAGER.streamResourcePacks().count() + " tree packs.");
    }

    private static void addDefaultLoaders() {
        MANAGER.addLoaders(
                LEAVES_PROPERTIES_LOADER,
                SOIL_PROPERTIES_LOADER,
                FAMILY_LOADER,
                DROP_CREATOR_TEMPLATE_LOADER,
                GEN_FEATURE_TEMPLATE_LOADER,
                GROWTH_LOGIC_KIT_TEMPLATE_LOADER,
                SPECIES_LOADER,
                JO_CODE_LOADER,
                BIOME_DATABASE_LOADER,
                GLOBAL_DROP_CREATOR_LOADER
        );
    }

    private static void registerModTreePacks() {
        // Register all mod tree packs. Gets the mods in an ordered list so that add-ons will come after DT.
        // This means that add-ons will take priority over DT.
        ModList.get().getMods().forEach(Resources::addModResourcePack);
    }




    private static void addModResourcePack(IModInfo modInfo) {
        final IModFile modFile = ModList.get().getModFileById(modInfo.getModId()).getFile();
        if (modFile.getProvider().isValid(modFile)) {
            addModResourcePack(modFile);
        }
    }

    private static void addModResourcePack(IModFile modFile) {
        final Path treesPath = modFile.findResource(TREES)
                .toAbsolutePath();

        if (Files.exists(treesPath)) {
            MANAGER.addPack(new ModTreeResourcePack(treesPath, modFile));
        }
    }

    private static void registerFlatTreePack() {
        final File mainTreeFolder = getTreeFolder();
        MANAGER.addPack(new FlatTreeResourcePack(mainTreeFolder.toPath().toAbsolutePath()));
    }

    private static File getTreeFolder() {
        final File mainTreeFolder = new File("trees/");

        // Create the trees folder if it doesn't already exist, crash if failed.
        if (!mainTreeFolder.exists() && !mainTreeFolder.mkdir()) {
            throw new RuntimeException("Failed to create \"trees\" folder in your Minecraft directory.");
        }
        return mainTreeFolder;
    }

    @SubscribeEvent
    public static void addReloadListeners(final AddReloadListenerEvent event) {
        event.addListener(new ReloadListener(event.getServerResources()));
    }

    /**
     * Listens for datapack reloads for actions such as reloading the trees resource manager and registering dirt bucket
     * recipes.
     */
    public static final class ReloadListener implements ResourceReloader {
        private final DataPackContents dataPackRegistries;

        public ReloadListener(DataPackContents dataPackRegistries) {
            this.dataPackRegistries = dataPackRegistries;
        }


        @Override
        public CompletableFuture<Void> reload(Synchronizer stage, ResourceManager resourceManager,
                                              Profiler preparationsProfiler, Profiler reloadProfiler,
                                              Executor backgroundExecutor, Executor gameExecutor) {
            final CompletableFuture<?>[] futures = MANAGER.prepareReload(gameExecutor, backgroundExecutor);

            // Reload all reload listeners in the trees resource manager and registers dirt bucket recipes.
            return CompletableFuture.allOf(futures)
                    .thenCompose(stage::whenPrepared)
                    .thenAcceptAsync(v -> MANAGER.reload(futures), gameExecutor)
                    .thenRunAsync(this::registerDirtBucketRecipes, gameExecutor);
        }

        private void registerDirtBucketRecipes() {
            if (!DTConfigs.GENERATE_DIRT_BUCKET_RECIPES.get()) {
                return;
            }

            final Map<RecipeType<?>, Map<Identifier, Recipe<?>>> recipes = new HashMap<>();

            // Put the recipes into the new map and make each type's recipes mutable.
            this.dataPackRegistries.getRecipeManager().recipes.forEach(((recipeType, currentRecipes) ->
                    recipes.put(recipeType, new HashMap<>(currentRecipes))));

            // Register dirt bucket recipes.
            DTRecipes.registerDirtBucketRecipes(recipes.get(RecipeType.CRAFTING));

            // Revert each type's recipes back to immutable.
            recipes.forEach(
                    ((recipeType, currentRecipes) -> recipes.put(recipeType, ImmutableMap.copyOf(currentRecipes))));

            // Set the new recipes.
            dataPackRegistries.getRecipeManager().recipes = ImmutableMap.copyOf(recipes);
        }
    }

}
