package io.github.steveplays28.dynamictreesfabric.api.configurations;

import io.github.steveplays28.dynamictreesfabric.deserialisation.result.Result;

/**
 * @author Harley O'Connor
 */
public interface ConfigurationTemplate<C extends Configuration<C, ?>> {

    Result<C, ?> apply(PropertiesAccessor properties);

    Iterable<ConfigurationProperty<?>> getRegisteredProperties();

}
