package io.github.steveplays28.dynamictreesfabric.resources.loader;

import java.util.HashMap;
import java.util.Map;

import com.google.gson.JsonObject;
import io.github.steveplays28.dynamictreesfabric.api.TreeRegistry;
import io.github.steveplays28.dynamictreesfabric.api.resource.loading.preparation.JsonRegistryResourceLoader;
import io.github.steveplays28.dynamictreesfabric.api.treepacks.Applier;
import io.github.steveplays28.dynamictreesfabric.api.treepacks.ApplierRegistryEvent;
import io.github.steveplays28.dynamictreesfabric.api.treepacks.PropertyApplierResult;
import io.github.steveplays28.dynamictreesfabric.blocks.leaves.LeavesProperties;
import io.github.steveplays28.dynamictreesfabric.blocks.rootyblocks.SoilHelper;
import io.github.steveplays28.dynamictreesfabric.deserialisation.JsonDeserialisers;
import io.github.steveplays28.dynamictreesfabric.deserialisation.JsonPropertyAppliers;
import io.github.steveplays28.dynamictreesfabric.deserialisation.TagKeyJsonPropertyApplier;
import io.github.steveplays28.dynamictreesfabric.growthlogic.GrowthLogicKitConfiguration;
import io.github.steveplays28.dynamictreesfabric.items.Seed;
import io.github.steveplays28.dynamictreesfabric.systems.SeedSaplingRecipe;
import io.github.steveplays28.dynamictreesfabric.systems.dropcreators.DropCreatorConfiguration;
import io.github.steveplays28.dynamictreesfabric.systems.genfeatures.GenFeatureConfiguration;
import io.github.steveplays28.dynamictreesfabric.trees.Species;
import io.github.steveplays28.dynamictreesfabric.util.CommonSetup;
import io.github.steveplays28.dynamictreesfabric.util.JsonMapWrapper;
import io.github.steveplays28.dynamictreesfabric.util.holderset.DTBiomeHolderSet;
import net.minecraftforge.registries.ForgeRegistries;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.util.TriConsumer;

import net.minecraft.block.Block;
import net.minecraft.block.ComposterBlock;
import net.minecraft.item.Item;
import net.minecraft.registry.Registry;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.util.Identifier;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.WorldView;
import net.minecraft.world.biome.Biome;

/**
 * @author Harley O'Connor
 */
public final class SpeciesResourceLoader extends JsonRegistryResourceLoader<Species> {

	private static final Logger LOGGER = LogManager.getLogger();

	/**
	 * A {@link JsonPropertyAppliers} for applying environment factors to {@link Species} objects.
	 */
	private final JsonPropertyAppliers<Species> environmentFactorAppliers = new JsonPropertyAppliers<>(Species.class);

	private final Map<Species, Float> composterChanceCache = new HashMap<>();

	public SpeciesResourceLoader() {
		super(Species.REGISTRY, ApplierRegistryEvent.SPECIES);
	}

	@SuppressWarnings("unchecked")
	@Override
	public void registerAppliers() {
		this.environmentFactorAppliers.register(new TagKeyJsonPropertyApplier<>(Registry.BIOME_REGISTRY, Species.class, Float.class,
				(TriConsumer<TagKey<Biome>, Species, Float>) (tagKey, species, factor) -> species.envFactor(tagKey, factor)));

		JsonDeserialisers.register(Species.CommonOverride.class, input ->
				JsonDeserialisers.BIOME_PREDICATE.deserialise(input)
						.map(biomePredicate -> (world, pos) -> world instanceof WorldView &&
								biomePredicate.test(((WorldView) world).getBiome(pos)))
		);

		this.loadAppliers
				.register("seed", Identifier.class, this::setSeed)
				.register("generate_seed", Boolean.class, Species::setShouldGenerateSeed)
				.register("generate_sapling", Boolean.class, Species::setShouldGenerateSapling)
				.register("sapling_name", String.class, Species::setSaplingName)
				.register("seed_name", String.class, Species::setSeedName);

		// Needed in common so sound and shape is known to clients joining servers.
		// TODO: Consider marking certain reload appliers as being needed client-side and loaded when joining a server.
		this.commonAppliers
				.register("always_show_on_waila", Boolean.class, Species::setAlwaysShowOnWaila)
				.register("sapling_sound", BlockSoundGroup.class, Species::setSaplingSound)
				.register("sapling_shape", VoxelShape.class, Species::setSaplingShape);

		// We need the sapling shape to know which parent smartmodel the sapling model should use.
		this.gatherDataAppliers
				.register("sapling_shape", VoxelShape.class, Species::setSaplingShape);

		this.reloadAppliers
				.register("tapering", Float.class, Species::setTapering)
				.register("up_probability", Integer.class, Species::setUpProbability)
				.register("lowest_branch_height", Integer.class, Species::setLowestBranchHeight)
				.register("signal_energy", Float.class, Species::setSignalEnergy)
				.register("growth_rate", Float.class, Species::setGrowthRate)
				.register("soil_longevity", Integer.class, Species::setSoilLongevity)
				.register("max_branch_radius", Integer.class, Species::setMaxBranchRadius)
				.register("transformable", Boolean.class, Species::setTransformable)
				.register("growth_logic_kit", GrowthLogicKitConfiguration.class, Species::setGrowthLogicKit)
				.register("leaves_properties", LeavesProperties.class, Species::setLeavesProperties)
				.register("world_gen_leaf_map_height", Integer.class, Species::setWorldGenLeafMapHeight)
				.register("environment_factors", JsonObject.class, this::applyEnvironmentFactors)
				.register("seed_drop_rarity", Float.class, Species::setupStandardSeedDropping)
				.register("stick_drop_rarity", Float.class, Species::setupStandardStickDropping)
				.register("mega_species", Identifier.class, this::setMegaSpecies)
				.register("seed", Seed.class, (species, seed) -> species.setSeed(() -> seed))
				.register("seed_composter_chance", Float.class, this.composterChanceCache::put)
				.register("sapling_grows_naturally", Boolean.class, Species::setCanSaplingGrowNaturally)
				.register("primitive_sapling", SeedSaplingRecipe.class, Species::addPrimitiveSaplingRecipe)
				.registerArrayApplier("primitive_saplings", SeedSaplingRecipe.class, Species::addPrimitiveSaplingRecipe)
				.register("common_override", Species.CommonOverride.class, Species::setCommonOverride)
				.register("perfect_biomes", DTBiomeHolderSet.class,
						(species, biomeList) -> species.getPerfectBiomes().getIncludeComponents().add(biomeList))
				.register("can_bone_meal_tree", Boolean.class, Species::setCanBoneMealTree)
				.registerArrayApplier("acceptable_growth_blocks", Block.class, Species::addAcceptableBlockForGrowth)
				.registerArrayApplier("acceptable_soils", String.class, (Applier<Species, String>) this::addAcceptableSoil)
				.registerArrayApplier("features", GenFeatureConfiguration.class, Species::addGenFeature)
				.registerArrayApplier("drop_creators", DropCreatorConfiguration.class, Species::addDropCreators)
				.register("does_rot", Boolean.class, Species::setDoesRot);

		super.registerAppliers();
	}

	private void setSeed(Species species, Identifier seedName) {
		final Identifier processedSeedName = TreeRegistry.processResLoc(seedName);
		species.setShouldGenerateSeed(false);
		species.setShouldGenerateSapling(false);
		CommonSetup.runOnCommonSetup(event -> {
			final Item seed = ForgeRegistries.ITEMS.getValue(processedSeedName);
			if (seed instanceof Seed) {
				species.setSeed(() -> (Seed) seed);
			} else {
				LOGGER.warn("Could not find valid seed item from registry name \"" + seedName + "\".");
			}
		});
	}

	private void applyEnvironmentFactors(Species species, JsonObject jsonObject) {
		this.environmentFactorAppliers.applyAll(new JsonMapWrapper(jsonObject), species)
				.forEachErrorWarning(
						error -> LOGGER.error("Error applying environment factor for " +
								"species '{}': {}", species.getRegistryName(), error),
						warning -> LOGGER.warn("Warning applying environment factor for " +
								"species '{}': {}", species.getRegistryName(), warning)
				);
	}

	private void setMegaSpecies(Species species, Identifier registryName) {
		final Identifier processedRegName = TreeRegistry.processResLoc(registryName);
		Species.REGISTRY.runOnNextLock(Species.REGISTRY.generateIfValidRunnable(processedRegName, species::setMegaSpecies, () -> LOGGER.warn("Could not set mega species for '" +
				species + "' as Species '" + processedRegName + "' was not found.")));
	}

	private PropertyApplierResult addAcceptableSoil(Species species, String acceptableSoil) {
		if (SoilHelper.getSoilFlags(acceptableSoil) == 0) {
			return PropertyApplierResult.failure("Could not find acceptable soil '" + acceptableSoil + "'.");
		}

		species.addAcceptableSoils(acceptableSoil);
		return PropertyApplierResult.success();
	}

	@Override
	protected void postLoadOnLoad(LoadData loadData, JsonObject json) {
		super.postLoadOnLoad(loadData, json);
		loadData.getResource().generateSeed().generateSapling();
	}

	@Override
	protected void postLoadOnReload(LoadData loadData, JsonObject json) {
		final Species species = loadData.getResource();
		this.composterChanceCache.put(species, species.defaultSeedComposterChance());
		if (this.shouldClearDropCreators(json)) {
			species.getDropCreators().clear();
		}
		super.postLoadOnReload(loadData, json);
		this.registerComposterChances();
	}

	private void registerComposterChances() {
		this.composterChanceCache.forEach((species, chance) -> {
			if (species.getSeed().isPresent() && chance > 0) {
				ComposterBlock.registerCompostableItem(chance, species.getSeed().get());
			}
		});
		this.composterChanceCache.clear();
	}

	private boolean shouldClearDropCreators(JsonObject json) {
		return json.has("drop_creators") && json.get("drop_creators").isJsonArray();
	}

}
