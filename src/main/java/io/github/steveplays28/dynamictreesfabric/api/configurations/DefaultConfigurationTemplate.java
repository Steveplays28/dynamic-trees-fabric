package io.github.steveplays28.dynamictreesfabric.api.configurations;

import io.github.steveplays28.dynamictreesfabric.deserialisation.result.JsonResult;
import io.github.steveplays28.dynamictreesfabric.deserialisation.result.Result;
import com.google.gson.JsonNull;

import java.util.Collections;

/**
 * @author Harley O'Connor
 */
public final class DefaultConfigurationTemplate<C extends Configuration<C, ?>> implements ConfigurationTemplate<C> {

    private final C defaultConfiguration;
    private final Iterable<ConfigurationProperty<?>> registeredProperties;

    public DefaultConfigurationTemplate(C defaultConfiguration, Configurable configurable) {
        this.defaultConfiguration = defaultConfiguration;
        this.registeredProperties = Collections.unmodifiableSet(configurable.getRegisteredProperties());
    }

    @Override
    public Result<C, ?> apply(PropertiesAccessor properties) {
        return JsonResult.success(JsonNull.INSTANCE, this.defaultConfiguration.copy().withAll(properties));
    }

    @Override
    public Iterable<ConfigurationProperty<?>> getRegisteredProperties() {
        return this.registeredProperties;
    }

}
