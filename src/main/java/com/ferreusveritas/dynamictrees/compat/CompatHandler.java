package com.ferreusveritas.dynamictrees.compat;

import com.ferreusveritas.dynamictrees.DynamicTrees;
import com.ferreusveritas.dynamictrees.compat.seasons.ActiveSeasonGrowthCalculator;
import com.ferreusveritas.dynamictrees.compat.seasons.BetterWeatherSeasonProvider;
import com.ferreusveritas.dynamictrees.compat.seasons.NormalSeasonManager;
import com.ferreusveritas.dynamictrees.compat.seasons.NullSeasonGrowthCalculator;
import com.ferreusveritas.dynamictrees.compat.seasons.NullSeasonProvider;
import com.ferreusveritas.dynamictrees.compat.seasons.SeasonHelper;
import com.ferreusveritas.dynamictrees.compat.seasons.SereneSeasonsSeasonProvider;
import com.ferreusveritas.dynamictrees.init.DTConfigs;
import com.google.common.collect.Maps;
import corgitaco.betterweather.api.season.Season;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Tuple;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;
import net.minecraftforge.fml.ModList;
import org.apache.logging.log4j.LogManager;
import sereneseasons.config.BiomeConfig;
import sereneseasons.config.SeasonsConfig;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;

/**
 * @author Harley O'Connor
 */
public final class CompatHandler {

    private static final LinkedHashMap<String, Supplier<NormalSeasonManager>> SEASON_MANAGERS = Maps.newLinkedHashMap();

    /**
     * Registers the specified {@link NormalSeasonManager} supplier for the specified {@code modId}. Given as a supplier for
     * lazy initialisation.
     *
     * <p>The season manager to use is then selected by {@link DTConfigs#PREFERRED_SEASON_MOD}
     * on config reload.</p>
     *
     * @param modId    The mod ID the season manager handles.
     * @param supplier The {@link NormalSeasonManager} supplier.
     */
    public static void registerSeasonManager(final String modId, Supplier<NormalSeasonManager> supplier) {
        SEASON_MANAGERS.put(modId, supplier);
    }

    public static void registerBuiltInSeasonManagers() {
        registerSeasonManager(DynamicTrees.SERENE_SEASONS, () -> {
            NormalSeasonManager seasonManager = new NormalSeasonManager(
                    world -> SeasonsConfig.isDimensionWhitelisted(world.dimension()) ?
                            new Tuple<>(new SereneSeasonsSeasonProvider(), new ActiveSeasonGrowthCalculator()) :
                            new Tuple<>(new NullSeasonProvider(), new NullSeasonGrowthCalculator())
            );
            seasonManager.setTropicalPredicate((world, pos) -> {
                final ResourceLocation registryName = world.getBiome(pos).getRegistryName();
                return registryName != null && BiomeConfig.usesTropicalSeasons(RegistryKey.create(Registry.BIOME_REGISTRY, registryName));
            });
            return seasonManager;
        });

        registerSeasonManager(DynamicTrees.BETTER_WEATHER, () -> new NormalSeasonManager(
                world -> Season.getSeason(world) == null ?
                        new Tuple<>(new NullSeasonProvider(), new NullSeasonGrowthCalculator()) :
                        new Tuple<>(new BetterWeatherSeasonProvider(), new ActiveSeasonGrowthCalculator())
        ));
    }

    public static final String DISABLED = "!";
    public static final String ANY = "*";

    public static void reloadSeasonManager() {
        final String modId = DTConfigs.PREFERRED_SEASON_MOD.get();

        // If disabled, use null manager.
        if (Objects.equals(modId, DISABLED)) {
            SeasonHelper.setSeasonManager(NormalSeasonManager.NULL.get());
            return;
        }

        // If any, select first manager registered.
        if (Objects.equals(modId, ANY)) {
            SeasonHelper.setSeasonManager(
                    SEASON_MANAGERS.entrySet().stream()
                            .filter(entry -> ModList.get().isLoaded(entry.getKey()))
                            .map(Map.Entry::getValue)
                            .findFirst()
                            .orElse(NormalSeasonManager.NULL)
                            .get()
            );
            return;
        }

        if (!ModList.get().isLoaded(modId)) {
            LogManager.getLogger().warn("Preferred season mod \"{}\" not installed.", modId);
            return;
        }

        if (!SEASON_MANAGERS.containsKey(modId)) {
            LogManager.getLogger().warn("Season manager not found for preferred season mod \"{}\".", modId);
            return;
        }

        SeasonHelper.setSeasonManager(SEASON_MANAGERS.get(modId).get());
    }

}
