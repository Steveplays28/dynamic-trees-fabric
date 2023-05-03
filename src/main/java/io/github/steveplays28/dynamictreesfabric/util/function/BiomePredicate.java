package io.github.steveplays28.dynamictreesfabric.util.function;

import io.github.steveplays28.dynamictreesfabric.api.configurations.ConfigurationProperty;
import java.util.function.Predicate;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.world.biome.Biome;

/**
 * A {@link Predicate} that tests if something should happen in a {@link Biome}. Mainly used as a {@link
 * ConfigurationProperty}.
 *
 * @author Harley O'Connor
 */
@FunctionalInterface
public interface BiomePredicate extends Predicate<RegistryEntry<Biome>> {
}
