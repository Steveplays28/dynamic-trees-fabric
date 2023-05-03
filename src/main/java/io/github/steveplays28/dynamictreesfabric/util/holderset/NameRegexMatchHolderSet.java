package io.github.steveplays28.dynamictreesfabric.util.holderset;

import java.util.function.Supplier;
import java.util.stream.Stream;

import com.mojang.serialization.Codec;
import io.github.steveplays28.dynamictreesfabric.init.DTRegistries;
import net.minecraftforge.registries.holdersets.HolderSetType;
import net.minecraftforge.registries.holdersets.ICustomHolderSet;

import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.entry.RegistryEntry;

public class NameRegexMatchHolderSet<T> extends RegexMatchHolderSet<T> {
	public NameRegexMatchHolderSet(Registry<T> registry, String regex) {
		super(registry, regex);
	}

	public NameRegexMatchHolderSet(Supplier<Registry<T>> registrySupplier, String regex) {
		super(registrySupplier, regex);
	}

	public static <T> Codec<? extends ICustomHolderSet<T>> codec(RegistryKey<? extends Registry<T>> registryKey, Codec<RegistryEntry<T>> holderCodec, boolean forceList) {
		return RegexMatchHolderSet.codec(registryKey, NameRegexMatchHolderSet::new);
	}

	@Override
	protected Stream<String> getInput(RegistryEntry<T> holder) {
		return holder.getKey().stream().map(key -> key.getValue().toString());
	}

	@Override
	public HolderSetType type() {
		return DTRegistries.NAME_REGEX_MATCH_HOLDER_SET_TYPE.get();
	}
}
