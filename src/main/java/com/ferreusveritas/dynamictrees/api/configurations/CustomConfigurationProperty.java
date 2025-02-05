package com.ferreusveritas.dynamictrees.api.configurations;

/**
 * @author Harley O'Connor
 */
public final class CustomConfigurationProperty<V> extends ConfigurationProperty<V> {

    CustomConfigurationProperty(String key, Class<V> type) {
        super(key, type);
    }

}
