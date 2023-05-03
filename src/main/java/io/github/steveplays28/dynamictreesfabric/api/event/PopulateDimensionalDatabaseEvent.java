package io.github.steveplays28.dynamictreesfabric.api.event;

import io.github.steveplays28.dynamictreesfabric.worldgen.BiomeDatabase;

import net.minecraft.util.Identifier;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.Event;

import java.util.Map;

/**
 * An {@link Event} for populating dimensional databases programmatically. This is posted after all default populators
 * and dimensional populators from Json have been registered.
 *
 * <p>Fired on the {@link MinecraftForge#EVENT_BUS}.</p>
 *
 * @author Harley O'Connor
 * @deprecated biome database population will happen solely from Json in the future
 */
@Deprecated
public final class PopulateDimensionalDatabaseEvent extends Event {

	private final Map<Identifier, BiomeDatabase> dimensionalMap;
	private final BiomeDatabase defaultDatabase;

	public PopulateDimensionalDatabaseEvent(final Map<Identifier, BiomeDatabase> dimensionalMap, final BiomeDatabase defaultDatabase) {
		this.dimensionalMap = dimensionalMap;
		this.defaultDatabase = defaultDatabase;
	}

	public BiomeDatabase getDimensionDatabase(final Identifier dimensionRegistryName) {
		return dimensionalMap.computeIfAbsent(dimensionRegistryName, k -> BiomeDatabase.copyOf(this.defaultDatabase));
	}

}
