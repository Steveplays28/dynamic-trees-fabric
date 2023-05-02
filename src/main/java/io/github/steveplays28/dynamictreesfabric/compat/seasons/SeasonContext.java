package io.github.steveplays28.dynamictreesfabric.compat.seasons;

import io.github.steveplays28.dynamictreesfabric.api.seasons.ClimateZoneType;
import io.github.steveplays28.dynamictreesfabric.api.seasons.SeasonGrowthCalculator;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

public class SeasonContext {
    private final SeasonProvider provider;
    private final SeasonGrowthCalculator calculator;
    private float temperateGrowthFactor;
    private float tropicalGrowthFactor;
    private float temperateSeedDropFactor;
    private float tropicalSeedDropFactor;
    private float temperateFruitProductionFactor;
    private float tropicalFruitProductionFactor;
    private Float seasonValue;

    private long methodTicks = 0;

    public SeasonContext(SeasonProvider provider, SeasonGrowthCalculator calculator) {
        this.provider = provider;
        this.calculator = calculator;
    }

    public void updateTick(Level world, long worldTicks) {

        if (methodTicks % 20 == 0) {
            provider.updateTick(world, worldTicks);
            seasonValue = provider.getSeasonValue(world, BlockPos.ZERO);
            temperateGrowthFactor = calculator.calcGrowthRate(seasonValue, ClimateZoneType.TEMPERATE);
            tropicalGrowthFactor = calculator.calcGrowthRate(seasonValue, ClimateZoneType.TROPICAL);
            temperateSeedDropFactor = calculator.calcSeedDropRate(seasonValue, ClimateZoneType.TEMPERATE);
            tropicalSeedDropFactor = calculator.calcSeedDropRate(seasonValue, ClimateZoneType.TROPICAL);
            temperateFruitProductionFactor = calculator.calcFruitProduction(seasonValue, ClimateZoneType.TEMPERATE);
            tropicalFruitProductionFactor = calculator.calcFruitProduction(seasonValue, ClimateZoneType.TROPICAL);
        }

        methodTicks++;
    }

    public SeasonProvider getSeasonProvider() {
        return provider;
    }

    public SeasonGrowthCalculator getCalculator() {
        return calculator;
    }

    public float getTemperateGrowthFactor(float offset) {
        return (offset == 0 || seasonValue == null) ? temperateGrowthFactor : calculator.calcGrowthRate(seasonValue + offset, ClimateZoneType.TEMPERATE);
    }

    public float getTropicalGrowthFactor(float offset) {
        return (offset == 0 || seasonValue == null) ? tropicalGrowthFactor : calculator.calcGrowthRate(seasonValue + offset, ClimateZoneType.TROPICAL);
    }

    public float getTemperateSeedDropFactor(float offset) {
        return (offset == 0 || seasonValue == null) ? temperateSeedDropFactor : calculator.calcSeedDropRate(seasonValue + offset, ClimateZoneType.TEMPERATE);
    }

    public float getTropicalSeedDropFactor(float offset) {
        return (offset == 0 || seasonValue == null) ? tropicalSeedDropFactor : calculator.calcSeedDropRate(seasonValue + offset, ClimateZoneType.TROPICAL);
    }

    public float getTemperateFruitProductionFactor(float offset) {
        return (offset == 0 || seasonValue == null) ? temperateFruitProductionFactor : calculator.calcFruitProduction(seasonValue + offset, ClimateZoneType.TEMPERATE);
    }

    public float getTropicalFruitProductionFactor(float offset) {
        return (offset == 0 || seasonValue == null) ? tropicalFruitProductionFactor : calculator.calcFruitProduction(seasonValue + offset, ClimateZoneType.TROPICAL);
    }

}
