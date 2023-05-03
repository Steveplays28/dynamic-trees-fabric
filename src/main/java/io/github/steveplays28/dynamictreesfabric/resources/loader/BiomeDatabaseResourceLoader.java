package io.github.steveplays28.dynamictreesfabric.resources.loader;

import static io.github.steveplays28.dynamictreesfabric.api.resource.loading.ApplierResourceLoader.postApplierEvent;
import static io.github.steveplays28.dynamictreesfabric.deserialisation.JsonHelper.throwIfShouldNotLoad;

import java.util.Deque;
import java.util.LinkedList;
import java.util.function.Consumer;
import java.util.stream.StreamSupport;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.github.steveplays28.dynamictreesfabric.api.event.Hooks;
import io.github.steveplays28.dynamictreesfabric.api.resource.ResourceAccessor;
import io.github.steveplays28.dynamictreesfabric.api.resource.loading.AbstractResourceLoader;
import io.github.steveplays28.dynamictreesfabric.api.resource.loading.ApplierResourceLoader;
import io.github.steveplays28.dynamictreesfabric.api.resource.loading.preparation.MultiJsonResourcePreparer;
import io.github.steveplays28.dynamictreesfabric.api.treepacks.ApplierRegistryEvent;
import io.github.steveplays28.dynamictreesfabric.api.treepacks.PropertyApplierResult;
import io.github.steveplays28.dynamictreesfabric.api.worldgen.BiomePropertySelectors;
import io.github.steveplays28.dynamictreesfabric.api.worldgen.FeatureCanceller;
import io.github.steveplays28.dynamictreesfabric.deserialisation.BiomeListDeserialiser;
import io.github.steveplays28.dynamictreesfabric.deserialisation.DeserialisationException;
import io.github.steveplays28.dynamictreesfabric.deserialisation.JsonDeserialisers;
import io.github.steveplays28.dynamictreesfabric.deserialisation.JsonPropertyAppliers;
import io.github.steveplays28.dynamictreesfabric.deserialisation.result.JsonResult;
import io.github.steveplays28.dynamictreesfabric.deserialisation.result.Result;
import io.github.steveplays28.dynamictreesfabric.init.DTConfigs;
import io.github.steveplays28.dynamictreesfabric.util.CommonCollectors;
import io.github.steveplays28.dynamictreesfabric.util.IgnoreThrowable;
import io.github.steveplays28.dynamictreesfabric.util.JsonMapWrapper;
import io.github.steveplays28.dynamictreesfabric.util.holderset.DTBiomeHolderSet;
import io.github.steveplays28.dynamictreesfabric.util.holderset.DelayedAnyHolderSet;
import io.github.steveplays28.dynamictreesfabric.worldgen.BiomeDatabase;
import io.github.steveplays28.dynamictreesfabric.worldgen.BiomeDatabases;
import io.github.steveplays28.dynamictreesfabric.worldgen.FeatureCancellationRegistry;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import net.minecraft.world.gen.GenerationStep;

/**
 * @author Harley O'Connor
 */
public final class BiomeDatabaseResourceLoader
		extends AbstractResourceLoader<Iterable<JsonElement>>
		implements ApplierResourceLoader<Iterable<JsonElement>> {

	public static final String SELECT = "select";
	public static final String APPLY = "apply";
	public static final String WHITE = "white";
	public static final String CANCELLERS = "cancellers";
	public static final String ENTRY_APPLIERS = "entries";
	public static final String CANCELLATION_APPLIERS = "cancellations";
	private static final Logger LOGGER = LogManager.getLogger();
	private static final String DEFAULT_POPULATOR = "default";
	private static final String METHOD = "method";
	private final JsonPropertyAppliers<BiomeDatabase.Entry> entryAppliers =
			new JsonPropertyAppliers<>(BiomeDatabase.Entry.class);
	private final JsonPropertyAppliers<BiomePropertySelectors.FeatureCancellations> cancellationAppliers =
			new JsonPropertyAppliers<>(BiomePropertySelectors.FeatureCancellations.class);

	public BiomeDatabaseResourceLoader() {
		super(new MultiJsonResourcePreparer("world_gen"));
	}

	public static BiomeDatabase.Operation getOperationOrWarn(final JsonElement jsonElement) {
		return getOperation(jsonElement).orElse(BiomeDatabase.Operation.REPLACE, LOGGER::error, LOGGER::warn);
	}

	private static Result<BiomeDatabase.Operation, JsonElement> getOperation(final JsonElement input) {
		return JsonDeserialisers.JSON_OBJECT.deserialise(input)
				.removeError() // Remove error at this point as we don't want to warn when element is not Json object.
				.map(jsonObject -> jsonObject.has(METHOD) ? jsonObject.get(METHOD) : null)
				.map(JsonDeserialisers.OPERATION::deserialise)
				.orElseApply(error -> JsonResult.failure(input, "Error getting operation (defaulting to " +
								"replace): " + error),
						JsonResult.success(input, BiomeDatabase.Operation.REPLACE));
	}

	@Override
	public void registerAppliers() {
		this.entryAppliers
				.register("species", JsonElement.class, this::applySpecies)
				.register("density", JsonElement.class, this::applyDensity)
				.register("chance", JsonElement.class, this::applyChance)
				.register("multipass", Boolean.class, this::applyMultipass)
				.register("multipass", JsonObject.class, BiomeDatabase.Entry::setCustomMultipass)
				.register("blacklist", Boolean.class, BiomeDatabase.Entry::setBlacklisted)
				.register("forestness", Float.class, BiomeDatabase.Entry::setForestness)
				.register("subterranean", Boolean.class, BiomeDatabase.Entry::setSubterranean)
				.registerIfTrueApplier("reset", BiomeDatabase.Entry::reset);

		this.cancellationAppliers
				.register("namespace", String.class, BiomePropertySelectors.FeatureCancellations::putNamespace)
				.registerArrayApplier("namespaces", String.class,
						BiomePropertySelectors.FeatureCancellations::putNamespace)
				.register("type", FeatureCanceller.class, BiomePropertySelectors.FeatureCancellations::putCanceller)
				.registerArrayApplier("types", FeatureCanceller.class,
						BiomePropertySelectors.FeatureCancellations::putCanceller)
				.register("stage", GenerationStep.Feature.class,
						BiomePropertySelectors.FeatureCancellations::putStage)
				.registerArrayApplier("stages", GenerationStep.Feature.class,
						BiomePropertySelectors.FeatureCancellations::putStage);

		postApplierEvent(new EntryApplierRegistryEvent<>(this.entryAppliers, ENTRY_APPLIERS));
		postApplierEvent(new CancellationApplierRegistryEvent<>(this.cancellationAppliers,
				CANCELLATION_APPLIERS));
	}

	private PropertyApplierResult applySpecies(BiomeDatabase.Entry entry, JsonElement jsonElement) {
		return PropertyApplierResult.from(JsonDeserialisers.SPECIES_SELECTOR.deserialise(jsonElement)
				.ifSuccess(speciesSelector -> entry.getDatabase().setSpeciesSelector(entry, speciesSelector, getOperationOrWarn(jsonElement))));
	}

	private PropertyApplierResult applyDensity(BiomeDatabase.Entry entry, JsonElement jsonElement) {
		return PropertyApplierResult.from(JsonDeserialisers.DENSITY_SELECTOR.deserialise(jsonElement)
				.ifSuccess(densitySelector -> entry.getDatabase().setDensitySelector(entry, densitySelector, getOperationOrWarn(jsonElement))));
	}

	private PropertyApplierResult applyChance(BiomeDatabase.Entry entry, JsonElement jsonElement) {
		return PropertyApplierResult.from(JsonDeserialisers.CHANCE_SELECTOR.deserialise(jsonElement)
				.ifSuccess(chanceSelector -> entry.getDatabase().setChanceSelector(entry, chanceSelector, getOperationOrWarn(jsonElement))));
	}

	private void applyMultipass(BiomeDatabase.Entry entry, Boolean multipass) {
		if (!multipass) {
			return;
		}
		entry.enableDefaultMultipass();
	}

	@Override
	public void applyOnSetup(ResourceAccessor<Iterable<JsonElement>> resourceAccessor,
	                         ResourceManager resourceManager) {
		BiomeDatabases.reset();
		if (this.isWorldGenDisabled()) {
			return;
		}

		Hooks.onAddFeatureCancellers();
		this.readCancellers(
				resourceAccessor.filtered(this::isDefaultPopulator).map(this::toLinkedList)
		);
	}

	private void readCancellers(ResourceAccessor<Deque<JsonElement>> defaultPopulators) {
		this.readModCancellers(defaultPopulators);
		this.readTreePackCancellers(defaultPopulators);
	}

	private void readModCancellers(ResourceAccessor<Deque<JsonElement>> defaultPopulators) {
		defaultPopulators.getAllResources().forEach(defaultPopulator ->
				this.readCancellers(defaultPopulator.getLocation(), defaultPopulator.getResource().pollFirst())
		);
	}

	private void readTreePackCancellers(ResourceAccessor<Deque<JsonElement>> defaultPopulators) {
		defaultPopulators.getAllResources().forEach(defaultPopulator ->
				defaultPopulator.getResource().forEach(json ->
						this.readCancellers(defaultPopulator.getLocation(), json)
				)
		);
	}

	private void readCancellers(final Identifier location, final JsonElement json) {
		LOGGER.debug("Reading cancellers from Json biome populator \"{}\".", location);

		try {
			JsonResult.forInput(json)
					.mapEachIfArray(JsonObject.class, object -> {
						try {
							this.readCancellersInSection(location, object);
						} catch (IgnoreThrowable ignored) {
						}
						return PropertyApplierResult.success();
					}).forEachWarning(warning ->
							LOGGER.warn("Warning whilst loading cancellers from populator \"{}\": {}",
									location, warning)
					).orElseThrow();
		} catch (DeserialisationException e) {
			LOGGER.error("Error whilst loading cancellers from populator \"{}\": {}", location,
					e.getMessage());
		}
	}

	private void readCancellersInSection(final Identifier location, final JsonObject json)
			throws DeserialisationException, IgnoreThrowable {

		final Consumer<String> errorConsumer = error -> LOGGER.error("Error loading populator \"{}\": {}",
				location, error);
		final Consumer<String> warningConsumer = warning -> LOGGER.warn("Warning whilst loading populator " +
				"\"{}\": {}", location, warning);

		throwIfShouldNotLoad(json);

		final DTBiomeHolderSet biomes = this.collectBiomes(json, warningConsumer);

		// Running this now would be too early!
		// if (biomes.isEmpty()) {
		//     warnNoBiomesSelected(json);
		//     return;
		// }

		JsonResult.forInput(json)
				.mapIfContains(CANCELLERS, JsonObject.class, cancellerObject ->
								this.applyCanceller(location, errorConsumer, warningConsumer,
										biomes, cancellerObject),
						PropertyApplierResult.success()
				)
				.forEachWarning(warningConsumer)
				.orElseThrow();
	}

	private DTBiomeHolderSet collectBiomes(JsonObject json, Consumer<String> warningConsumer) throws DeserialisationException {
		return JsonResult.forInput(json)
				.mapIfContains(SELECT, DTBiomeHolderSet.class, list -> list)
				.forEachWarning(warningConsumer)
				.orElseThrow();
	}

	private PropertyApplierResult applyCanceller(Identifier location,
	                                             Consumer<String> errorConsumer,
	                                             Consumer<String> warningConsumer, DTBiomeHolderSet biomes,
	                                             JsonObject json) {
		final BiomePropertySelectors.FeatureCancellations cancellations =
				new BiomePropertySelectors.FeatureCancellations();
		this.applyCancellationAppliers(location, json, cancellations);

		cancellations.putDefaultStagesIfEmpty();

		final BiomeDatabase.Operation operation = JsonResult.forInput(json)
				.mapIfContains(METHOD, BiomeDatabase.Operation.class, op -> op, BiomeDatabase.Operation.SPLICE_AFTER)
				.forEachWarning(warningConsumer)
				.orElse(BiomeDatabase.Operation.SPLICE_AFTER, errorConsumer, warningConsumer);

		FeatureCancellationRegistry.addCancellations(biomes, operation, cancellations);

		return PropertyApplierResult.success();
	}

	private void applyCancellationAppliers(Identifier location, JsonObject json,
	                                       BiomePropertySelectors.FeatureCancellations cancellations) {
		this.cancellationAppliers.applyAll(new JsonMapWrapper(json), cancellations)
				.forEachErrorWarning(
						error -> LOGGER.error("Error whilst applying feature cancellations " +
								"in \"{}\" " + "populator: {}", location, error),
						warning -> LOGGER.warn("Warning whilst applying feature " +
								"cancellations in \"{}\" populator: {}", location, warning)
				);
	}

	@Override
	public void applyOnReload(ResourceAccessor<Iterable<JsonElement>> resourceAccessor,
	                          ResourceManager resourceManager) {
		BiomeDatabases.reset();
		if (this.isWorldGenDisabled()) {
			return;
		}

		this.readPopulators(
				resourceAccessor.filtered(this::isDefaultPopulator).map(this::toLinkedList)
		);
		this.readDimensionalPopulators(
				resourceAccessor.filtered(resource -> !this.isDefaultPopulator(resource)).map(this::toLinkedList)
		);
	}

	private boolean isWorldGenDisabled() {
		return !DTConfigs.WORLD_GEN.get();
	}

	private void readPopulators(ResourceAccessor<Deque<JsonElement>> resourceAccessor) {
		Hooks.onPopulateDefaultDatabase();
		this.readModPopulators(BiomeDatabases.getDefault(), resourceAccessor);
		this.readTreePackPopulators(BiomeDatabases.getDefault(), resourceAccessor);
	}

	private void readModPopulators(BiomeDatabase database, ResourceAccessor<Deque<JsonElement>> resourceAccessor) {
		resourceAccessor.getAllResources().forEach(defaultPopulator ->
				this.readPopulator(database, defaultPopulator.getLocation(), defaultPopulator.getResource().pollFirst())
		);
	}

	private void readTreePackPopulators(BiomeDatabase database, ResourceAccessor<Deque<JsonElement>> resourceAccessor) {
		resourceAccessor.getAllResources().forEach(defaultPopulator ->
				defaultPopulator.getResource().forEach(jsonElement ->
						this.readPopulator(database, defaultPopulator.getLocation(), jsonElement))
		);
	}

	private void readDimensionalPopulators(ResourceAccessor<Deque<JsonElement>> resourceAccessor) {
		Hooks.onPopulateDimensionalDatabases();
		this.readDimensionalModPopulators(resourceAccessor);
		this.readDimensionalTreePackPopulators(resourceAccessor);
	}

	private void readDimensionalModPopulators(ResourceAccessor<Deque<JsonElement>> resourceAccessor) {
		resourceAccessor.getAllResources().forEach(dimensionalPopulator ->
				this.readDimensionalPopulator(dimensionalPopulator.getLocation(),
						dimensionalPopulator.getResource().pollFirst())
		);
	}

	private void readDimensionalTreePackPopulators(ResourceAccessor<Deque<JsonElement>> resourceAccessor) {
		resourceAccessor.getAllResources().forEach(dimensionalPopulator ->
				dimensionalPopulator.getResource().forEach(json ->
						this.readDimensionalPopulator(dimensionalPopulator.getLocation(), json))
		);
	}

	private void readDimensionalPopulator(Identifier dimensionLocation, JsonElement dimensionalPopulator) {
		this.readPopulator(BiomeDatabases.getOrCreateDimensional(dimensionLocation), dimensionLocation,
				dimensionalPopulator);
	}

	private void readPopulator(BiomeDatabase database, Identifier location, JsonElement json) {
		LOGGER.debug("Loading Json biome populator \"{}\".", location);

		try {
			JsonResult.forInput(json)
					.mapEachIfArray(JsonObject.class, object -> {
						this.readPopulatorSection(database, location, object);
						return PropertyApplierResult.success();
					}).forEachWarning(warning ->
							LOGGER.warn("Warning whilst loading populator \"{}\": {}", location, warning)
					).orElseThrow();
		} catch (DeserialisationException e) {
			LOGGER.error("Error loading populator \"{}\": {}", location, e.getMessage());
		}
	}

	private void readPopulatorSection(BiomeDatabase database, Identifier location, JsonObject json)
			throws DeserialisationException {

		final DTBiomeHolderSet biomes = this.collectBiomes(json, warning ->
				LOGGER.warn("Warning whilst loading populator \"{}\": {}", location, warning));

		// Running this now would be too early!
		// if (biomes.isEmpty()) {
		//     warnNoBiomesSelected(json);
		//     return;
		// }

		JsonResult.forInput(json)
				.mapIfContains(APPLY, JsonObject.class, applyObject -> {
					BiomeDatabase.Entry entry = database.getJsonEntry(biomes);
					this.entryAppliers.applyAll(new JsonMapWrapper(applyObject), entry);
					return PropertyApplierResult.success();
				}, PropertyApplierResult.success())
				.elseMapIfContains(WHITE, String.class, type -> {
					this.applyWhite(database, location, biomes, type);
					return PropertyApplierResult.success();
				}, PropertyApplierResult.success())
				.forEachWarning(warning ->
						LOGGER.warn("Warning whilst loading populator \"{}\": {}", location, warning))
				.orElseThrow();
	}

	private void applyWhite(BiomeDatabase database, Identifier location, DTBiomeHolderSet biomes, String type)
			throws DeserialisationException {
		if (type.equalsIgnoreCase("all")) {
			DTBiomeHolderSet allBiomes = new DTBiomeHolderSet();
			allBiomes.getIncludeComponents().add(new DelayedAnyHolderSet<>(BiomeListDeserialiser.DELAYED_BIOME_REGISTRY));
			database.getJsonEntry(allBiomes).setBlacklisted(false);
		} else if (type.equalsIgnoreCase("selected")) {
			database.getJsonEntry(biomes).setBlacklisted(false);
		} else {
			throw new DeserialisationException("Unknown type for whitelist in populator \"" +
					location + "\": \"" + type + "\".");
		}
	}

	private boolean isDefaultPopulator(final Identifier key) {
		return key.getPath().equals(DEFAULT_POPULATOR);
	}

	// private void warnNoBiomesSelected(JsonObject json) {
	//     if (noBiomesSelectedWarningNotSuppressed(json)) {
	//         LogManager.getLogger().warn("Could not get any biomes from selector:\n" + json.get(SELECT));
	//     }
	// }
	//
	// private boolean noBiomesSelectedWarningNotSuppressed(JsonObject json) {
	//     final JsonElement suppress = json.get("suppress_none_selected");
	//     return suppress == null || !suppress.isJsonPrimitive() || !suppress.getAsJsonPrimitive().isBoolean() ||
	//             !suppress.getAsJsonPrimitive().getAsBoolean();
	// }

	private LinkedList<JsonElement> toLinkedList(Iterable<JsonElement> elements) {
		return StreamSupport.stream(elements.spliterator(), false)
				.collect(CommonCollectors.toLinkedList());
	}

	public static final class EntryApplierRegistryEvent<O> extends ApplierRegistryEvent<O, JsonElement> {
		public EntryApplierRegistryEvent(JsonPropertyAppliers<O> appliers, String identifier) {
			super(appliers, identifier);
		}
	}

	public static final class CancellationApplierRegistryEvent<O> extends ApplierRegistryEvent<O, JsonElement> {
		public CancellationApplierRegistryEvent(JsonPropertyAppliers<O> appliers, String identifier) {
			super(appliers, identifier);
		}
	}

}
