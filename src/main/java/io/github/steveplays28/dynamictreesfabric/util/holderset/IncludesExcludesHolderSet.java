package io.github.steveplays28.dynamictreesfabric.util.holderset;

import io.github.steveplays28.dynamictreesfabric.init.DTRegistries;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.entry.RegistryEntryList;
import net.minecraft.registry.entry.RegistryEntryListCodec;
import net.minecraftforge.registries.holdersets.CompositeHolderSet;
import net.minecraftforge.registries.holdersets.HolderSetType;
import net.minecraftforge.registries.holdersets.ICustomHolderSet;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class IncludesExcludesHolderSet<T> extends CompositeHolderSet<T> {
    public static <T> Codec<? extends ICustomHolderSet<T>> codec(RegistryKey<? extends Registry<T>> registryKey, Codec<RegistryEntry<T>> holderCodec, boolean forceList) {
        Codec<RegistryEntryList<T>> holderSetCodec = RegistryEntryListCodec.create(registryKey, holderCodec, forceList);
        return RecordCodecBuilder.<IncludesExcludesHolderSet<T>>create(builder -> builder.group(
                holderSetCodec.fieldOf("includes").forGetter(IncludesExcludesHolderSet::includes),
                holderSetCodec.fieldOf("excludes").forGetter(IncludesExcludesHolderSet::excludes)
        ).apply(builder, IncludesExcludesHolderSet::new));
    }

    private final RegistryEntryList<T> includes;
    private final RegistryEntryList<T> excludes;

    public IncludesExcludesHolderSet(RegistryEntryList<T> includes, RegistryEntryList<T> excludes) {
        super(List.of(includes, excludes));
        this.includes = includes;
        this.excludes = excludes;
    }

    public RegistryEntryList<T> includes() {
        return this.includes;
    }

    public List<RegistryEntryList<T>> getIncludeComponents() {
        return this.includes instanceof CompositeHolderSet<T> compositeHolderSet ? compositeHolderSet.getComponents() : null;
    }

    public List<RegistryEntryList<T>> getExcludeComponents() {
        return this.excludes instanceof CompositeHolderSet<T> compositeHolderSet ? compositeHolderSet.getComponents() : null;
    }

    public void clear() {
        this.getIncludeComponents().clear();
        this.getExcludeComponents().clear();
    }

    public RegistryEntryList<T> excludes() {
        return this.excludes;
    }

    @Override
    protected Set<RegistryEntry<T>> createSet() {
        return this.includes.stream().filter(holder -> !this.excludes.contains(holder)).collect(Collectors.toSet());
    }

    @Override
    public HolderSetType type() {
        return DTRegistries.INCLUDES_EXCLUDES_HOLDER_SET_TYPE.get();
    }
}
