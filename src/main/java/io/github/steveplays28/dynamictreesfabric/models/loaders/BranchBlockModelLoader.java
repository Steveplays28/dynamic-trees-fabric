package io.github.steveplays28.dynamictreesfabric.models.loaders;

import javax.annotation.Nullable;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import io.github.steveplays28.dynamictreesfabric.api.TreeRegistry;
import io.github.steveplays28.dynamictreesfabric.models.geometry.BranchBlockModelGeometry;
import net.minecraftforge.client.model.geometry.IGeometryLoader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.minecraft.client.texture.MissingSprite;
import net.minecraft.util.Identifier;
import net.minecraft.util.InvalidIdentifierException;

/**
 * Loads a branch block model from a Json file, with useful warnings when things aren't found.
 *
 * <p>Can also be used by sub-classes to load other models, like for roots in
 * {@link RootBlockModelLoader}.</p>
 *
 * @author Harley O'Connor
 */
public class BranchBlockModelLoader implements IGeometryLoader<BranchBlockModelGeometry> {

	public static final Logger LOGGER = LogManager.getLogger();

	private static final String TEXTURES = "textures";
	private static final String BARK = "bark";
	private static final String RINGS = "rings";

	@Override
	public BranchBlockModelGeometry read(JsonObject modelObject, JsonDeserializationContext deserializationContext) throws JsonParseException {
		final JsonObject textures = this.getTexturesObject(modelObject);
		final Identifier familyResLoc = this.getResLoc(modelObject);

		return this.getModelGeometry(this.getBarkResLoc(textures), this.getRingsResLoc(textures),
				familyResLoc == null ? null : TreeRegistry.processResLoc(familyResLoc));
	}

	protected JsonObject getTexturesObject(final JsonObject modelContents) {
		if (!modelContents.has(TEXTURES) || !modelContents.get(TEXTURES).isJsonObject()) {
			this.throwRequiresElement(TEXTURES, "Json Object");
		}

		return modelContents.getAsJsonObject(TEXTURES);
	}

	protected Identifier getBarkResLoc(final JsonObject textureObject) {
		return this.getTextureLocation(textureObject, BARK);
	}

	protected Identifier getRingsResLoc(final JsonObject textureObject) {
		return this.getTextureLocation(textureObject, RINGS);
	}

	@Nullable
	protected Identifier getResLoc(final JsonObject object) {
		try {
			return this.getResLocOrThrow(this.getOrThrow(object, "family"));
		} catch (final RuntimeException e) {
			return null;
		}
	}

	protected Identifier getTextureLocation(final JsonObject textureObject, final String textureElement) {
		try {
			return this.getResLocOrThrow(this.getOrThrow(textureObject, textureElement));
		} catch (final RuntimeException e) {
			LOGGER.error("{} missing or did not have valid \"{}\" texture location element, using missing " +
					"texture.", this.getModelTypeName(), textureElement);
			return MissingSprite.getMissingSpriteId();
		}
	}

	protected String getOrThrow(final JsonObject jsonObject, final String identifier) {
		if (jsonObject.get(identifier) == null || !jsonObject.get(identifier).isJsonPrimitive() ||
				!jsonObject.get(identifier).getAsJsonPrimitive().isString()) {
			this.throwRequiresElement(identifier, "String");
		}

		return jsonObject.get(identifier).getAsString();
	}

	protected void throwRequiresElement(final String element, final String expectedType) {
		throw new RuntimeException(this.getModelTypeName() + " requires a valid \"" + element + "\" element of " +
				"type " + expectedType + ".");
	}

	protected Identifier getResLocOrThrow(final String resLocStr) {
		try {
			return new Identifier(resLocStr);
		} catch (InvalidIdentifierException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * @return The type of model the class is loading. Useful for warnings when using sub-classes.
	 */
	protected String getModelTypeName() {
		return "Branch";
	}

	/**
	 * Gets the {@link BranchBlockModelGeometry} object from the given bark and rings texture locations.
	 * Can be overridden by subclasses to provide their custom {@link BranchBlockModelGeometry}.
	 *
	 * @param barkResLoc  The {@link Identifier} object for the bark.
	 * @param ringsResLoc The {@link Identifier} object for the rings.
	 * @return The {@link BranchBlockModelGeometry} object.
	 */
	protected BranchBlockModelGeometry getModelGeometry(final Identifier barkResLoc,
	                                                    final Identifier ringsResLoc,
	                                                    @Nullable final Identifier familyResLoc) {
		return new BranchBlockModelGeometry(barkResLoc, ringsResLoc, familyResLoc, false);
	}

}
