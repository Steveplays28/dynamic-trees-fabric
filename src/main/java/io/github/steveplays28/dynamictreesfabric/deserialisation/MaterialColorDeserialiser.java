package io.github.steveplays28.dynamictreesfabric.deserialisation;

import java.util.HashMap;
import java.util.Map;

import com.google.gson.JsonElement;
import io.github.steveplays28.dynamictreesfabric.deserialisation.result.Result;

import net.minecraft.block.MapColor;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;

/**
 * @author Harley O'Connor
 */
public final class MaterialColorDeserialiser implements JsonDeserialiser<MapColor> {

	private static final Map<Identifier, MapColor> MATERIAL_COLORS =
			Util.make(new HashMap<>(), materialColors -> {
				materialColors.put(new Identifier("none"), MapColor.CLEAR);
				materialColors.put(new Identifier("grass"), MapColor.PALE_GREEN);
				materialColors.put(new Identifier("sand"), MapColor.PALE_YELLOW);
				materialColors.put(new Identifier("wool"), MapColor.WHITE_GRAY);
				materialColors.put(new Identifier("fire"), MapColor.BRIGHT_RED);
				materialColors.put(new Identifier("ice"), MapColor.PALE_PURPLE);
				materialColors.put(new Identifier("metal"), MapColor.IRON_GRAY);
				materialColors.put(new Identifier("plant"), MapColor.DARK_GREEN);
				materialColors.put(new Identifier("snow"), MapColor.WHITE);
				materialColors.put(new Identifier("clay"), MapColor.LIGHT_BLUE_GRAY);
				materialColors.put(new Identifier("dirt"), MapColor.DIRT_BROWN);
				materialColors.put(new Identifier("stone"), MapColor.STONE_GRAY);
				materialColors.put(new Identifier("water"), MapColor.WATER_BLUE);
				materialColors.put(new Identifier("wood"), MapColor.OAK_TAN);
				materialColors.put(new Identifier("quartz"), MapColor.OFF_WHITE);
				materialColors.put(new Identifier("color_orange"), MapColor.ORANGE);
				materialColors.put(new Identifier("color_magenta"), MapColor.MAGENTA);
				materialColors.put(new Identifier("color_light_blue"), MapColor.LIGHT_BLUE);
				materialColors.put(new Identifier("color_yellow"), MapColor.YELLOW);
				materialColors.put(new Identifier("color_light_green"), MapColor.LIME);
				materialColors.put(new Identifier("color_pink"), MapColor.PINK);
				materialColors.put(new Identifier("color_gray"), MapColor.GRAY);
				materialColors.put(new Identifier("color_light_gray"), MapColor.LIGHT_GRAY);
				materialColors.put(new Identifier("color_cyan"), MapColor.CYAN);
				materialColors.put(new Identifier("color_purple"), MapColor.PURPLE);
				materialColors.put(new Identifier("color_blue"), MapColor.BLUE);
				materialColors.put(new Identifier("color_brown"), MapColor.BROWN);
				materialColors.put(new Identifier("color_green"), MapColor.GREEN);
				materialColors.put(new Identifier("color_red"), MapColor.RED);
				materialColors.put(new Identifier("color_black"), MapColor.BLACK);
				materialColors.put(new Identifier("gold"), MapColor.GOLD);
				materialColors.put(new Identifier("diamond"), MapColor.DIAMOND_BLUE);
				materialColors.put(new Identifier("lapis"), MapColor.LAPIS_BLUE);
				materialColors.put(new Identifier("emerald"), MapColor.EMERALD_GREEN);
				materialColors.put(new Identifier("podzol"), MapColor.SPRUCE_BROWN);
				materialColors.put(new Identifier("nether"), MapColor.DARK_RED);
				materialColors.put(new Identifier("terracotta_white"), MapColor.TERRACOTTA_WHITE);
				materialColors.put(new Identifier("terracotta_orange"), MapColor.TERRACOTTA_ORANGE);
				materialColors.put(new Identifier("terracotta_magenta"), MapColor.TERRACOTTA_MAGENTA);
				materialColors.put(new Identifier("terracotta_light_blue"), MapColor.TERRACOTTA_LIGHT_BLUE);
				materialColors.put(new Identifier("terracotta_yellow"), MapColor.TERRACOTTA_YELLOW);
				materialColors.put(new Identifier("terracotta_light_green"),
						MapColor.TERRACOTTA_LIME);
				materialColors.put(new Identifier("terracotta_pink"), MapColor.TERRACOTTA_PINK);
				materialColors.put(new Identifier("terracotta_gray"), MapColor.TERRACOTTA_GRAY);
				materialColors.put(new Identifier("terracotta_light_gray"), MapColor.TERRACOTTA_LIGHT_GRAY);
				materialColors.put(new Identifier("terracotta_cyan"), MapColor.TERRACOTTA_CYAN);
				materialColors.put(new Identifier("terracotta_purple"), MapColor.TERRACOTTA_PURPLE);
				materialColors.put(new Identifier("terracotta_blue"), MapColor.TERRACOTTA_BLUE);
				materialColors.put(new Identifier("terracotta_brown"), MapColor.TERRACOTTA_BROWN);
				materialColors.put(new Identifier("terracotta_green"), MapColor.TERRACOTTA_GREEN);
				materialColors.put(new Identifier("terracotta_red"), MapColor.TERRACOTTA_RED);
				materialColors.put(new Identifier("terracotta_black"), MapColor.TERRACOTTA_BLACK);
				materialColors.put(new Identifier("crimson_nylium"), MapColor.DULL_RED);
				materialColors.put(new Identifier("crimson_stem"), MapColor.DULL_PINK);
				materialColors.put(new Identifier("crimson_hyphae"), MapColor.DARK_CRIMSON);
				materialColors.put(new Identifier("warped_nylium"), MapColor.TEAL);
				materialColors.put(new Identifier("warped_stem"), MapColor.DARK_AQUA);
				materialColors.put(new Identifier("warped_hyphae"), MapColor.DARK_DULL_PINK);
				materialColors.put(new Identifier("warped_wart_block"), MapColor.BRIGHT_TEAL);
			});

	/**
	 * Registers given material color under the given name, if that name is not already taken.
	 *
	 * @param name          the name to register the material color under
	 * @param materialColor the material color to register
	 */
	public static void registerMaterialColor(Identifier name, MapColor materialColor) {
		MATERIAL_COLORS.putIfAbsent(name, materialColor);
	}

	@Override
	public Result<MapColor, JsonElement> deserialise(JsonElement input) {
		return JsonDeserialisers.RESOURCE_LOCATION.deserialise(input)
				.map(MATERIAL_COLORS::get, "Could not get material color from \"{}\".");
	}
}
