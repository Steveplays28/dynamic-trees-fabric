package io.github.steveplays28.dynamictreesfabric.compat.seasons;

import io.github.steveplays28.dynamictreesfabric.api.seasons.SeasonManager;
import io.github.steveplays28.dynamictreesfabric.compat.CompatHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

import java.util.function.Supplier;

public class SeasonHelper {

    // A season provider returns a float value from 0(Inclusive) to 4(Exclusive) that signifies one of the four classic seasons.
    // A whole number is the beginning of a season with #.5 being the middle of the season.
    public static final float SPRING = 0.0f;
    public static final float SUMMER = 1.0f;
    public static final float AUTUMN = 2.0f;
    public static final float WINTER = 3.0f;

    // Tropical convenience values.
    public static final float DRY = SUMMER;
    public static final float WET = WINTER;

    private static SeasonManager seasonManager = NormalSeasonManager.NULL.get();

    static public SeasonManager getSeasonManager() {
        return seasonManager;
    }

    /**
     * Maybe you don't like the global function season function.  Fine, do it all yourself then!
     *
     * <p>Add-ons should not use this method! {@link CompatHandler#registerSeasonManager(String, Supplier)}
     * should be used to register a season manager for a corresponding mod to respect the preferred season mod
     * configuration option.</p>
     */
    static public void setSeasonManager(SeasonManager manager) {
        seasonManager = manager;
    }

    static public void updateTick(Level world, long worldTicks) {
        seasonManager.updateTick(world, worldTicks);
    }

    static public float globalSeasonalGrowthFactor(Level world, BlockPos rootPos) {
        return globalSeasonalGrowthFactor(world, rootPos, 0);
    }

    static public float globalSeasonalGrowthFactor(Level world, BlockPos rootPos, float offset) {
        return seasonManager.getGrowthFactor(world, rootPos, offset);
    }

    static public float globalSeasonalSeedDropFactor(Level world, BlockPos pos) {
        return globalSeasonalSeedDropFactor(world, pos, 0);
    }

    static public float globalSeasonalSeedDropFactor(Level world, BlockPos pos, float offset) {
        return seasonManager.getSeedDropFactor(world, pos, offset);
    }

    static public float globalSeasonalFruitProductionFactor(Level world, BlockPos pos, boolean getAsScan) {
        return globalSeasonalFruitProductionFactor(world, pos, 0, getAsScan);
    }

    static public float globalSeasonalFruitProductionFactor(Level world, BlockPos pos, float offset, boolean getAsScan) {
        return seasonManager.getFruitProductionFactor(world, pos, offset, getAsScan);
    }

    /**
     * @param world The world
     * @return season value 0.0(Early Spring, Inclusive) -> 4.0(Later Winter, Exclusive) or null if there's no seasons
     * in the world.
     */
    static public Float getSeasonValue(Level world, BlockPos pos) {
        return seasonManager.getSeasonValue(world, pos);
    }

    /**
     * Tests if the position in world is considered tropical and thus follows tropical season rules.
     *
     * @param world
     * @param pos
     * @return
     */
    static public boolean isTropical(Level world, BlockPos pos) {
        return seasonManager.isTropical(world, pos);
    }

    /**
     * Test if the season value falls between two seasonal points. Wraps around the Spring/Winter point(0) if seasonA >
     * seasonB;
     *
     * @param testValue value to test
     * @param SeasonA   chronological season boundary beginning
     * @param SeasonB   chronological season boundary ending
     * @return
     */
    static public boolean isSeasonBetween(Float testValue, float SeasonA, float SeasonB) {

        testValue %= 4.0f;
        SeasonA %= 4.0f;
        SeasonB %= 4.0f;

        if (SeasonA <= SeasonB) {
            return testValue > SeasonA && testValue < SeasonB; //Simply between point A and B(inside)
        } else {
            return testValue < SeasonB || testValue > SeasonA; //The test wraps around the zero point(outside)
        }

    }

    static public boolean shouldSnowMelt(Level world, BlockPos pos) {
        return seasonManager.shouldSnowMelt(world, pos);
    }

}
