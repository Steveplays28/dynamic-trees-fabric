package io.github.steveplays28.dynamictreesfabric.deserialisation;

import com.google.gson.JsonElement;
import io.github.steveplays28.dynamictreesfabric.api.treepacks.Applier;
import io.github.steveplays28.dynamictreesfabric.api.treepacks.PropertyApplier;
import io.github.steveplays28.dynamictreesfabric.api.treepacks.PropertyApplierResult;
import io.github.steveplays28.dynamictreesfabric.util.function.TriFunction;
import org.apache.logging.log4j.util.TriConsumer;
import org.jetbrains.annotations.Nullable;

import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;
import net.minecraft.util.InvalidIdentifierException;

public class TagKeyJsonPropertyApplier<K, O, V> extends PropertyApplier<O, V, JsonElement> {
	protected final TriFunction<TagKey<K>, O, V, PropertyApplierResult> tagKeyFunction;
	protected final RegistryKey<? extends Registry<K>> registryKey;

	public TagKeyJsonPropertyApplier(RegistryKey<? extends Registry<K>> registryKey, Class<O> objectClass, Class<V> valueClass, TriConsumer<TagKey<K>, O, V> tagKeyConsumer) {
		this(registryKey, objectClass, valueClass, (tagKey, o, v) -> {
			tagKeyConsumer.accept(tagKey, o, v);
			return PropertyApplierResult.success();
		});
	}

	public TagKeyJsonPropertyApplier(RegistryKey<? extends Registry<K>> registryKey, Class<O> objectClass, Class<V> valueClass, TriFunction<TagKey<K>, O, V, PropertyApplierResult> tagKeyFunction) {
		super("none", objectClass, valueClass, (o, v) -> {
		});
		this.tagKeyFunction = tagKeyFunction;
		this.registryKey = registryKey;
	}

	@SuppressWarnings("unchecked")
	@Nullable
	@Override
	public PropertyApplierResult applyIfShould(String key, Object object, JsonElement input) {
		if (!this.objectClass.isInstance(object))
			return null;

		try {
			TagKey<K> tagKey = TagKey.of(this.registryKey, new Identifier(key.charAt(0) == '#' ? key.substring(1) : key));
			return JsonDeserialisers.getOrThrow(this.valueClass).deserialise(input).map(value -> this.tagKeyFunction.apply(tagKey, (O) object, value))
					.orElseApply(
							PropertyApplierResult::failure,
							PropertyApplierResult::addWarnings,
							null
					);
		} catch (InvalidIdentifierException e) {
			return PropertyApplierResult.failure(e.getMessage());
		}
	}

	@Nullable
	@Override
	protected <S, R> PropertyApplierResult applyIfShould(Object object, JsonElement input, Class<R> valueClass, Applier<S, R> applier) {
		return null;
	}
}
