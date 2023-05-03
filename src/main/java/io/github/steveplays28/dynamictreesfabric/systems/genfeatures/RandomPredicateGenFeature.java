package io.github.steveplays28.dynamictreesfabric.systems.genfeatures;

import io.github.steveplays28.dynamictreesfabric.api.configurations.ConfigurationProperty;
import io.github.steveplays28.dynamictreesfabric.systems.genfeatures.context.PostGenerationContext;
import io.github.steveplays28.dynamictreesfabric.systems.genfeatures.context.PostGrowContext;
import io.github.steveplays28.dynamictreesfabric.util.CoordUtils;
import net.minecraft.util.Identifier;

public class RandomPredicateGenFeature extends GenFeature {

    public static final ConfigurationProperty<Boolean> ONLY_WORLD_GEN = ConfigurationProperty.bool("only_world_gen");
    public static final ConfigurationProperty<GenFeatureConfiguration> GEN_FEATURE = ConfigurationProperty.property("gen_feature", GenFeatureConfiguration.class);

    public RandomPredicateGenFeature(Identifier registryName) {
        super(registryName);
    }

    @Override
    protected void registerProperties() {
        this.register(PLACE_CHANCE, GEN_FEATURE, ONLY_WORLD_GEN);
    }

    @Override
    protected GenFeatureConfiguration createDefaultConfiguration() {
        return super.createDefaultConfiguration()
                .with(PLACE_CHANCE, 0.5f)
                .with(GEN_FEATURE, GenFeatureConfiguration.getNull())
                .with(ONLY_WORLD_GEN, false);
    }

    @Override
    protected boolean postGenerate(GenFeatureConfiguration configuration, PostGenerationContext context) {
        if (configuration.get(ONLY_WORLD_GEN) && !context.isWorldGen() ||
                Math.abs(CoordUtils.coordHashCode(context.pos(), 2) / (float) 0xFFFF) > configuration.get(PLACE_CHANCE)) {
            // If the chance is not met, do nothing.
            return false;
        }

        final GenFeatureConfiguration configurationToPlace = configuration.get(GEN_FEATURE);
        return configuration.getGenFeature().isValid() &&
                configurationToPlace.getGenFeature().postGenerate(configurationToPlace, context);
    }

    @Override
    protected boolean postGrow(GenFeatureConfiguration configuration, PostGrowContext context) {
        if (configuration.get(ONLY_WORLD_GEN)
                || Math.abs(CoordUtils.coordHashCode(context.pos(), 2) / (float) 0xFFFF) > configuration.get(PLACE_CHANCE)) {
            // If the chance is not met, or its only for world gen, do nothing.
            return false;
        }

        final GenFeatureConfiguration configurationToPlace = configuration.get(GEN_FEATURE);
        return configuration.getGenFeature().isValid() &&
                configurationToPlace.getGenFeature().postGrow(configurationToPlace, context);
    }

}
