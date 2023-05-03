package io.github.steveplays28.dynamictreesfabric.worldgen.deserialisation;

import java.util.Map;
import java.util.function.Consumer;

import org.jetbrains.annotations.Nullable;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.github.steveplays28.dynamictreesfabric.api.TreeRegistry;
import io.github.steveplays28.dynamictreesfabric.api.worldgen.BiomePropertySelectors;
import io.github.steveplays28.dynamictreesfabric.deserialisation.DeserialisationException;
import io.github.steveplays28.dynamictreesfabric.deserialisation.JsonDeserialisers;
import io.github.steveplays28.dynamictreesfabric.deserialisation.result.JsonResult;
import io.github.steveplays28.dynamictreesfabric.deserialisation.result.Result;
import io.github.steveplays28.dynamictreesfabric.trees.Species;

/**
 * Gets an {@link BiomePropertySelectors.SpeciesSelector} object from a {@link JsonElement}.
 *
 * @author Harley O'Connor
 */
public final class SpeciesSelectorDeserialiser implements JsonBiomeDatabaseDeserialiser<BiomePropertySelectors.SpeciesSelector> {

	@Override
	public Result<BiomePropertySelectors.SpeciesSelector, JsonElement> deserialise(final JsonElement input) {
		return JsonResult.forInput(input)
				.mapIfType(Species.class, this::readStatic)
				.elseMapIfType(String.class, this::readStatic)
				.elseMapIfType(JsonObject.class, this::readSelector)
				.elseTypeError();
	}

	private BiomePropertySelectors.SpeciesSelector readStatic(Species species) {
		return new BiomePropertySelectors.StaticSpeciesSelector(
				new BiomePropertySelectors.SpeciesSelection(species)
		);
	}

	private BiomePropertySelectors.SpeciesSelector readStatic(String string) throws DeserialisationException {
		if (this.isDefault(string)) {
			return new BiomePropertySelectors.StaticSpeciesSelector();
		}
		throw new DeserialisationException("\"" + string + "\" is not a supported parameter for a " +
				"static species selector.");
	}

	private BiomePropertySelectors.SpeciesSelector readSelector(JsonObject object, Consumer<String> warningConsumer)
			throws DeserialisationException {

		return JsonResult.forInput(object)
				.mapIfContains(STATIC, JsonElement.class, input ->
						JsonResult.forInput(input)
								.mapIfType(Species.class, this::readStatic)
								.elseMapIfType(String.class, this::readStatic)
								.elseTypeError()
								.forEachWarning(warningConsumer)
								.orElseThrow()
				).elseMapIfContains(RANDOM, JsonElement.class, input ->
						this.getRandomSpeciesSelector(input, warningConsumer)
				).forEachWarning(warningConsumer)
				.orElseThrow();
	}

	@Nullable
	private BiomePropertySelectors.SpeciesSelector getRandomSpeciesSelector(JsonElement input,
	                                                                        Consumer<String> warningConsumer)
			throws DeserialisationException {

		return JsonDeserialisers.JSON_OBJECT.deserialise(input).map(object -> {
					final BiomePropertySelectors.RandomSpeciesSelector randomSelector = new BiomePropertySelectors.RandomSpeciesSelector();

					for (Map.Entry<String, JsonElement> entry : object.entrySet()) {
						final String speciesName = entry.getKey();

						JsonDeserialisers.INTEGER.deserialise(entry.getValue()).ifSuccessOrElseThrow(weight -> {
							if (weight > 0) {
								if (this.isDefault(speciesName)) {
									randomSelector.add(weight);
								} else {
									TreeRegistry.findSpeciesSloppy(speciesName).ifValid(species ->
											randomSelector.add(species, weight)
									);
								}
							}
						}, warningConsumer);
					}

					if (randomSelector.getSize() < 1) {
						throw new DeserialisationException("No species were selected in random selector '" + input + "'.");
					}
					return randomSelector;
				})
				.forEachWarning(warningConsumer)
				.orElseThrow();
	}

}
