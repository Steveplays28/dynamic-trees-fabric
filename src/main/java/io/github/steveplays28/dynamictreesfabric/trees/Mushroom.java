package io.github.steveplays28.dynamictreesfabric.trees;

import io.github.steveplays28.dynamictreesfabric.systems.genfeatures.GenFeatures;
import io.github.steveplays28.dynamictreesfabric.systems.genfeatures.HugeMushroomGenFeature;

import net.minecraft.block.Blocks;

public class Mushroom extends Species {

	protected final boolean redcap;

	/**
	 * @param redcap True to select redcap mushroom.  Otherwise brown cap is selected
	 */
	public Mushroom(boolean redcap) {
		this.redcap = redcap;

		this.setRegistryName(io.github.steveplays28.dynamictreesfabric.DynamicTreesFabric.resLoc((redcap ? "red" : "brown") + "_mushroom"));
		this.setUnlocalizedName(this.getRegistryName().toString());
		this.setStandardSoils();

		this.addGenFeature(GenFeatures.HUGE_MUSHROOM.with(HugeMushroomGenFeature.MUSHROOM_BLOCK,
				redcap ? Blocks.RED_MUSHROOM_BLOCK : Blocks.BROWN_MUSHROOM_BLOCK));
	}

	@Override
	public boolean isTransformable() {
		return false;
	}

}
