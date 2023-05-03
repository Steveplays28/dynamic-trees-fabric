package io.github.steveplays28.dynamictreesfabric.data;

import net.minecraft.item.Item;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;

/**
 * @author Harley O'Connor
 */
public final class DTItemTags {

	public static final TagKey<Item> BRANCHES = bind("branches");
	public static final TagKey<Item> BRANCHES_THAT_BURN = bind("branches_that_burn");
	public static final TagKey<Item> FUNGUS_BRANCHES = bind("fungus_branches");

	public static final TagKey<Item> SEEDS = bind("seeds");
	public static final TagKey<Item> FUNGUS_CAPS = bind("fungus_caps");

	/**
	 * Items that apply a growth pulse to trees. By default, includes bone meal.
	 */
	public static final TagKey<Item> FERTILIZER = bind("fertilizer");
	/**
	 * Items that apply the {@link io.github.steveplays28.dynamictreesfabric.systems.substances.GrowthSubstance growth substance}
	 * to trees.
	 */
	public static final TagKey<Item> ENHANCED_FERTILIZER = bind("enhanced_fertilizer");

	// TODO: FABRIC PORT: Check which toString() method is needed
	private static TagKey<Item> bind(String identifier) {
		return ItemTags.of(new Identifier(io.github.steveplays28.dynamictreesfabric.DynamicTreesFabric.MOD_ID, identifier).toString());
	}

}
