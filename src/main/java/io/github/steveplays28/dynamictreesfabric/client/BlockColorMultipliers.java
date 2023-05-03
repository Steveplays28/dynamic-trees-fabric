package io.github.steveplays28.dynamictreesfabric.client;

import java.util.HashMap;
import java.util.Map;

import net.minecraft.client.color.block.BlockColorProvider;
import net.minecraft.util.Identifier;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
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
