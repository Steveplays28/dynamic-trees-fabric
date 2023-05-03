package io.github.steveplays28.dynamictreesfabric.util.holderset;

import com.mojang.serialization.Codec;
import io.github.steveplays28.dynamictreesfabric.init.DTRegistries;

import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.entry.RegistryEntry;

import net.minecraftforge.registries.holdersets.HolderSetType;
import net.minecraftforge.registries.holdersets.ICustomHolderSet;

import java.util.function.Supplier;
import java.util.stream.Stream;

public class TagsRegexMatchHolderSet<T> extends RegexMatchHolderSet<T> {
	public TagsRegexMatchHolderSet(Registry<T> registry, String regex) {
		super(registry, regex);
	}

	public TagsRegexMatchHolderSet(Supplier<Registry<T>> registrySupplier, String regex) {
		super(registrySupplier, regex);
	}

	public static <T> Codec<? extends ICustomHolderSet<T>> codec(RegistryKey<? extends Registry<T>> registryKey, Codec<RegistryEntry<T>> holderCodec, boolean forceList) {
		return RegexMatchHolderSet.codec(registryKey, TagsRegexMatchHolderSet::new);
	}

	@Override
	protected Stream<String> getInput(RegistryEntry<T> holder) {
		return holder.streamTags().map(tagKey -> tagKey.id().toString());
	}

	@Override
	public HolderSetType type() {
		return DTRegistries.TAGS_REGEX_MATCH_HOLDER_SET_TYPE.get();
	}
}
