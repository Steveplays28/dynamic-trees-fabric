package io.github.steveplays28.dynamictreesfabric.api.registry;

import io.github.steveplays28.dynamictreesfabric.data.provider.DTBlockStateProvider;
import io.github.steveplays28.dynamictreesfabric.data.provider.DTItemModelProvider;

import net.minecraft.text.ClickEvent;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;

import org.apache.commons.lang3.tuple.Pair;

import java.util.function.Consumer;

/**
 * Holds an entry for a {@link SimpleRegistry}.
 *
 * @author Harley O'Connor
 */
public abstract class RegistryEntry<T extends RegistryEntry<T>> {

	private Identifier registryName;
	private boolean valid = true;
	private boolean generateData;

	protected RegistryEntry() {
	}

	protected RegistryEntry(Identifier registryName) {
		this.registryName = registryName;
	}

	/**
	 * Makes the current entry invalid.
	 *
	 * @return This {@link RegistryEntry}, for calling this in-line.
	 */
	@SuppressWarnings("unchecked")
	protected T nullEntry() {
		this.valid = false;
		return (T) this;
	}

	/**
	 * @return True if the {@link RegistryEntry} is not null.
	 */
	public boolean isValid() {
		return this.valid;
	}

	/**
	 * Calls {@link Consumer#accept(Object)} on the given {@link Consumer} of type {@link T}, only if this {@link
	 * RegistryEntry} is {@code valid}.
	 *
	 * @param consumer The {@link Consumer} of type {@link T}.
	 * @return {@code true} if this {@link RegistryEntry} is {@code valid}; {@code false} otherwise.
	 */
	@SuppressWarnings("unchecked")
	public final boolean ifValid(final Consumer<T> consumer) {
		if (this.isValid()) {
			consumer.accept((T) this);
			return true;
		}
		return false;
	}

	/**
	 * Calls {@link Consumer#accept(Object)} on the given {@link Consumer} of type {@link T}, only if this {@link
	 * RegistryEntry} is {@code valid}. Otherwise calls {@link Runnable#run()} on the given {@link Runnable}.
	 *
	 * @param consumer The {@link Consumer} of type {@link T} to consume if {@code valid}.
	 * @param runnable The {@link Runnable} to run if {@code invalid}.
	 * @return {@code true} if this {@link RegistryEntry} is {@code valid}; {@code false} otherwise.
	 */
	@SuppressWarnings("unchecked")
	public final boolean ifValidElse(final Consumer<T> consumer, final Runnable runnable) {
		if (this.isValid()) {
			consumer.accept((T) this);
			return true;
		}
		runnable.run();
		return false;
	}

	/**
	 * Returns this {@link RegistryEntry} if valid, or otherwise the specified {@link RegistryEntry}.
	 *
	 * @param otherValue The value to return if this {@link RegistryEntry} is invalid.
	 * @return This {@link RegistryEntry} if it is valid; otherwise the specified one.
	 */
	@SuppressWarnings("unchecked")
	public final T elseIfInvalid(final T otherValue) {
		return this.isValid() ? (T) this : otherValue;
	}

	/**
	 * Calls {@link Runnable#run()} on the given {@link Runnable} if this {@link RegistryEntry} is {@code invalid}.
	 *
	 * @param runnable The {@link Runnable} to run if {@code invalid}.
	 * @return {@code true} if this {@link RegistryEntry} is {@code invalid}; {@code false} otherwise.
	 */
	public final boolean ifInvalid(final Runnable runnable) {
		if (!this.isValid()) {
			runnable.run();
			return true;
		}
		return false;
	}

	public boolean shouldGenerateData() {
		return generateData;
	}

	public void setGenerateData(boolean generateData) {
		this.generateData = generateData;
	}

	public void generateStateData(DTBlockStateProvider provider) {
	}

	public void generateItemModelData(DTItemModelProvider provider) {
	}

	public final Identifier getRegistryName() {
		return this.registryName;
	}

	@SuppressWarnings("unchecked")
	public final T setRegistryName(final Identifier registryName) {
		this.registryName = registryName;
		return (T) this;
	}

	public Text getTextComponent() {
		return this.formatComponent(Text.literal(this.getRegistryName().toString()), Formatting.AQUA);
	}

	protected Text formatComponent(final Text component, final Formatting colour) {
		return component.copy().styled(style -> style.withColor(colour)
				.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
						Text.translatable("chat.registry_name", this.getRegistryName())))
				.withClickEvent(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD,
						this.getRegistryName().toString())));
	}

	@Override
	public String toString() {
		return this.getClass().getSimpleName() + "{registryName=" + this.getRegistryName() + "}";
	}

	public String toLoadDataString() {
		return this.toString();
	}

	public String toReloadDataString() {
		return this.toString();
	}

	@SafeVarargs
	public final String getString(final Pair<String, Object>... propertyPairs) {
		final StringBuilder stringBuilder = new StringBuilder(this.getClass().getSimpleName() + "{registryName=" + this.getRegistryName() + ", ");

		for (int i = 0; i < propertyPairs.length; i++) {
			final Pair<String, Object> currentProperty = propertyPairs[i];
			stringBuilder.append(currentProperty.getKey()).append("=").append(currentProperty.getValue()).append(i != propertyPairs.length - 1 ? ", " : "}");
		}

		return stringBuilder.toString();
	}

}
