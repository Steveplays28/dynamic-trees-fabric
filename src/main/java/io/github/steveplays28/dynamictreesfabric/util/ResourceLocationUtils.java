package io.github.steveplays28.dynamictreesfabric.util;

import net.minecraft.util.Identifier;

/**
 * @author Harley O'Connor
 */
public final class ResourceLocationUtils {

	public static Identifier parse(String string, final String defaultNamespace) {
		if (!string.contains(":")) {
			string = defaultNamespace + ":" + string;
		}
		return new Identifier(string);
	}

	public static Identifier namespace(final Identifier resourceLocation, final String namespace) {
		return new Identifier(namespace, resourceLocation.getPath());
	}

	public static Identifier prefix(final Identifier resourceLocation, final String prefix) {
		return new Identifier(resourceLocation.getNamespace(), prefix + resourceLocation.getPath());
	}

	public static Identifier suffix(final Identifier resourceLocation, final String suffix) {
		return new Identifier(resourceLocation.getNamespace(), resourceLocation.getPath() + suffix);
	}

	public static Identifier surround(final Identifier resourceLocation, final String prefix, final String suffix) {
		return new Identifier(resourceLocation.getNamespace(), prefix + resourceLocation.getPath() + suffix);
	}

}
