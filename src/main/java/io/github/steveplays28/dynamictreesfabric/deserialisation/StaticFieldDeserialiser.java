package io.github.steveplays28.dynamictreesfabric.deserialisation;

import java.lang.reflect.Modifier;
import java.util.stream.Stream;

import com.google.gson.JsonElement;
import io.github.steveplays28.dynamictreesfabric.deserialisation.result.Result;
import org.apache.logging.log4j.LogManager;

/**
 * Gets an {@link Object} of type {@link T} from the name of the {@code public static} {@link java.lang.reflect.Field}
 * given from the {@link JsonElement}.
 * <p>
 * Note that {@code field}s are case-sensitive and so the name must be checked by
 * {@link String#equals(Object)}, not {@link String#equalsIgnoreCase(String)}. Also note that this should not be used
 * on Minecraft classes because they are normally obfuscated in production environments.
 *
 * @author Harley O'Connor
 */
public final class StaticFieldDeserialiser<T> implements JsonDeserialiser<T> {

	private final Class<T> type;

	public StaticFieldDeserialiser(final Class<T> type) {
		this.type = type;
	}

	@Override
	public Result<T, JsonElement> deserialise(JsonElement jsonElement) {
		return JsonDeserialisers.STRING.deserialise(jsonElement)
				.map(string -> Stream.of(this.type.getFields())
								.filter(field -> Modifier.isStatic(field.getModifiers()) && field.getName().equals(string))
								.findFirst().map(field -> {
									try {
										final Object obj = field.get(null);

										if (this.type.isInstance(obj)) {
											return this.type.cast(obj);
										}
									} catch (final IllegalAccessException e) {
										LogManager.getLogger().warn("Tried to access field '" + field.getName() + "' illegally from class '" + this.type.getName() + "'.", e);
									}
									return null;
								}).orElse(null),
						"Could not get '" + this.type.getName() + "' from '{previous_value}'.");
	}

}
