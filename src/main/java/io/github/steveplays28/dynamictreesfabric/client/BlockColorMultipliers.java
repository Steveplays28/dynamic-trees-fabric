package io.github.steveplays28.dynamictreesfabric.client;

import net.minecraft.client.color.block.BlockColorProvider;
import net.minecraft.util.Identifier;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.HashMap;
import java.util.Map;

@OnlyIn(Dist.CLIENT)
public class BlockColorMultipliers {

	private static Map<String, BlockColorProvider> colorBase = new HashMap<>();

	public static void register(String label, BlockColorProvider colorMultiplier) {
		colorBase.put(label, colorMultiplier);
	}

	public static void register(Identifier label, BlockColorProvider colorMultiplier) {
		colorBase.put(label.toString(), colorMultiplier);
	}

	public static BlockColorProvider find(String label) {
		return colorBase.get(label);
	}

	public static BlockColorProvider find(Identifier label) {
		return colorBase.get(label.toString());
	}

	public static void cleanUp() {
		colorBase = null;//Once all of the color multipliers have been resolved we no longer need this data structure
	}

}
