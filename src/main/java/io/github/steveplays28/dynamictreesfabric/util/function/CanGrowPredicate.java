package io.github.steveplays28.dynamictreesfabric.util.function;

import java.util.function.BiPredicate;

import io.github.steveplays28.dynamictreesfabric.api.configurations.ConfigurationProperty;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldAccess;

/**
 * A {@link BiPredicate} that tests if something should grow based on the {@link IWorld} and {@link BlockPos}. Mainly
 * used as a {@link ConfigurationProperty}.
 *
 * @author Harley O'Connor
 */
@FunctionalInterface
public interface CanGrowPredicate extends BiPredicate<WorldAccess, BlockPos> {
}
