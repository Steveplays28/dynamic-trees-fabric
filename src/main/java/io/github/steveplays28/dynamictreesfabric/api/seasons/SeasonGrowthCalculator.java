package io.github.steveplays28.dynamictreesfabric.api.seasons;

/**
 * Provides a means to calculate temperate and tropical growth rate values.
 *
 * @author ferreusveritas
 */
public interface SeasonGrowthCalculator {

    float calcGrowthRate(Float seasonValue, ClimateZoneType type);

    float calcSeedDropRate(Float seasonValue, ClimateZoneType type);

    float calcFruitProduction(Float seasonValue, ClimateZoneType type);

}
