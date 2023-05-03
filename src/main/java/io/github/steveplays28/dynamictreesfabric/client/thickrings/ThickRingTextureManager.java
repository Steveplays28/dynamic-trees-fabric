package io.github.steveplays28.dynamictreesfabric.client.thickrings;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.util.Identifier;

public class ThickRingTextureManager {

    /**
     * THIS IS STILL WIP. THICK RINGS ARE NOT YET STITCHED AUTOMATICALLY
     */

    public static final Identifier LOCATION_THICKRINGS_TEXTURE = new Identifier(io.github.steveplays28.dynamictreesfabric.DynamicTreesFabric.MOD_ID, "textures/atlas/thick_rings.png");

    public static ThickRingAtlasTexture textureAtlas;
    public static SpriteAtlasTexture.Preparations thickRingData;

//	protected static final RenderState.TextureState BRANCHES_SHEET_MIPPED = new RenderState.TextureState(LOCATION_THICKRINGS_TEXTURE, false, true);
//	public static final RenderType BRANCH_SOLID = RenderType.makeType("dynamic_trees_branch_solid", DefaultVertexFormats.BLOCK, 7, 2097152, true, false, RenderType.State.getBuilder().shadeModel(new RenderState.ShadeModelState(true)).lightmap(new RenderState.LightmapState(true)).texture(BRANCHES_SHEET_MIPPED).build(true));

    private static final BiMap<Identifier, Identifier> thickRingTextures = HashBiMap.create();

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
