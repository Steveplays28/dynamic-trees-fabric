package io.github.steveplays28.dynamictreesfabric.growthlogic;

import io.github.steveplays28.dynamictreesfabric.api.registry.Registry;

public class GrowthLogicKits {

	public static final GrowthLogicKit DARK_OAK = new DarkOakLogic(io.github.steveplays28.dynamictreesfabric.DynamicTreesFabric.resLoc("dark_oak"));
	public static final GrowthLogicKit CONIFER = new ConiferLogic(io.github.steveplays28.dynamictreesfabric.DynamicTreesFabric.resLoc("conifer"));
	public static final GrowthLogicKit JUNGLE = new JungleLogic(io.github.steveplays28.dynamictreesfabric.DynamicTreesFabric.resLoc("jungle"));
	public static final GrowthLogicKit NETHER_FUNGUS = new NetherFungusLogic(io.github.steveplays28.dynamictreesfabric.DynamicTreesFabric.resLoc("nether_fungus"));
	public static final GrowthLogicKit PALM = new PalmGrowthLogic(io.github.steveplays28.dynamictreesfabric.DynamicTreesFabric.resLoc("palm"));

	public static void register(final Registry<GrowthLogicKit> registry) {
		registry.registerAll(DARK_OAK, CONIFER, JUNGLE, NETHER_FUNGUS, PALM);
	}

}
