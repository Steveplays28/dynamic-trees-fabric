package io.github.steveplays28.dynamictreesfabric.api.resource.loading.preparation;

import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import io.github.steveplays28.dynamictreesfabric.api.resource.DTResource;
import io.github.steveplays28.dynamictreesfabric.api.resource.ResourceCollector;
import io.github.steveplays28.dynamictreesfabric.deserialisation.JsonHelper;

import net.minecraft.resource.Resource;
import net.minecraft.util.Identifier;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import java.io.IOException;
import java.io.Reader;

/**
 * @author Harley O'Connor
 */
public final class JsonResourcePreparer extends AbstractResourcePreparer<JsonElement> {

	private static final String JSON_EXTENSION = ".json";

	public JsonResourcePreparer(String folderName) {
		this(folderName, ResourceCollector.ordered());
	}

	public JsonResourcePreparer(String folderName, ResourceCollector<JsonElement> resourceCollector) {
		super(folderName, JSON_EXTENSION, resourceCollector);
	}

	@Nonnull
	static JsonElement readResource(Resource resource) throws PreparationException, IOException {
		final Reader reader = resource.getReader();
		final JsonElement json = tryParseJson(reader);

		if (json == null) {
			throw new PreparationException("Couldn't load file as it's null or empty");
		}
		return json;
	}

	@Nullable
	private static JsonElement tryParseJson(Reader reader) throws PreparationException {
		try {
			return net.minecraft.util.JsonHelper.deserialize(JsonHelper.getGson(), reader, JsonElement.class);
		} catch (JsonParseException e) {
			throw new PreparationException(e);
		}
	}

	@Override
	protected void readAndPutResource(Resource resource, Identifier resourceName) throws PreparationException, IOException {
		final JsonElement jsonElement = readResource(resource);
		this.resourceCollector.put(new DTResource<>(resourceName, jsonElement));
	}

}
