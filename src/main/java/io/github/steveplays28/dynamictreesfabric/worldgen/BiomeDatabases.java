package io.github.steveplays28.dynamictreesfabric.worldgen;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import io.github.steveplays28.dynamictreesfabric.init.DTConfigs;
import org.apache.logging.log4j.LogManager;

import net.minecraft.util.Identifier;
import net.minecraft.util.InvalidIdentifierException;

/**
 * @author Harley O'Connor
 */
public final class BiomeDatabases {

	private static final BiomeDatabase DEFAULT_DATABASE = new BiomeDatabase();
	/**
	 * Dimension names to their respective {@link io.github.steveplays28.dynamictreesfabric.worldgen.BiomeDatabase}.
	 */
	private static final Map<Identifier, BiomeDatabase> DIMENSIONAL_DATABASES = Maps.newHashMap();
	/**
	 * Dimension names for dimensions that are blacklisted.
	 */
	private static final Set<Identifier> BLACKLIST = Sets.newHashSet();

	public static BiomeDatabase getDefault() {
		return DEFAULT_DATABASE;
	}

	public static BiomeDatabase getDimensionalOrDefault(Identifier dimensionLocation) {
		return Optional.ofNullable(DIMENSIONAL_DATABASES.get(dimensionLocation))
				.orElse(DEFAULT_DATABASE);
	}

	public static BiomeDatabase getOrCreateDimensional(Identifier dimensionLocation) {
		return DIMENSIONAL_DATABASES.computeIfAbsent(dimensionLocation, k -> BiomeDatabase.copyOf(DEFAULT_DATABASE));
	}

	public static Map<Identifier, BiomeDatabase> getDimensionalDatabases() {
		return DIMENSIONAL_DATABASES;
	}

	public static boolean isBlacklisted(Identifier dimensionLocation) {
		return BLACKLIST.contains(dimensionLocation);
	}

	public static void populateBlacklistFromConfig() {
		DTConfigs.DIMENSION_BLACKLIST.get().forEach(BiomeDatabases::tryBlacklist);
	}

	private static void tryBlacklist(String location) {
		try {
			BLACKLIST.add(new Identifier(location));
		} catch (InvalidIdentifierException e) {
			LogManager.getLogger().error("Couldn't get location for dimension blacklist in config.", e);
		}
	}

	public static void reset() {
		DEFAULT_DATABASE.reset();
		DIMENSIONAL_DATABASES.clear();
		BLACKLIST.clear();
	}

}
