package io.github.steveplays28.dynamictreesfabric.deserialisation;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import io.github.steveplays28.dynamictreesfabric.deserialisation.result.Result;

/**
 * @author Harley O'Connor
 */
public final class MapDeserialiser<K, V> implements JsonDeserialiser<Map<K, V>> {

	private final JsonDeserialiser<K> keyGetter;
	private final JsonDeserialiser<V> valueGetter;
	private final Supplier<Map<K, V>> mapSupplier;

	public MapDeserialiser(JsonDeserialiser<K> keyGetter, JsonDeserialiser<V> valueGetter) {
		this(keyGetter, valueGetter, HashMap::new);
	}

	public MapDeserialiser(JsonDeserialiser<K> keyGetter, JsonDeserialiser<V> valueGetter, Supplier<Map<K, V>> mapSupplier) {
		this.keyGetter = keyGetter;
		this.valueGetter = valueGetter;
		this.mapSupplier = mapSupplier;
	}

	public static <K, V> Class<Map<K, V>> getMapClass(Class<K> keyClass, Class<V> valueClass) {
		return getMapClass(keyClass, valueClass, HashMap::new);
	}

	@SuppressWarnings({"MismatchedQueryAndUpdateOfCollection", "unchecked"})
	public static <K, V> Class<Map<K, V>> getMapClass(Class<K> keyClass, Class<V> valueClass, Supplier<Map<K, V>> mapSupplier) {
		return (Class<Map<K, V>>) mapSupplier.get().getClass();
	}

	@Override
	public Result<Map<K, V>, JsonElement> deserialise(JsonElement jsonElement) {
		return JsonDeserialisers.JSON_OBJECT.deserialise(jsonElement).map((object, warningConsumer) -> {
			final Map<K, V> map = this.mapSupplier.get();
			for (Map.Entry<String, JsonElement> entry : object.entrySet()) {
				this.valueGetter.deserialise(entry.getValue()).map(
						value -> this.keyGetter.deserialise(new JsonPrimitive(entry.getKey()))
								.ifSuccessOrElseThrow(key -> map.put(key, value), warningConsumer)
				).orElseThrow();
			}
			return map;
		});
	}

}
