package io.github.steveplays28.dynamictreesfabric.util.holderset;

import java.util.function.Supplier;
import net.minecraft.registry.Registry;
import net.minecraft.registry.entry.RegistryEntryList;
import net.minecraft.registry.tag.TagKey;

public class DelayedTagEntriesHolderSet<T> extends RegistryEntryList.Named<T> {
    private final Supplier<Registry<T>> registrySupplier;

    public DelayedTagEntriesHolderSet(Supplier<Registry<T>> registrySupplier, TagKey<T> key) {
        super(null, key);
        this.registrySupplier = registrySupplier;
    }

    @Override
    public boolean isValidInRegistry(Registry<T> registry) {
        return this.registrySupplier.get() == registry;
    }
}
