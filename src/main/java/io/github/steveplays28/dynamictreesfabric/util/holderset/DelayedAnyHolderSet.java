/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package io.github.steveplays28.dynamictreesfabric.util.holderset;

import com.mojang.datafixers.util.Either;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;
import net.minecraft.registry.Registry;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.entry.RegistryEntryList;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.math.random.Random;

public record DelayedAnyHolderSet<T>(Supplier<Registry<T>> registrySupplier) implements RegistryEntryList<T> {
    public Registry<T> registry() {
        return Objects.requireNonNull(this.registrySupplier.get());
    }

    @Override
    public Iterator<RegistryEntry<T>> iterator() {
        return this.stream().iterator();
    }

    @Override
    public Stream<RegistryEntry<T>> stream() {
        return this.registry().streamEntries().map(Function.identity());
    }

    @Override
    public int size() {
        return this.registry().size();
    }

    @Override
    public Either<TagKey<T>, List<RegistryEntry<T>>> getStorage() {
        return Either.right(this.stream().toList());
    }

    @Override
    public Optional<RegistryEntry<T>> getRandom(Random random) {
        return this.registry().getRandom(random);
    }

    @Override
    public RegistryEntry<T> get(int i) {
        return this.registry().getEntry(i).orElseThrow(() -> new NoSuchElementException("No element " + i + " in registry " + this.registry().getKey()));
    }

    @Override
    public boolean contains(RegistryEntry<T> holder) {
        return holder.getKey().map(this.registry()::contains).orElseGet(() -> this.registry().getKey(holder.value()).isPresent());
    }

    @Override
    public boolean isValidInRegistry(Registry<T> registry) {
        return this.registry() == registry;
    }

    @Override
    public String toString() {
        return "AnySet(" + this.registry().getKey() + ")";
    }
}
