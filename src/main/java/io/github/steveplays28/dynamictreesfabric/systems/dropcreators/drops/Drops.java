package io.github.steveplays28.dynamictreesfabric.systems.dropcreators.drops;

import java.util.List;

import com.mojang.serialization.Codec;
import net.minecraftforge.registries.ForgeRegistries;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.random.Random;

/**
 * @author Harley O'Connor
 */
@FunctionalInterface
public interface Drops {

	Codec<Item> ITEM_CODEC = ForgeRegistries.ITEMS.getCodec();

	Drops NONE = (drops, random, fortune) -> {
	};

	static int getChance(int fortune, int baseChance) {
		if (baseChance <= 1) {
			return baseChance;
		}
		int chance = baseChance;
		if (fortune > 0) {
			chance -= 10 << fortune;
			if (chance < 40) {
				chance = 40;
			}
		}
		return chance;
	}

	void appendDrops(List<ItemStack> drops, Random random, int fortune);

}
