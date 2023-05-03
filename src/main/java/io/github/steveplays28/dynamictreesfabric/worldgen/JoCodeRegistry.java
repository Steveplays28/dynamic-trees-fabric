package io.github.steveplays28.dynamictreesfabric.worldgen;

import io.github.steveplays28.dynamictreesfabric.trees.Species;
import javax.annotation.Nullable;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.random.Random;
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

    private static final Map<Identifier, Map<Integer, List<JoCode>>> CODES = new HashMap<>();

    public static void clear() {
        CODES.clear();
    }

    public static void register(Identifier speciesName, int radius, JoCode code) {
        CODES.computeIfAbsent(speciesName, s -> new HashMap<>())
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
    public static Map<Integer, List<JoCode>> getCodes(Identifier speciesName) {
        return Collections.unmodifiableMap(CODES.getOrDefault(speciesName, new HashMap<>()));
    }

    /**
     * Returns a list of codes under the specified {@code speciesName} with the specified {@code radius}.
     *
     * @param speciesName the name of the {@link Species} to get the codes for
     * @param radius      the radius of the codes to return
     * @return an unmodifiable list of {@link JoCode} objects, or an empty list none were found with the specified
     * {@code radius} under the specified {@code speciesName}
     */
    public static List<JoCode> getCodes(Identifier speciesName, int radius) {
        return Collections.unmodifiableList(getCodes(speciesName).getOrDefault(radius, new ArrayList<>()));
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
    public static JoCode getRandomCode(Identifier speciesName, int radius, Random random) {
        final List<JoCode> list = getCodes(speciesName, radius);

        if (list.isEmpty()) {
            return null;
        }

        return list.get(random.nextInt(list.size()));
    }

}
