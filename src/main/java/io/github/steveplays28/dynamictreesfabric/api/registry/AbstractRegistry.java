package io.github.steveplays28.dynamictreesfabric.api.registry;

import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.Nonnull;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import io.github.steveplays28.dynamictreesfabric.api.TreeRegistry;
import io.github.steveplays28.dynamictreesfabric.util.CommonCollectors;
import net.minecraftforge.fml.ModLoader;
import net.minecraftforge.registries.ForgeRegistry;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;

import net.minecraft.util.Identifier;

/**
 * A skeletal implementation of {@link Registry}.
 *
 * @author Harley O'Connor
 * @see Registry
 * @see SimpleRegistry
 * @see ConcurrentRegistry
 */
public abstract class AbstractRegistry<V extends RegistryEntry<V>> implements Registry<V> {

	protected static final Logger LOGGER = LogManager.getLogger();
	protected static final Marker REGISTRY_DUMP = MarkerManager.getMarker("REGISTRY_DUMP");
	protected static final Comparator<String> STRING_COMPARATOR = Comparator.naturalOrder();

	protected final Class<V> type;

	/**
	 * The name of this {@link Registry}. This will usually be obtained from calling {@link Class#getSimpleName()} on
	 * the {@link RegistryEntry}, but some registries may choose to use custom names.
	 */
	protected final String name;

	/**
	 * The "null" value. This is what will be returned by {@link #get(Identifier)} if the entry was not found in
	 * the registry.
	 */
	protected final V nullValue;

	/**
	 * If this {@link Registry} is clearable, {@link #clear()} can be called, which wipes all the values and locks the
	 * registry.
	 */
	protected final boolean clearable;

	protected final Codec<V> getterCodec;

	/**
	 * A {@link List} of runnables that will be called on the next {@link #lock()} call. Allows for things to be run
	 * once all registries are "final" (at least for the time being). Note that these will be cleared after use (every
	 * time the registry is locked).
	 */
	protected final List<Runnable> onLockRunnables = new LinkedList<>();

	/**
	 * Comparator for sorting the {@link RegistryEntry} objects by their registry names (in natural order).
	 */
	protected final Comparator<V> comparator = (entry, entryToCompareTo) -> STRING_COMPARATOR
			.compare(entry.getRegistryName().getPath(), entryToCompareTo.getRegistryName().getPath());

	/**
	 * Holds whether or not the {@link Registry} is currently locked. This is false (unlocked) by default, and should
	 * then be locked after all initial registries are created by {@link #postRegistryEvent()}.
	 *
	 * <p>It can then be unlocked by calling {@link #unlock()} to register new values, but should
	 * always be locked after by calling {@link #lock()} again, which performs additional tasks like {@link
	 * #dump()}.</p>
	 */
	protected boolean locked = false;

	/**
	 * Constructs a new {@link AbstractRegistry}.
	 *
	 * @param name      The {@link #name} for this {@link AbstractRegistry}.
	 * @param type      The {@link Class} of the {@link RegistryEntry}.
	 * @param nullValue A null entry. See {@link #nullValue} for more details.
	 * @param clearable True if {@link #clear()} can be called to wipe the registry.
	 */
	protected AbstractRegistry(final String name, final Class<V> type, final V nullValue, final boolean clearable) {
		this.type = type;
		this.name = name;
		this.nullValue = nullValue.nullEntry();
		this.clearable = clearable;
		this.getterCodec = Identifier.CODEC.comapFlatMap(this::getAsDataResult, RegistryEntry::getRegistryName);
	}

	protected void assertValid(final V value) {
		final Identifier registryName = value.getRegistryName();

		if (this.locked) {
			throw new RuntimeException(this.getErrorMessage(value, registryName, " to locked registry "));
		}

		if (this.has(registryName)) {
			throw new RuntimeException(this.getErrorMessage(value, registryName, " that already had a value registered in registry "));
		}
	}

	protected String getErrorMessage(final V value, final Identifier registryName, final String message) {
		return "Tried to register '" + value + "' under registry name '" + registryName + "' " + message + " '" + this.name + "'.";
	}

	/**
	 * Registers all the given {@link RegistryEntry} to this {@link Registry}. See {@link #register(RegistryEntry)} for
	 * more details on the specific registry objects.
	 *
	 * @param values The {@link RegistryEntry} objects to register.
	 */
	@Override
	@SafeVarargs
	public final void registerAll(final V... values) {
		for (final V value : values) {
			register(value);
		}
	}

	@Override
	public final Optional<V> getOptional(final String registryName) {
		return this.getOptional(Identifier.tryParse(registryName));
	}

	@Override
	public final V get(final Identifier registryName) {
		return this.getOptional(registryName).orElse(this.nullValue);
	}

	@Override
	public final V get(final String registryName) {
		return this.get(Identifier.tryParse(registryName));
	}

	@Override
	public final DataResult<V> getAsDataResult(final Identifier registryName) {
		return this.getOptional(TreeRegistry.processResLoc(registryName)).map(DataResult::success)
				.orElse(DataResult.error("Could not find " + this.name + " '" + registryName + "'."));
	}

	@Override
	public final boolean has(final Identifier registryName) {
		return this.getAll().stream()
				.map(RegistryEntry::getRegistryName)
				.anyMatch(registryName::equals);
	}

	@Override
	public final Optional<V> getOptional(final Identifier registryName) {
		return this.getAll().stream()
				.filter(entry -> entry.getRegistryName().equals(registryName))
				.findFirst();
	}

	/**
	 * Gets all the registry name {@link Identifier} objects of all the entries currently in this {@link
	 * Registry}.
	 *
	 * @return The {@link Set} of registry names.
	 */
	@Override
	public final Set<Identifier> getRegistryNames() {
		return this.getAll().stream().map(RegistryEntry::getRegistryName).collect(Collectors.toSet());
	}

	@Override
	public final Class<V> getType() {
		return type;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public final boolean isLocked() {
		return locked;
	}

	/**
	 * Locks the registries, dumping all registry objects to debug log and running any {@link #onLockRunnables}.
	 */
	@Override
	public final void lock() {
		this.locked = true;
		this.dump();

		// Run all of the on lock runnables and then clear them.
		this.onLockRunnables.forEach(Runnable::run);
		this.onLockRunnables.clear();
	}

	/**
	 * Unlocks the registries for modification.
	 */
	@Override
	public final void unlock() {
		this.locked = false;
	}

	/**
	 * Runs the {@link Runnable} next time this {@link Registry} is locked.
	 *
	 * @param runnable The {@link Runnable} to run on lock.
	 */
	@Override
	public final void runOnNextLock(final Runnable runnable) {
		this.onLockRunnables.add(runnable);
	}

	/**
	 * Clears the registry of all values, only if it is {@link #clearable}.
	 */
	@Override
	public final void clear() {
		if (!this.clearable) {
			return;
		}

		this.lock();
		this.clearAll();
	}

	/**
	 * Clears all values. This method should <b>not</b> check {@link #clearable}, that should be checked before
	 * calling!
	 */
	protected abstract void clearAll();

	@Override
	public final Codec<V> getGetterCodec() {
		return getterCodec;
	}

	/**
	 * Generates a runnable that runs the given {@link Consumer} only if the {@link RegistryEntry} obtained from the
	 * given {@link Identifier} is valid (not null), and if it's not runs the given {@link Runnable}.
	 *
	 * @param registryName The {@link Identifier} of the {@link RegistryEntry}.
	 * @param consumer     The {@link Consumer} to accept if the {@link RegistryEntry} is vaid.
	 * @param elseRunnable A {@link Runnable} to run if the entry is not valid.
	 * @return The generated {@link Runnable}.
	 */
	@Override
	public final Runnable generateIfValidRunnable(final Identifier registryName, final Consumer<V> consumer, final Runnable elseRunnable) {
		return () -> {
			if (!this.get(registryName).ifValid(consumer)) {
				elseRunnable.run();
			}
		};
	}

	/**
	 * Posts a {@link RegistryEvent} to the mod event bus for any programmatic registration. Should only be called once
	 * and during game start.
	 */
	@Override
	public void postRegistryEvent() {
		ModLoader.get().postEvent(new RegistryEvent<>(this));
	}

	/**
	 * @return The {@link #comparator} for sorting the registry objects by their registry names in natural order.
	 */
	@Override
	public final Comparator<V> getComparator() {
		return comparator;
	}

	/**
	 * Dumps all entries with their registry names in the debug log, based off the {@link ForgeRegistry} dump method.
	 */
	@Override
	public final void dump() {
		if (!LOGGER.isDebugEnabled(REGISTRY_DUMP))
			return;

		StringBuilder builder = new StringBuilder();
		this.getAll().stream().sorted(this.comparator).forEach(entry -> builder.append("\tEntry: ")
				.append(entry.getRegistryName()).append(", ").append(entry).append('\n'));
		builder.append("Name: ").append(this.name).append('\n');
		LOGGER.debug(REGISTRY_DUMP, builder.toString());
	}

	@Override
	public final Set<V> getAllFor(final String namespace) {
		return this.getAll().stream()
				.filter(entry -> entry.getRegistryName().getNamespace().equals(namespace))
				.collect(CommonCollectors.toUnmodifiableLinkedSet());
	}

	@Override
	public Stream<V> dataGenerationStream(String namespace) {
		return this.getAllFor(namespace).stream()
				.filter(RegistryEntry::shouldGenerateData);
	}

	@Nonnull
	@Override
	public final Iterator<V> iterator() {
		return this.getAll().iterator();
	}

}
