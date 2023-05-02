package io.github.steveplays28.dynamictreesfabric.systems.dropcreators;

import io.github.steveplays28.dynamictreesfabric.api.configurations.Configuration;
import io.github.steveplays28.dynamictreesfabric.api.configurations.TemplateRegistry;
import io.github.steveplays28.dynamictreesfabric.systems.dropcreators.context.DropContext;

/**
 * @author Harley O'Connor
 */
public final class DropCreatorConfiguration extends Configuration<DropCreatorConfiguration, DropCreator> {

    public static final TemplateRegistry<DropCreatorConfiguration> TEMPLATES = new TemplateRegistry<>();

    public DropCreatorConfiguration(DropCreator dropCreator) {
        super(dropCreator);
    }

    /**
     * {@inheritDoc}
     *
     * @return The copy of this {@link DropCreatorConfiguration}.
     */
    @Override
    public DropCreatorConfiguration copy() {
        final DropCreatorConfiguration duplicateGenFeature = new DropCreatorConfiguration(this.configurable);
        duplicateGenFeature.properties.putAll(this.properties);
        return duplicateGenFeature;
    }

    public <C extends DropContext> void appendDrops(DropCreator.Type<C> type, C context) {
        this.configurable.appendDrops(this, type, context);
    }

    public static DropCreatorConfiguration getNull() {
        return DropCreator.NULL.getDefaultConfiguration();
    }

}
