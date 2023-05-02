package io.github.steveplays28.dynamictreesfabric.worldgen;

import io.github.steveplays28.dynamictreesfabric.api.worldgen.BiomePropertySelectors;
import io.github.steveplays28.dynamictreesfabric.util.holderset.DTBiomeHolderSet;

import java.util.ArrayList;
import java.util.List;

public class FeatureCancellationRegistry {
    private static final List<Entry> CANCELLATIONS = new ArrayList<>();

    public static void addCancellations(DTBiomeHolderSet biomes, BiomeDatabase.Operation operation, BiomePropertySelectors.FeatureCancellations cancellations) {
        CANCELLATIONS.add(new Entry(biomes, operation, cancellations));
    }

    public static List<Entry> getCancellations() {
        return CANCELLATIONS;
    }

    public record Entry(DTBiomeHolderSet biomes, BiomeDatabase.Operation operation, BiomePropertySelectors.FeatureCancellations cancellations) {}
}
