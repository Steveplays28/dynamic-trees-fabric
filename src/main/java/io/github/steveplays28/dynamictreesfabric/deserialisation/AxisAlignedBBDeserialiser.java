package io.github.steveplays28.dynamictreesfabric.deserialisation;

import com.google.gson.JsonElement;
import io.github.steveplays28.dynamictreesfabric.deserialisation.result.Result;

import net.minecraft.util.math.Box;

/**
 * @author Harley O'Connor
 */
public final class AxisAlignedBBDeserialiser implements JsonDeserialiser<Box> {

	@Override
	public Result<Box, JsonElement> deserialise(JsonElement jsonElement) {
		return JsonDeserialisers.JSON_ARRAY.deserialise(jsonElement).map((jsonArray, warningConsumer) -> {
			if (jsonArray.size() != 6) {
				throw DeserialisationException.error("Array was not of correct size (6).");
			}

			final double[] params = new double[6];

			for (int i = 0; i < jsonArray.size(); i++) {
				params[i] = JsonDeserialisers.DOUBLE.deserialise(jsonArray.get(i)).orElseThrow();
			}

			return new Box(params[0], params[1], params[2], params[3], params[4], params[5]);
		});
	}

}
