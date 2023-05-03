package io.github.steveplays28.dynamictreesfabric.util;

import java.util.function.Supplier;

import javax.annotation.Nonnull;

/**
 * @author Harley O'Connor
 */
public interface MutableLazyValue<T> {

	static <T> MutableLazyValue<T> supplied(Supplier<T> supplier) {
		return new MutableSuppliedLazyValue<>(supplier);
	}

	T get();

	void reset(Supplier<T> supplier);

	void set(@Nonnull T value);

}
