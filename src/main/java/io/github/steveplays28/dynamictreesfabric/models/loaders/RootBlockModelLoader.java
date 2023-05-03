package io.github.steveplays28.dynamictreesfabric.models.loaders;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import io.github.steveplays28.dynamictreesfabric.models.geometry.BranchBlockModelGeometry;
import io.github.steveplays28.dynamictreesfabric.models.geometry.RootBlockModelGeometry;

/**
 * @author Harley O'Connor
 */
@Environment(EnvType.CLIENT)
public class RootBlockModelLoader extends BranchBlockModelLoader {

	@Override
	public BranchBlockModelGeometry read(JsonObject modelObject, JsonDeserializationContext deserializationContext) {
		final JsonObject textures = this.getTexturesObject(modelObject);
		return new RootBlockModelGeometry(this.getBarkResLoc(textures));
	}

	@Override
	protected String getModelTypeName() {
		return "Surface Root";
	}

}
