package io.github.steveplays28.dynamictreesfabric.models.loaders;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import io.github.steveplays28.dynamictreesfabric.models.geometry.BranchBlockModelGeometry;
import io.github.steveplays28.dynamictreesfabric.models.geometry.RootBlockModelGeometry;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/**
 * @author Harley O'Connor
 */
@OnlyIn(Dist.CLIENT)
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
