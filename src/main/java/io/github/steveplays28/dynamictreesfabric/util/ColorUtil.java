package io.github.steveplays28.dynamictreesfabric.util;

import net.minecraft.util.math.ColorHelper;

public class ColorUtil {
	public static int decodeARGB32(String rgbString) throws NumberFormatException {
		int packedColor = Integer.decode(rgbString);

		return ColorHelper.Argb.getArgb(0xFF, ColorHelper.Argb.getRed(packedColor), ColorHelper.Argb.getGreen(packedColor), ColorHelper.Argb.getBlue(packedColor));
	}
}
