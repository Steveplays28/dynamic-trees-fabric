package io.github.steveplays28.dynamictreesfabric.util.holderset;

import com.mojang.datafixers.util.Either;

import net.minecraft.registry.Registry;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.entry.RegistryEntryList;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Util;
import net.minecraft.util.math.random.Random;

import java.util.*;
import java.util.stream.Collectors;

public abstract class StreamBackedHolderSet<T> implements RegistryEntryList<T> {
	public List<RegistryEntry<T>> contents() {
		return this.stream().collect(Collectors.toList());
	}

	public Set<RegistryEntry<T>> contentsSet() {
		return this.stream().collect(Collectors.toSet());
	}

	public int size() {
		return this.contents().size();
	}

	public Spliterator<RegistryEntry<T>> spliterator() {
		return this.stream().spliterator();
	}

	public Iterator<RegistryEntry<T>> iterator() {
		return this.stream().iterator();
	}

	public Optional<RegistryEntry<T>> getRandom(Random random) {
		return Util.getRandomOrEmpty(this.contents(), random);
	}

	public RegistryEntry<T> get(int index) {
		return this.contents().get(index);
	}

	public boolean isValidInRegistry(Registry<T> registry) {
		return true;
	}

	@Override
	public Either<TagKey<T>, List<RegistryEntry<T>>> getStorage() {
		return Either.right(this.contents());
	}

	@Override
	public boolean contains(RegistryEntry<T> holder) {
		return this.stream().anyMatch(h -> Objects.equals(h, holder));
	}
}
