package io.github.steveplays28.dynamictreesfabric.systems.dropcreators;

import io.github.steveplays28.dynamictreesfabric.api.configurations.ConfigurationProperty;
import io.github.steveplays28.dynamictreesfabric.init.DTConfigs;
import io.github.steveplays28.dynamictreesfabric.systems.dropcreators.context.LogDropContext;
import io.github.steveplays28.dynamictreesfabric.systems.nodemappers.NetVolumeNode;
import io.github.steveplays28.dynamictreesfabric.trees.Species;
import io.github.steveplays28.dynamictreesfabric.trees.Species.LogsAndSticks;

import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;

public class LogDropCreator extends DropCreator {

	/**
	 * This works in addition to {@link DTConfigs#TREE_HARVEST_MULTIPLIER}, meant for trees that are too small to drop
	 * any wood.
	 */
	public static final ConfigurationProperty<Float> MULTIPLIER = ConfigurationProperty.floatProperty("multiplier");

	public LogDropCreator(Identifier registryName) {
		super(registryName);
	}

	@Override
	protected void registerProperties() {
		this.register(MULTIPLIER);
	}

	@Override
	protected DropCreatorConfiguration createDefaultConfiguration() {
		return super.createDefaultConfiguration()
				.with(MULTIPLIER, 1.0f);
	}

	@Override
	public void appendLogDrops(DropCreatorConfiguration configuration, LogDropContext context) {
		final Species species = context.species();
		final NetVolumeNode.Volume volume = context.volume();
		volume.multiplyVolume(configuration.get(MULTIPLIER));

		final LogsAndSticks las = species.getLogsAndSticks(volume);

		int numLogs = las.logs.size();
		if (numLogs > 0) {
			context.drops().addAll(las.logs);
		}
		int numSticks = las.sticks;
		if (numSticks > 0) {
			final ItemStack stack = species.getFamily().getStick(numSticks);
			while (numSticks > 0) {
				ItemStack drop = stack.copy();
				drop.setCount(Math.min(numSticks, stack.getMaxCount()));
				context.drops().add(drop);
				numSticks -= stack.getMaxCount();
			}
		}
	}

}
