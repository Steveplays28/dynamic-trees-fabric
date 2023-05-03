package io.github.steveplays28.dynamictreesfabric.deserialisation;

import java.util.HashMap;
import java.util.Map;

import com.google.gson.JsonElement;
import io.github.steveplays28.dynamictreesfabric.deserialisation.result.Result;

import net.minecraft.block.Material;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;

/**
 * @author Harley O'Connor
 */
public final class MaterialDeserialiser implements JsonDeserialiser<Material> {

	private static final Map<Identifier, Material> MATERIALS = Util.make(new HashMap<>(), materials -> {
		materials.put(new Identifier("air"), Material.AIR);
		materials.put(new Identifier("structural_air"), Material.STRUCTURE_VOID);
		materials.put(new Identifier("portal"), Material.PORTAL);
		materials.put(new Identifier("cloth_decoration"), Material.CARPET);
		materials.put(new Identifier("plant"), Material.PLANT);
		materials.put(new Identifier("water_plant"), Material.UNDERWATER_PLANT);
		materials.put(new Identifier("replaceable_plant"), Material.REPLACEABLE_PLANT);
		materials.put(new Identifier("replaceable_fireproof_plant"), Material.NETHER_SHOOTS);
		materials.put(new Identifier("replaceable_water_plant"), Material.REPLACEABLE_UNDERWATER_PLANT);
		materials.put(new Identifier("water"), Material.WATER);
		materials.put(new Identifier("bubble_column"), Material.BUBBLE_COLUMN);
		materials.put(new Identifier("lava"), Material.LAVA);
		materials.put(new Identifier("top_snow"), Material.SNOW_LAYER);
		materials.put(new Identifier("fire"), Material.FIRE);
		materials.put(new Identifier("decoration"), Material.DECORATION);
		materials.put(new Identifier("web"), Material.COBWEB);
		materials.put(new Identifier("buildable_glass"), Material.REDSTONE_LAMP);
		materials.put(new Identifier("clay"), Material.ORGANIC_PRODUCT);
		materials.put(new Identifier("dirt"), Material.SOIL);
		materials.put(new Identifier("grass"), Material.SOLID_ORGANIC);
		materials.put(new Identifier("ice_solid"), Material.DENSE_ICE);
		materials.put(new Identifier("sand"), Material.AGGREGATE);
		materials.put(new Identifier("sponge"), Material.SPONGE);
		materials.put(new Identifier("shulker_shell"), Material.SHULKER_BOX);
		materials.put(new Identifier("wood"), Material.WOOD);
		materials.put(new Identifier("nether_wood"), Material.NETHER_WOOD);
		materials.put(new Identifier("bamboo_sapling"), Material.BAMBOO_SAPLING);
		materials.put(new Identifier("bamboo"), Material.BAMBOO);
		materials.put(new Identifier("wool"), Material.WOOL);
		materials.put(new Identifier("explosive"), Material.TNT);
		materials.put(new Identifier("leaves"), Material.LEAVES);
		materials.put(new Identifier("glass"), Material.GLASS);
		materials.put(new Identifier("ice"), Material.ICE);
		materials.put(new Identifier("cactus"), Material.CACTUS);
		materials.put(new Identifier("stone"), Material.STONE);
		materials.put(new Identifier("metal"), Material.METAL);
		materials.put(new Identifier("snow"), Material.SNOW_BLOCK);
		materials.put(new Identifier("heavy_metal"), Material.REPAIR_STATION);
		materials.put(new Identifier("barrier"), Material.BARRIER);
		materials.put(new Identifier("piston"), Material.PISTON);
//        materials.put(new ResourceLocation("coral"), Material.CORAL);
		materials.put(new Identifier("vegetable"), Material.GOURD);
		materials.put(new Identifier("egg"), Material.EGG);
		materials.put(new Identifier("cake"), Material.CAKE);
	});

	/**
	 * Registers given material under the given name, if that name is not already taken.
	 *
	 * @param name     the name to register the material under
	 * @param material the material to register
	 */
	public static void registerMaterial(Identifier name, Material material) {
		MATERIALS.putIfAbsent(name, material);
	}

	@Override
	public Result<Material, JsonElement> deserialise(JsonElement input) {
		return JsonDeserialisers.RESOURCE_LOCATION.deserialise(input)
				.map(MATERIALS::get, "Could not get material from \"{}\".");
	}
}
