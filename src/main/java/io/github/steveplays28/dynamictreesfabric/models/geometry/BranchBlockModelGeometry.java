package io.github.steveplays28.dynamictreesfabric.models.geometry;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.jetbrains.annotations.Nullable;

import com.mojang.datafixers.util.Pair;
import io.github.steveplays28.dynamictreesfabric.client.thickrings.ThickRingTextureManager;
import io.github.steveplays28.dynamictreesfabric.models.bakedmodels.BasicBranchBlockBakedModel;
import io.github.steveplays28.dynamictreesfabric.models.bakedmodels.ThickBranchBlockBakedModel;
import io.github.steveplays28.dynamictreesfabric.trees.Family;
import net.minecraftforge.client.model.geometry.IGeometryBakingContext;
import net.minecraftforge.client.model.geometry.IUnbakedGeometry;

import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.ModelBakeSettings;
import net.minecraft.client.render.model.ModelLoader;
import net.minecraft.client.render.model.UnbakedModel;
import net.minecraft.client.render.model.json.ModelOverrideList;
import net.minecraft.client.resources.model.*;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.client.util.SpriteIdentifier;
import net.minecraft.util.Identifier;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public class BranchBlockModelGeometry implements IUnbakedGeometry<BranchBlockModelGeometry> {

	protected final Set<Identifier> textures = new HashSet<>();
	protected final Identifier barkResLoc;
	protected final Identifier ringsResLoc;
	protected final boolean forceThickness;

	protected Identifier familyResLoc;
	protected Family family;

	protected Identifier thickRingsResLoc;

	public BranchBlockModelGeometry(@Nullable final Identifier barkResLoc, @Nullable final Identifier ringsResLoc, @Nullable final Identifier familyResLoc, final boolean forceThickness) {
		this.barkResLoc = barkResLoc;
		this.ringsResLoc = ringsResLoc;
		this.familyResLoc = familyResLoc;
		this.forceThickness = forceThickness;

		this.addTextures(barkResLoc, ringsResLoc);
	}

	/**
	 * Adds the given texture {@link Identifier} objects to the list. Checks they're not null before adding them
	 * so {@link Nullable} objects can be fed safely.
	 *
	 * @param textureResourceLocations Texture {@link Identifier} objects.
	 */
	protected void addTextures(final Identifier... textureResourceLocations) {
		for (Identifier resourceLocation : textureResourceLocations) {
			if (resourceLocation != null) {
				this.textures.add(resourceLocation);
			}
		}
	}

	@Override
	public BakedModel bake(IGeometryBakingContext owner, ModelLoader bakery, Function<SpriteIdentifier, Sprite> spriteGetter, ModelBakeSettings modelTransform, ModelOverrideList overrides, Identifier modelLocation) {
		if (!this.useThickModel(this.setFamily(modelLocation))) {
			return new BasicBranchBlockBakedModel(modelLocation, this.barkResLoc, this.ringsResLoc);
		} else {
			return new ThickBranchBlockBakedModel(modelLocation, this.barkResLoc, this.ringsResLoc, this.thickRingsResLoc);
		}
	}

	private Identifier setFamilyResLoc(final Identifier modelResLoc) {
		if (this.familyResLoc == null) {
			this.familyResLoc = new Identifier(modelResLoc.getNamespace(), modelResLoc.getPath().replace("block/", "").replace("_branch", "").replace("stripped_", ""));
		}
		return this.familyResLoc;
	}

	private Family setFamily(final Identifier modelResLoc) {
		if (this.family == null) {
			this.family = Family.REGISTRY.get(this.setFamilyResLoc(modelResLoc));
		}
		return this.family;
	}

	private boolean useThickModel(final Family family) {
		return this.forceThickness || family.isThick();
	}

	@SuppressWarnings("deprecation")
	@Override
	public Collection<SpriteIdentifier> getMaterials(IGeometryBakingContext owner, Function<Identifier, UnbakedModel> modelGetter, Set<Pair<String, String>> missingTextureErrors) {
		if (this.thickRingsResLoc == null && this.useThickModel(this.setFamily(new Identifier(owner.getModelName())))) {
			this.thickRingsResLoc = ThickRingTextureManager.addRingTextureLocation(this.ringsResLoc);
			this.addTextures(this.thickRingsResLoc);
		}

		return this.textures.stream()
				.map(resourceLocation -> new SpriteIdentifier(SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE, resourceLocation))
				.collect(Collectors.toList());
	}

}
