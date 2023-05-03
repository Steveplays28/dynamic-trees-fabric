package io.github.steveplays28.dynamictreesfabric.api;

import io.github.steveplays28.dynamictreesfabric.api.cells.CellKit;
import io.github.steveplays28.dynamictreesfabric.api.registry.SimpleRegistry;
import io.github.steveplays28.dynamictreesfabric.growthlogic.GrowthLogicKit;
import io.github.steveplays28.dynamictreesfabric.init.DTTrees;
import io.github.steveplays28.dynamictreesfabric.items.DendroPotion;
import io.github.steveplays28.dynamictreesfabric.items.Seed;
import io.github.steveplays28.dynamictreesfabric.systems.dropcreators.DropCreatorConfiguration;
import io.github.steveplays28.dynamictreesfabric.systems.dropcreators.GlobalDropCreators;
import io.github.steveplays28.dynamictreesfabric.trees.Species;
import java.util.*;
import java.util.stream.Collectors;
import net.minecraft.block.BlockState;
import net.minecraft.util.Identifier;

/**
 * Contains various utility functions relating to {@link Object}s with a {@link SimpleRegistry}.
 *
 * @author ferreusveritas
 */
public final class TreeRegistry {

    private TreeRegistry() {
    }

    //////////////////////////////
    // SPECIES REGISTRY
    //////////////////////////////

    public static Species findSpecies(final String name) {
        return findSpecies(getResLoc(name));
    }

    public static Species findSpecies(final Identifier name) {
        return Species.REGISTRY.get(name);
    }

    /**
     * Searches first for the full tree name.  If that fails then it will find the first tree matching the simple name
     * and return it instead otherwise null
     *
     * @param name The name of the tree.  Either the simple name or the full name
     * @return The tree that was found or null if not found
     */
    public static Species findSpeciesSloppy(final String name) {
        final Identifier resourceLocation = getResLoc(name);

        // Search specific domain first.
        if (Species.REGISTRY.has(resourceLocation)) {
            return findSpecies(resourceLocation);
        }

        // Search all domains.
        for (Species species : Species.REGISTRY) {
            if (species.getRegistryName().getPath().equals(resourceLocation.getPath())) {
                return species;
            }
        }

        return Species.NULL_SPECIES;
    }

    /**
     * Returns a new {@link ArrayList<ResourceLocation>} from the {@link Species#REGISTRY} values.
     *
     * @return A new {@link List} from the {@link Species#REGISTRY}.
     */
    public static List<Identifier> getSpeciesDirectory() {
        return new ArrayList<>(Species.REGISTRY.getRegistryNames());
    }

    /**
     * Returns all {@link Species} registry names for which the {@link Species} if marked {@code transformable}.
     *
     * @return A {@link List<ResourceLocation>} for which their {@link Species} can be transformed to other {@link
     * Species}.
     */
    public static List<Identifier> getTransformableSpeciesLocations() {
        return Species.REGISTRY.getRegistryNames().stream().filter(resLoc ->
                findSpecies(resLoc).isTransformable()).collect(Collectors.toList());
    }

    /**
     * Returns all {@link Species} which are marked {@code transformable}.
     *
     * @return A {@link List<Species>} which can be transformed to other {@link Species}.
     */
    public static List<Species> getTransformableSpecies() {
        return getTransformableSpeciesLocations().stream().map(TreeRegistry::findSpecies).collect(Collectors.toList());
    }

    /**
     * Returns a {@link List} of all transformable {@link Species} which can be transformed by a {@link DendroPotion}.
     * This includes any {@link Species} which has a {@link Seed} and is not the common species (or whose seed is
     * common).
     *
     * @return All {@link Species} which are marked {@code transformable} and have their own {@link Seed}.
     */
    public static List<Species> getPotionTransformableSpecies() {
        return getTransformableSpecies().stream().filter(species -> species.hasSeed() &&
                (!species.isCommonSpecies() || species.isSeedCommon())).collect(Collectors.toList());
    }

    //////////////////////////////
    // SAPLING HANDLING
    //////////////////////////////

    public final static Map<BlockState, Species> SAPLING_REPLACERS = new HashMap<>();

    public static void registerSaplingReplacer(BlockState state, Species species) {
        SAPLING_REPLACERS.put(state, species);
    }

    //////////////////////////////
    // DROP HANDLING
    //////////////////////////////

    public static final Identifier GLOBAL = io.github.steveplays28.dynamictreesfabric.DynamicTreesFabric.resLoc("global");

    /**
     * This exists so that mods not interested in making Dynamic Trees can still add drops to all trees.
     *
     * @param configuration the drop creator configuration to register
     */
    public static boolean registerDropCreator(final Identifier speciesName,
                                              final DropCreatorConfiguration configuration) {
        return findSpecies(speciesName).addDropCreators(configuration);
    }

    public static void registerGlobalDropCreator(final Identifier registryName,
                                                 final DropCreatorConfiguration configuration) {
        GlobalDropCreators.put(registryName, configuration);
    }

    public static boolean removeDropCreators(final Identifier speciesName,
                                             final Identifier dropCreatorName) {
        return findSpecies(speciesName).removeDropCreator(dropCreatorName);
    }

    public static Map<Identifier, List<DropCreatorConfiguration>> getDropCreatorsMap() {
        final Map<Identifier, List<DropCreatorConfiguration>> dir = new HashMap<>();
        dir.put(GLOBAL, GlobalDropCreators.getAll());
        Species.REGISTRY.forEach(species -> dir.put(species.getRegistryName(), species.getDropCreators()));
        return dir;
    }

    //////////////////////////////
    // CELL KIT HANDLING
    //////////////////////////////

    public static CellKit findCellKit(String name) {
        return findCellKit(getResLoc(name));
    }

    public static CellKit findCellKit(Identifier name) {
        return CellKit.REGISTRY.get(name);
    }

    //////////////////////////////
    // GROWTH LOGIC KIT HANDLING
    //////////////////////////////

    public static GrowthLogicKit findGrowthLogicKit(final String name) {
        return findGrowthLogicKit(getResLoc(name));
    }

    public static GrowthLogicKit findGrowthLogicKit(final Identifier name) {
        return GrowthLogicKit.REGISTRY.get(name);
    }

    public static Identifier getResLoc(final String resLocStr) {
        return processResLoc(new Identifier(resLocStr));
    }

    /**
     * Parses resource location and  processes it via {@link #processResLoc(Identifier)}. If it could not be
     * parsed, returns {@link DTTrees#NULL}.
     *
     * @param resourceLocationString The {@link Identifier} {@link String} to parse.
     * @return The parsed and processed {@link Identifier} object.
     */
    public static Identifier parseResLoc(final String resourceLocationString) {
        return Optional.ofNullable(Identifier.tryParse(resourceLocationString))
                .orElse(DTTrees.NULL);
    }

    /**
     * Changes namespace of resource location to "dynamictrees" as a default if it is set to Minecraft. This is safe
     * since Minecraft won't (or shouldn't) have used any of our registries.
     *
     * @param resourceLocation The {@link Identifier} to parse.
     * @return The {@link Identifier} object.
     */
    public static Identifier processResLoc(final Identifier resourceLocation) {
        return io.github.steveplays28.dynamictreesfabric.DynamicTreesFabric.MINECRAFT.equals(resourceLocation.getNamespace()) ?
                io.github.steveplays28.dynamictreesfabric.DynamicTreesFabric.resLoc(resourceLocation.getPath()) : resourceLocation;
    }

}
