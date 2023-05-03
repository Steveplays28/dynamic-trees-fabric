package io.github.steveplays28.dynamictreesfabric.util.holderset;

import java.util.function.BiFunction;
import java.util.function.Supplier;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraftforge.registries.holdersets.ICustomHolderSet;

import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryOps;
import net.minecraft.registry.entry.RegistryEntry;

public abstract class RegexMatchHolderSet<T> extends StreamBackedHolderSet<T> implements ICustomHolderSet<T> {
	private final Supplier<Registry<T>> registrySupplier;
	private final String regex;
	private Pattern pattern;
	public RegexMatchHolderSet(Registry<T> registry, String regex) {
		this(() -> registry, regex);
	}

	public RegexMatchHolderSet(Supplier<Registry<T>> registrySupplier, String regex) {
		this.registrySupplier = registrySupplier;
		this.regex = regex;
	}

	protected static <T> Codec<? extends ICustomHolderSet<T>> codec(RegistryKey<? extends Registry<T>> registryKey, BiFunction<Registry<T>, String, RegexMatchHolderSet<T>> factory) {
		return RecordCodecBuilder.<RegexMatchHolderSet<T>>create(builder -> builder.group(
				RegistryOps.retrieveRegistry(registryKey).forGetter(RegexMatchHolderSet::registry),
				Codec.STRING.fieldOf("regex").forGetter(RegexMatchHolderSet::regex)
		).apply(builder, factory));
	}

	public final Supplier<Registry<T>> registrySupplier() {
		return this.registrySupplier;
	}

	public Registry<T> registry() {
		return this.registrySupplier.get();
	}

	public final String regex() {
		return this.regex;
	}

	private Pattern getPattern() {
		if (this.pattern == null) {
			this.pattern = Pattern.compile(this.regex);
		}

		return this.pattern;
	}

	@SuppressWarnings("unchecked")
	@Override
	public Stream<RegistryEntry<T>> stream() {
		return (Stream<RegistryEntry<T>>) (Stream<?>) this.registry().streamEntries().filter(holder -> this.getInput(holder).anyMatch(input -> this.getPattern().matcher(input).matches()));
	}

	/**
	 * Gets the stream of input data from the holder to use for regex matching.
	 * If any string matches, the holder will be included in the set.
	 */
	protected abstract Stream<String> getInput(RegistryEntry<T> holder);
}
