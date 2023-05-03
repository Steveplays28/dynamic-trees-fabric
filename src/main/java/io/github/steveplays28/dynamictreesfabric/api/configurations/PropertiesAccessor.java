package io.github.steveplays28.dynamictreesfabric.api.configurations;

import org.jetbrains.annotations.Nullable;

/**
 * @author Harley O'Connor
 */
public interface PropertiesAccessor {

	@Nullable
	<V> V get(ConfigurationProperty<V> property);

	boolean has(ConfigurationProperty<?> property);

	void forEach(IterationAction<?> action);

	interface IterationAction<V> {
		void apply(ConfigurationProperty<V> property, V value);
	}

}
