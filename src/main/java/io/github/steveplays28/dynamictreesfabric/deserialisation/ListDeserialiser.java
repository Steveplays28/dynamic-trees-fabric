package io.github.steveplays28.dynamictreesfabric.deserialisation;

import java.util.LinkedList;
import java.util.List;
import java.util.function.Supplier;

import com.google.gson.JsonElement;
import io.github.steveplays28.dynamictreesfabric.deserialisation.result.Result;

public final class ListDeserialiser<T> implements JsonDeserialiser<List<T>> {

	private final JsonDeserialiser<T> thisGetter;
	private final Supplier<List<T>> listSupplier;

	public ListDeserialiser(JsonDeserialiser<T> thisGetter) {
		this(thisGetter, LinkedList::new);
	}

	public ListDeserialiser(JsonDeserialiser<T> thisGetter, Supplier<List<T>> listSupplier) {
		this.thisGetter = thisGetter;
		this.listSupplier = listSupplier;
	}

	public static <T> Class<List<T>> getListClass(Class<T> c) {
		return getListClass(c, LinkedList::new);
	}

	@SuppressWarnings({"MismatchedQueryAndUpdateOfCollection", "unchecked"})
	public static <T> Class<List<T>> getListClass(Class<T> c, Supplier<List<T>> listSupplier) {
		List<T> instance = listSupplier.get();
		return (Class<List<T>>) instance.getClass();
	}

	@Override
	public Result<List<T>, JsonElement> deserialise(JsonElement jsonElement) {
		return JsonDeserialisers.JSON_ARRAY.deserialise(jsonElement).map(array -> {
			final List<T> getterList = this.listSupplier.get();
			array.forEach(elem -> {
				thisGetter.deserialise(elem).ifSuccess(getterList::add);
			});
			return getterList;
		});
	}
}
