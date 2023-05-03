package io.github.steveplays28.dynamictreesfabric.util;

import java.util.Objects;
import java.util.function.Supplier;

import javax.annotation.Nonnull;

/**
 * @author Harley O'Connor
 */
public final class MutableSuppliedLazyValue<T> extends SuppliedLazyValue<T> implements MutableLazyValue<T> {

	public MutableSuppliedLazyValue(Supplier<T> supplier) {
		super(supplier);
	}

	@Override
	public void reset(Supplier<T> supplier) {
		this.supplier = supplier;
		this.object = null;
	}

	@Override
	public void set(@Nonnull T value) {
		Objects.requireNonNull(value);
		this.object = value;
	}

}
