package io.github.steveplays28.dynamictreesfabric.deserialisation;

import com.google.gson.JsonElement;
import io.github.steveplays28.dynamictreesfabric.api.registry.Registry;
import io.github.steveplays28.dynamictreesfabric.api.registry.RegistryEntry;
import io.github.steveplays28.dynamictreesfabric.api.registry.SimpleRegistry;
import io.github.steveplays28.dynamictreesfabric.deserialisation.result.Result;

/**
 * Gets {@link RegistryEntry} object of type {@link T} from the given {@link SimpleRegistry} object.
 *
 * @author Harley O'Connor
 */
public final class RegistryEntryDeserialiser<T extends RegistryEntry<T>> implements JsonDeserialiser<T> {

	private final Registry<T> registry;

	public RegistryEntryDeserialiser(Registry<T> registry) {
		this.registry = registry;
	}

	@Override
	public Result<T, JsonElement> deserialise(JsonElement jsonElement) {
		return JsonDeserialisers.DT_RESOURCE_LOCATION.deserialise(jsonElement)
				.map(
						this.registry::get,
						RegistryEntry::isValid,
						"Could not find " + this.registry.getName() + " for registry name '{}'."
				);
	}

}
