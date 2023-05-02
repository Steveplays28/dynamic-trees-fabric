package io.github.steveplays28.dynamictreesfabric.deserialisation;

import io.github.steveplays28.dynamictreesfabric.deserialisation.result.Result;
import io.github.steveplays28.dynamictreesfabric.systems.dropcreators.drops.Drops;
import io.github.steveplays28.dynamictreesfabric.systems.dropcreators.drops.StackDrops;
import io.github.steveplays28.dynamictreesfabric.systems.dropcreators.drops.WeightedDrops;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Harley O'Connor
 */
public final class DropsDeserialiser implements JsonDeserialiser<Drops> {

    public static final Map<String, Codec<Drops>> DROPS_TYPES = new HashMap<>();

    @SuppressWarnings("unchecked")
    public static <D extends Drops> void registerCodec(String id, Codec<D> dropsCodec) {
        DROPS_TYPES.put(id, ((Codec<Drops>) dropsCodec));
    }

    static {
        registerCodec("stack", StackDrops.CODEC);
        registerCodec("weighted", WeightedDrops.CODEC);
    }

    @Override
    public Result<Drops, JsonElement> deserialise(JsonElement jsonElement) {
        return JsonDeserialisers.JSON_OBJECT.deserialise(jsonElement).map(object -> {
            final String id = JsonHelper.getOrDefault(object, "id", String.class, null);
            if (id == null) {
                return null;
            }

            final Codec<Drops> codec = DROPS_TYPES.get(id);
            if (codec == null) {
                return null;
            }

            final JsonObject properties = JsonHelper.getOrDefault(object, "properties", JsonObject.class, new JsonObject());
            return codec.decode(JsonOps.INSTANCE, properties)
                    .result().map(Pair::getFirst).orElse(null);
        }, "Error de-serialising drops from element \"" + jsonElement + "\".");
    }

}
