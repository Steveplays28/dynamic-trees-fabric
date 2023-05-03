package io.github.steveplays28.dynamictreesfabric.systems.dropcreators;

import io.github.steveplays28.dynamictreesfabric.api.configurations.ConfigurableRegistry;
import io.github.steveplays28.dynamictreesfabric.api.configurations.ConfigurableRegistryEntry;
import io.github.steveplays28.dynamictreesfabric.api.configurations.ConfigurationProperty;
import io.github.steveplays28.dynamictreesfabric.api.registry.RegistryEntry;
import io.github.steveplays28.dynamictreesfabric.api.registry.SimpleRegistry;
import io.github.steveplays28.dynamictreesfabric.init.DTTrees;
import io.github.steveplays28.dynamictreesfabric.systems.dropcreators.context.DropContext;
import io.github.steveplays28.dynamictreesfabric.systems.dropcreators.context.LogDropContext;
import io.github.steveplays28.dynamictreesfabric.trees.Resettable;
import org.apache.logging.log4j.util.TriConsumer;

import net.minecraft.util.Identifier;

/**
 * This exists solely to aid in the creation of a cleaner anonymous class. All of the members in this class act as
 * pass-thrus by default.
 *
 * @author ferreusveritas
 */
public abstract class DropCreator extends ConfigurableRegistryEntry<DropCreator, DropCreatorConfiguration>
		implements Resettable<DropCreator> {

	public static final ConfigurationProperty<Float> RARITY = ConfigurationProperty.floatProperty("rarity");
	public static final DropCreator NULL = new DropCreator(DTTrees.NULL) {
		@Override
		protected void registerProperties() {
		}
	};

	public static final ConfigurableRegistry<DropCreator, DropCreatorConfiguration> REGISTRY =
			new ConfigurableRegistry<>(DropCreator.class, NULL, DropCreatorConfiguration.TEMPLATES);

	public DropCreator(final Identifier registryName) {
		super(registryName);
	}

	@Override
	protected DropCreatorConfiguration createDefaultConfiguration() {
		return new DropCreatorConfiguration(this);
	}

	public <C extends DropContext> void appendDrops(final DropCreatorConfiguration configuration, final Type<C> type,
	                                                final C context) {
		type.appendDrops(configuration, context);
	}

	protected void appendHarvestDrops(final DropCreatorConfiguration configuration, DropContext context) {
	}

	protected void appendVoluntaryDrops(final DropCreatorConfiguration configuration, DropContext context) {
	}

	protected void appendLeavesDrops(final DropCreatorConfiguration configuration, DropContext context) {
	}

	protected void appendLogDrops(final DropCreatorConfiguration configuration, LogDropContext context) {
	}

	public static final class Type<C extends DropContext> extends RegistryEntry<Type<C>> {
		public static final Type<DropContext> NULL = new Type<>(DTTrees.NULL, (dropCreator, configured, context) -> {
		});

		@SuppressWarnings("unchecked")
		public static final Class<Type<DropContext>> TYPE = (Class<Type<DropContext>>) NULL.getClass();
		public static final SimpleRegistry<Type<DropContext>> REGISTRY = new SimpleRegistry<>(TYPE, NULL);

		public static final Type<DropContext> HARVEST = register(new Type<>(io.github.steveplays28.dynamictreesfabric.DynamicTreesFabric.resLoc("harvest"),
				DropCreator::appendHarvestDrops));
		public static final Type<DropContext> VOLUNTARY = register(new Type<>(io.github.steveplays28.dynamictreesfabric.DynamicTreesFabric.resLoc("voluntary"),
				DropCreator::appendVoluntaryDrops));
		public static final Type<DropContext> LEAVES = register(new Type<>(io.github.steveplays28.dynamictreesfabric.DynamicTreesFabric.resLoc("leaves"),
				DropCreator::appendLeavesDrops));
		public static final Type<LogDropContext> LOGS = register(new Type<>(io.github.steveplays28.dynamictreesfabric.DynamicTreesFabric.resLoc("logs"),
				DropCreator::appendLogDrops));
		private final TriConsumer<DropCreator, DropCreatorConfiguration, C> appendDropConsumer;

		public Type(Identifier registryName,
		            TriConsumer<DropCreator, DropCreatorConfiguration, C> appendDropConsumer) {
			super(registryName);
			this.appendDropConsumer = appendDropConsumer;
		}

		@SuppressWarnings("unchecked")
		private static <C extends DropContext> Type<C> register(Type<C> type) {
			REGISTRY.register((Type<DropContext>) type);
			return type;
		}

		@SuppressWarnings("unchecked")
		public static Class<Type<DropContext>> getGenericClass() {
			return (Class<Type<DropContext>>) NULL.getClass();
		}

		public void appendDrops(DropCreatorConfiguration configuration, C context) {
			this.appendDropConsumer.accept(configuration.getConfigurable(), configuration, context);
		}
	}

}
