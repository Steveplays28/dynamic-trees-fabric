package io.github.steveplays28.dynamictreesfabric.systems.substances;

import io.github.steveplays28.dynamictreesfabric.api.substances.SubstanceEffect;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.function.Supplier;

/**
 * Holds lingering {@link SubstanceEffect} classes and a {@link Supplier} to instantiate them with. Stored in a {@link
 * LinkedHashMap} so that {@link io.github.steveplays28.dynamictreesfabric.entities.LingeringEffectorEntity} can send the relevant
 * substance as an index in a packet.
 *
 * @author Harley O'Connor
 */
@SuppressWarnings("unchecked")
public final class LingeringSubstances {

    public static final LinkedHashMap<Class<SubstanceEffect>, Supplier<SubstanceEffect>> LINGERING_SUBSTANCES = new LinkedHashMap<>();

    public static <S extends SubstanceEffect> void registerLingeringSubstance(final Class<S> substanceClass, Supplier<S> substanceSupplier) {
        LINGERING_SUBSTANCES.put((Class<SubstanceEffect>) substanceClass, (Supplier<SubstanceEffect>) substanceSupplier);
    }

    public static <S extends SubstanceEffect> int indexOf(final Class<S> substanceClass) {
        return new ArrayList<>(LINGERING_SUBSTANCES.keySet()).indexOf(substanceClass);
    }

    public static <S extends SubstanceEffect> Supplier<S> fromIndex(final int index) {
        return (Supplier<S>) new ArrayList<>(LINGERING_SUBSTANCES.values()).get(index);
    }

    static {
        // It's important that the order in which these are added is equal on the client and server.
        registerLingeringSubstance(GrowthSubstance.class, GrowthSubstance::new);
        registerLingeringSubstance(HarvestSubstance.class, HarvestSubstance::new);
    }

}
