package io.github.steveplays28.dynamictreesfabric.data;

import io.github.steveplays28.dynamictreesfabric.trees.Species;

import net.minecraft.loot.context.LootContextParameter;

/**
 * @author Harley O'Connor
 */
public final class DTLootParameters {
	public static final LootContextParameter<Species> SPECIES = create("species");
	public static final LootContextParameter<Integer> FERTILITY = create("fertility");
	public static final LootContextParameter<Integer> FORTUNE = create("fortune");
	public static final LootContextParameter<Species.LogsAndSticks> LOGS_AND_STICKS = create("logs_and_sticks");

	private static <T> LootContextParameter<T> create(String path) {
		return new LootContextParameter<>(io.github.steveplays28.dynamictreesfabric.DynamicTreesFabric.resLoc(path));
	}
}
