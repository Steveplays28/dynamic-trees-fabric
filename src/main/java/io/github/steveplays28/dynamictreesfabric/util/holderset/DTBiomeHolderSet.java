package io.github.steveplays28.dynamictreesfabric.util.holderset;

import net.minecraft.world.biome.Biome;
import net.minecraftforge.registries.holdersets.AndHolderSet;

import java.util.ArrayList;

public class DTBiomeHolderSet extends IncludesExcludesHolderSet<Biome> {
    public DTBiomeHolderSet() {
        super(new AndHolderSet<>(new ArrayList<>()), new AndHolderSet<>(new ArrayList<>()));
    }
}
