package io.github.steveplays28.dynamictreesfabric.systems.dropcreators;

import io.github.steveplays28.dynamictreesfabric.api.registry.Registry;

/**
 * @author Harley O'Connor
 */
public final class DropCreators {

	public static final DropCreator NORMAL = new NormalDropCreator(io.github.steveplays28.dynamictreesfabric.DynamicTreesFabric.resLoc("normal"));
	public static final DropCreator LOOT_TABLE = new LootTableDropCreator(io.github.steveplays28.dynamictreesfabric.DynamicTreesFabric.resLoc("loot_table"));
	public static final DropCreator SEED = new SeedDropCreator(io.github.steveplays28.dynamictreesfabric.DynamicTreesFabric.resLoc("seed"));
	public static final DropCreator FRUIT = new FruitDropCreator(io.github.steveplays28.dynamictreesfabric.DynamicTreesFabric.resLoc("fruit"));
	public static final DropCreator STICK = new StickDropCreator(io.github.steveplays28.dynamictreesfabric.DynamicTreesFabric.resLoc("stick"));
	public static final DropCreator LOG = new LogDropCreator(io.github.steveplays28.dynamictreesfabric.DynamicTreesFabric.resLoc("log"));

	public static void register(final Registry<DropCreator> registry) {
		registry.registerAll(NORMAL, LOOT_TABLE, SEED, FRUIT, STICK, LOG);
	}

}
