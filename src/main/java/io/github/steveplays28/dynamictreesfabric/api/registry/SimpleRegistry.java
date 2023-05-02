package io.github.steveplays28.dynamictreesfabric.api.registry;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistry;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * A custom registry which can be safely unlocked at any point. Largely based off {@link ForgeRegistry}.
 *
 * @param <V> The {@link RegistryEntry} type that will be registered.
 * @author Harley O'Connor
 * @see ConcurrentRegistry
 */
public class SimpleRegistry<V extends RegistryEntry<V>> extends AbstractRegistry<V> {

    /**
     * The {@link Set} of {@link RegistryEntry} objects currently registered.
     */
    private final Set<V> entries = new LinkedHashSet<>();

    /**
     * Constructs a new {@link SimpleRegistry} with the name being set to {@link Class#getSimpleName()} of the given {@link
     * RegistryEntry}.
     *
     * @param type      The {@link Class} of the {@link RegistryEntry}.
     * @param nullValue A null entry. See {@link #nullValue} for more details.
     */
    public SimpleRegistry(final Class<V> type, final V nullValue) {
        this(type.getSimpleName(), type, nullValue);
    }

    /**
     * Constructs a new {@link SimpleRegistry}.
     *
     * @param name      The {@link #name} for this {@link SimpleRegistry}.
     * @param type      The {@link Class} of the {@link RegistryEntry}.
     * @param nullValue A null entry. See {@link #nullValue} for more details.
     */
    public SimpleRegistry(final String name, final Class<V> type, final V nullValue) {
        this(name, type, nullValue, false);
    }

    /**
     * Constructs a new {@link SimpleRegistry} with the name being set to {@link Class#getSimpleName()} of the given {@link
     * RegistryEntry}.
     *
     * @param type      The {@link Class} of the {@link RegistryEntry}.
     * @param nullValue A null entry. See {@link #nullValue} for more details.
     * @param clearable True if {@link #clear()} can be called to wipe the registry.
     */
    public SimpleRegistry(final Class<V> type, final V nullValue, final boolean clearable) {
        this(type.getSimpleName(), type, nullValue, clearable);
    }

    /**
     * Constructs a new {@link SimpleRegistry}.
     *
     * @param name      The {@link #name} for this {@link SimpleRegistry}.
     * @param type      The {@link Class} of the {@link RegistryEntry}.
     * @param nullValue A null entry. See {@link #nullValue} for more details.
     * @param clearable True if {@link #clear()} can be called to wipe the registry.
     */
    public SimpleRegistry(final String name, final Class<V> type, final V nullValue, final boolean clearable) {
        super(name, type, nullValue, clearable);
        this.register(nullValue);
    }

    /**
     * Registers the given {@link RegistryEntry} to this {@link SimpleRegistry}.
     *
     * <p>Note that this will throw a runtime exception if this {@link SimpleRegistry} is locked, or if
     * the {@link ResourceLocation} already has a value registered, therefore {@link #isLocked()} or/and {@link
     * #has(ResourceLocation)} should be checked before calling if either conditions are uncertain.</p>
     *
     * <p>If you're thinking of using this you should probably be doing it from a
     * {@link RegistryEvent}, in which case you don't have to worry about locking.</p>
     *
     * @param value The {@link RegistryEntry} to register.
     * @return This {@link SimpleRegistry} object for chaining.
     */
    @Override
    public SimpleRegistry<V> register(final V value) {
        this.assertValid(value);
        this.entries.add(value);
        return this;
    }

    /**
     * Gets all {@link RegistryEntry} objects currently registered. Note this are obtained as an
     * <b>unmodifiable set</b>, meaning they should only be read from this. For registering values
     * use {@link #register(RegistryEntry)}.
     *
     * @return All {@link RegistryEntry} objects currently registered.
     */
    @Override
    public final Set<V> getAll() {
        return Collections.unmodifiableSet(this.entries);
    }

    @Override
    protected void clearAll() {
        this.entries.clear();
    }

}
