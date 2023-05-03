package io.github.steveplays28.dynamictreesfabric.deserialisation;

import java.util.Locale;

import com.google.gson.JsonElement;
import io.github.steveplays28.dynamictreesfabric.deserialisation.result.Result;
import org.apache.commons.lang3.StringUtils;

import net.minecraft.util.Identifier;

/**
 * An {@link JsonDeserialiser} for {@link Identifier}s, but if no namespace is defined it defaults to the
 * specified {@link #defaultNamespace} given in {@link #ResourceLocationDeserialiser(String)}.
 * <p>
 * Main instance stored in {@link JsonDeserialisers#RESOURCE_LOCATION} for fetching resource locations with default
 * namespace {@code minecraft}.
 *
 * @author Harley O'Connor
 */
public final class ResourceLocationDeserialiser implements JsonDeserialiser<Identifier> {

	private final String defaultNamespace;

	public ResourceLocationDeserialiser(String defaultNamespace) {
		this.defaultNamespace = defaultNamespace;
	}

	public static boolean isValidResourceLocation(String p_217855_0_) {
		final String[] namespaceAndPath = Identifier.split(p_217855_0_, ':');
		return Identifier.isNamespaceValid(StringUtils.isEmpty(namespaceAndPath[0]) ? "minecraft" : namespaceAndPath[0])
				&& Identifier.isPathValid(namespaceAndPath[1]);
	}

	public static ResourceLocationDeserialiser create() {
		return new ResourceLocationDeserialiser("minecraft");
	}

	public static ResourceLocationDeserialiser create(final String defaultNamespace) {
		return new ResourceLocationDeserialiser(defaultNamespace);
	}

	@Override
	public Result<Identifier, JsonElement> deserialise(JsonElement jsonElement) {
		return JsonDeserialisers.STRING.deserialise(jsonElement)
				.map(string -> string.toLowerCase(Locale.ROOT))
				.mapIfValid(ResourceLocationDeserialiser::isValidResourceLocation,
						"Invalid resource location '{value}'. Namespace Constraints: [a-z0-9_.-] Path Constraints: [a-z0-9/._-]",
						this::decode);
	}

	private Identifier decode(final String resLocStr) {
		final String[] namespaceAndPath = new String[]{this.defaultNamespace, resLocStr};
		final int colonIndex = resLocStr.indexOf(':');
		if (colonIndex >= 0) {
			namespaceAndPath[1] = resLocStr.substring(colonIndex + 1);
			if (colonIndex >= 1) {
				namespaceAndPath[0] = resLocStr.substring(0, colonIndex);
			}
		}

		return new Identifier(namespaceAndPath[0], namespaceAndPath[1]);
	}

}
