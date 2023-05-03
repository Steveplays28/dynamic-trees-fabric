package io.github.steveplays28.dynamictreesfabric.deserialisation;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import io.github.steveplays28.dynamictreesfabric.deserialisation.result.JsonResult;
import io.github.steveplays28.dynamictreesfabric.deserialisation.result.Result;
import io.github.steveplays28.dynamictreesfabric.util.CommonVoxelShapes;

import net.minecraft.util.math.Box;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;

/**
 * @author Harley O'Connor
 */
public final class VoxelShapeDeserialiser implements JsonDeserialiser<VoxelShape> {

	@Override
	public Result<VoxelShape, JsonElement> deserialise(JsonElement input) {
		return JsonResult.forInput(input)
				.mapIfType(String.class, name ->
						CommonVoxelShapes.SHAPES.getOrDefault(name.toLowerCase(), VoxelShapes.fullCube())
				).elseMapIfType(Box.class, VoxelShapes::cuboid)
				.elseMapIfType(JsonArray.class, array -> {
					VoxelShape shape = VoxelShapes.empty();
					for (JsonElement element : array) {
						shape = VoxelShapes.union(
								JsonDeserialisers.AXIS_ALIGNED_BB.deserialise(element)
										.map(VoxelShapes::cuboid)
										.orElseThrow()
						);
					}
					return shape;
				}).elseTypeError();
	}

}
