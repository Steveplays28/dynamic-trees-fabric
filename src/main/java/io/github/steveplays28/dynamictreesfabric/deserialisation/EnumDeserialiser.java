package io.github.steveplays28.dynamictreesfabric.deserialisation;

import com.electronwill.nightconfig.core.EnumGetMethod;
import io.github.steveplays28.dynamictreesfabric.deserialisation.result.Result;
import com.google.gson.JsonElement;

/**
 * An {@link JsonDeserialiser} for getting the given {@link Enum} of type {@link T} from a {@link JsonElement}.
 *
 * @param <T> The {@link Enum} type.
 * @author Harley O'Connor
 */
public final class EnumDeserialiser<T extends Enum<T>> implements JsonDeserialiser<T> {

    private final Class<T> enumType;

    public EnumDeserialiser(Class<T> enumType) {
        this.enumType = enumType;
    }

    @Override
    public Result<T, JsonElement> deserialise(JsonElement jsonElement) {
        return JsonDeserialisers.STRING.deserialise(jsonElement).map(enumStr -> EnumGetMethod.NAME_IGNORECASE.get(enumStr, this.enumType),
                "Couldn't get enum " + this.enumType + " from value '{previous_value}'.");
    }


}
