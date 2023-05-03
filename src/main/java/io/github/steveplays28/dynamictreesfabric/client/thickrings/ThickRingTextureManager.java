package io.github.steveplays28.dynamictreesfabric.client.thickrings;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.util.Identifier;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ThickRingTextureManager {

	/**
	 * THIS IS STILL WIP. THICK RINGS ARE NOT YET STITCHED AUTOMATICALLY
	 */

	public static final Identifier LOCATION_THICKRINGS_TEXTURE = new Identifier(io.github.steveplays28.dynamictreesfabric.DynamicTreesFabric.MOD_ID, "textures/atlas/thick_rings.png");
	private static final BiMap<Identifier, Identifier> thickRingTextures = HashBiMap.create();
	public static ThickRingAtlasTexture textureAtlas;

//	protected static final RenderState.TextureState BRANCHES_SHEET_MIPPED = new RenderState.TextureState(LOCATION_THICKRINGS_TEXTURE, false, true);
//	public static final RenderType BRANCH_SOLID = RenderType.makeType("dynamic_trees_branch_solid", DefaultVertexFormats.BLOCK, 7, 2097152, true, false, RenderType.State.getBuilder().shadeModel(new RenderState.ShadeModelState(true)).lightmap(new RenderState.LightmapState(true)).texture(BRANCHES_SHEET_MIPPED).build(true));
	public static SpriteAtlasTexture.Preparations thickRingData;

	public static Identifier addRingTextureLocation(Identifier ringsRes) {
		Identifier thickRingSet = new Identifier(ringsRes.getNamespace(), ringsRes.getPath() + "_thick");
		thickRingTextures.put(ringsRes, thickRingSet);
		return thickRingSet;
	}

	public static Set<Identifier> getThickRingResourceLocations() {
		return new HashSet<>(thickRingTextures.values());
	}

	public static Set<Map.Entry<Identifier, Identifier>> getThickRingEntrySet() {
		return thickRingTextures.entrySet();
	}

	public static Identifier getThickRingFromBaseRing(Identifier baseRing) {
		return thickRingTextures.get(baseRing);
	}

	public static Identifier getBaseRingFromThickRing(Identifier thickRing) {
		return thickRingTextures.inverse().get(thickRing);
	}

}
