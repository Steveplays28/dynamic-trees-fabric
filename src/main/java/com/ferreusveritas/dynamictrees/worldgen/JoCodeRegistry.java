package com.ferreusveritas.dynamictrees.worldgen;

import com.ferreusveritas.dynamictrees.tree.species.Species;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;

import org.jetbrains.annotations.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Harley O'Connor
 */
public final class JoCodeRegistry {

    private JoCodeRegistry() {
    }

    private static final Map<ResourceLocation, Map<Integer, List<JoCode>>> CODES = new HashMap<>();
    private static final Map<ResourceLocation, Map<Integer, List<JoCode>>> ROOTS_CODES = new HashMap<>();

    public static void clear() {
        CODES.clear();
        ROOTS_CODES.clear();
    }

    public static void register(ResourceLocation speciesName, int radius, JoCode code) {
        CODES.computeIfAbsent(speciesName, s -> new HashMap<>())
                .computeIfAbsent(radius, r -> new ArrayList<>()).add(code);
    }

    public static void registerRoot(ResourceLocation speciesName, int radius, RootsJoCode code) {
        ROOTS_CODES.computeIfAbsent(speciesName, s -> new HashMap<>())
                .computeIfAbsent(radius, r -> new ArrayList<>()).add(code);
    }

    /**
     * Returns a map of {@linkplain JoCode JoCodes} under the specified {@code speciesName}, keyed by their respective
     * radii.
     *
     * @param speciesName the name of the {@link Species} to get the codes for
     * @return an unmodifiable map of radii to the list of {@link JoCode} objects for that radius, or an empty map if
     * none were found for the specified {@code speciesName}
     */
    public static Map<Integer, List<JoCode>> getCodes(ResourceLocation speciesName) {
        return getCodes(speciesName, false);
    }
    public static Map<Integer, List<JoCode>> getCodes(ResourceLocation speciesName, boolean root) {
        return Collections.unmodifiableMap((root?ROOTS_CODES:CODES).getOrDefault(speciesName, new HashMap<>()));
    }

    /**
     * Returns a list of codes under the specified {@code speciesName} with the specified {@code radius}.
     *
     * @param speciesName the name of the {@link Species} to get the codes for
     * @param radius      the radius of the codes to return
     * @return an unmodifiable list of {@link JoCode} objects, or an empty list none were found with the specified
     * {@code radius} under the specified {@code speciesName}
     */
    public static List<JoCode> getCodes(ResourceLocation speciesName, int radius) {
        return getCodes(speciesName, radius, false);
    }
    public static List<JoCode> getCodes(ResourceLocation speciesName, int radius, boolean root) {
        return Collections.unmodifiableList(getCodes(speciesName, root).getOrDefault(radius, new ArrayList<>()));
    }

    /**
     * Returns a random code under the specified {@code speciesName} with the specified {@code radius}.
     *
     * @param speciesName the name of the {@link Species} to get the codes for
     * @param radius      the radius of the code to return
     * @param random      the random instance to use
     * @return the randomly selected {@linkplain JoCode}; otherwise {@code null} if there were none to choose from
     */
    @Nullable
    public static JoCode getRandomCode(ResourceLocation speciesName, int radius, RandomSource random) {
        return getRandomCode(speciesName,radius,random,false);
    }
    @Nullable
    public static JoCode getRandomCode(ResourceLocation speciesName, int radius, RandomSource random, boolean root) {
        final List<JoCode> list = getCodes(speciesName, radius, root);

        if (list.isEmpty()) {
            return null;
        }
        return list.get(random.nextInt(list.size()));
    }

}
