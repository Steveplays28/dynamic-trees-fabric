package io.github.steveplays28.dynamictreesfabric.api.registry;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.fml.ModLoader;
import net.minecraftforge.registries.ForgeRegistry;

import java.util.Comparator;
import java.util.Iterator;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Stream;

/**
 * A custom registry which can be safely unlocked at any point. Largely based off {@link
 * net.minecraftforge.registries.IForgeRegistry}.
 *
 * @author Harley O'Connor
 * @see AbstractRegistry
 * @see SimpleRegistry
 * @see ConcurrentRegistry
 */
public interface Registry<V extends RegistryEntry<V>> extends Iterable<V> {

    /**
     * Registers the given {@link RegistryEntry} to this {@link Registry}.
     *
     * <p>Note that this will throw a runtime exception if this {@link Registry} is locked, or if
     * the {@link ResourceLocation} already has a value registered, therefore {@link #isLocked()} or/and {@link
     * #has(ResourceLocation)} should be checked before calling if either conditions are uncertain.</p>
     *
     * <p>If you're thinking of using this you should probably be doing it from a
     * {@link RegistryEvent}, in which case you don't have to worry about locking.</p>
     *
     * @param value The {@link RegistryEntry} to register.
     * @return This {@link Registry} object for chaining.
     */
    Registry<V> register(V value);

    /**
     * Registers all the given {@link RegistryEntry} to this {@link Registry}. See {@link #register(RegistryEntry)} for
     * more details on the specific registry objects.
     *
     * @param values The {@link RegistryEntry} objects to register.
     */
    @SuppressWarnings("unchecked")
    void registerAll(V... values);

    boolean has(ResourceLocation registryName);

    Optional<V> getOptional(ResourceLocation registryName);

    Optional<V> getOptional(String registryName);

    V get(ResourceLocation registryName);

    V get(String registryName);

    DataResult<V> getAsDataResult(ResourceLocation registryName);

    /**
     * Gets all {@link RegistryEntry} objects currently registered. Note this are obtained as an
     * <b>unmodifiable set</b>, meaning they should only be read from this. For registering values
     * use {@link #register(RegistryEntry)}.
     *
     * @return All {@link RegistryEntry} objects currently registered.
     */
    Set<V> getAll();

    /**
     * Gets all the registry name {@link ResourceLocation} objects of all the entries currently in this {@link
     * Registry}.
     *
     * @return The {@link Set} of registry names.
     */
    Set<ResourceLocation> getRegistryNames();

    Class<V> getType();

    String getName();

    boolean isLocked();

    /**
     * Locks the registries, dumping all registry objects to debug log and running any on lock runnables given to {@link
     * #runOnNextLock(Runnable)}.
     */
    void lock();

    /**
     * Unlocks the registries for modification.
     */
    void unlock();

    /**
     * Runs the {@link Runnable} next time this {@link Registry} is locked.
     *
     * @param runnable The {@link Runnable} to run on lock.
     */
    void runOnNextLock(Runnable runnable);

    void clear();

    Codec<V> getGetterCodec();

    /**
     * Generates a runnable that runs the given {@link Consumer} only if the {@link RegistryEntry} obtained from the
     * given {@link ResourceLocation} is valid (not null), and if it's not runs the given {@link Runnable}.
     *
     * @param registryName The {@link ResourceLocation} of the {@link RegistryEntry}.
     * @param consumer     The {@link Consumer} to accept if the {@link RegistryEntry} is vaid.
     * @param elseRunnable A {@link Runnable} to run if the entry is not valid.
     * @return The generated {@link Runnable}.
     */
    Runnable generateIfValidRunnable(ResourceLocation registryName, Consumer<V> consumer, Runnable elseRunnable);

    /**
     * Posts a {@link RegistryEvent} to the mod event bus. Note that this is posted using {@link
     * ModLoader#postEvent(Event)} and as such should only be called during the initial loading phase.
     */
    void postRegistryEvent();

    /**
     * @return The {@link Comparator} for sorting the registry objects by their registry names in natural order.
     */
    Comparator<V> getComparator();

    /**
     * Dumps all entries with their registry names in the debug log, based off the {@link ForgeRegistry} dump method.
     */
    void dump();

    Set<V> getAllFor(String namespace);

    Stream<V> dataGenerationStream(String namespace);

    @Override
    Iterator<V> iterator();

}
