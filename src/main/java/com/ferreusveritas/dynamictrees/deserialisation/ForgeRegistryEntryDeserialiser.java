package com.ferreusveritas.dynamictrees.deserialisation;

import com.ferreusveritas.dynamictrees.deserialisation.result.Result;
import com.google.gson.JsonElement;
import net.minecraftforge.registries.IForgeRegistry;

import org.jetbrains.annotations.Nullable;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Predicate;

/**
 * Implementation of {@link JsonDeserialiser} that attempts to
 * get a forge registry entry object from a {@link JsonElement}.
 *
 * @author Harley O'Connor
 */
public final class ForgeRegistryEntryDeserialiser<T> implements JsonDeserialiser<T> {

    private final IForgeRegistry<T> registry;
    private final String registryDisplayName;

    @Nullable
    private final T nullValue;
    private final Predicate<T> validator;

    public ForgeRegistryEntryDeserialiser(final IForgeRegistry<T> registry, final String registryDisplayName) {
        this(registry, registryDisplayName, null);
    }

    public ForgeRegistryEntryDeserialiser(final IForgeRegistry<T> registry, final String registryDisplayName, @Nullable final T nullValue) {
        this.registry = registry;
        this.registryDisplayName = registryDisplayName;
        this.nullValue = nullValue;
        this.validator = value -> value != nullValue;
    }

    @Override
    public Result<T, JsonElement> deserialise(JsonElement jsonElement) {
        final AtomicBoolean intentionallyNull = new AtomicBoolean();
        return JsonDeserialisers.RESOURCE_LOCATION.deserialise(jsonElement).map(registryName -> {
                    // If registry name is the null value's registry name then it was intentionally the null value, so don't warn.
                    if (this.nullValue != null && Objects.equals(registryName, this.registry.getKey(this.nullValue))) {
                        intentionallyNull.set(true);
                        return this.nullValue;
                    }

                    return this.registry.getValue(registryName);
                }, value -> intentionallyNull.get() || this.validator.test(value),
                "Could not find " + this.registryDisplayName + " for registry name '{}'.");
    }

}
