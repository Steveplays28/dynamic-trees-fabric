package io.github.steveplays28.dynamictreesfabric.util.holderset;

import java.util.ArrayList;

import net.minecraftforge.registries.holdersets.AndHolderSet;

import net.minecraft.world.biome.Biome;

public class DTBiomeHolderSet extends IncludesExcludesHolderSet<Biome> {
	public DTBiomeHolderSet() {
		super(new AndHolderSet<>(new ArrayList<>()), new AndHolderSet<>(new ArrayList<>()));
	}
}
