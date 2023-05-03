package io.github.steveplays28.dynamictreesfabric.systems.dropcreators;

import io.github.steveplays28.dynamictreesfabric.api.configurations.ConfigurationProperty;
import io.github.steveplays28.dynamictreesfabric.event.VoluntarySeedDropEvent;
import io.github.steveplays28.dynamictreesfabric.init.DTConfigs;
import io.github.steveplays28.dynamictreesfabric.systems.dropcreators.context.DropContext;
import io.github.steveplays28.dynamictreesfabric.trees.Species;
import net.minecraftforge.common.MinecraftForge;

import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;

public class SeedDropCreator extends DropCreator {

	public static final ConfigurationProperty<Float> HARVEST_RARITY = ConfigurationProperty.floatProperty("harvest_rarity");
	public static final ConfigurationProperty<Float> VOLUNTARY_RARITY = ConfigurationProperty.floatProperty("voluntary_rarity");
	public static final ConfigurationProperty<Float> LEAVES_RARITY = ConfigurationProperty.floatProperty("leaves_rarity");
	/**
	 * Allows a custom seed to be set, for example, tree A may want to drop the seed of tree B.
	 */
	public static final ConfigurationProperty<ItemStack> SEED = ConfigurationProperty.property("seed", ItemStack.class);

	public SeedDropCreator(Identifier registryName) {
		super(registryName);
	}

	// Allows for overriding species seed drop if a custom seed is set.
	protected ItemStack getSeedStack(Species species, DropCreatorConfiguration configuration) {
		final ItemStack customSeed = configuration.get(SEED);
		return customSeed.isEmpty() ? species.getSeedStack(1) : customSeed;
	}

	@Override
	protected void registerProperties() {
		this.register(RARITY, HARVEST_RARITY, VOLUNTARY_RARITY, LEAVES_RARITY, SEED);
	}

	@Override
	protected DropCreatorConfiguration createDefaultConfiguration() {
		return super.createDefaultConfiguration()
				.with(RARITY, 1f)
				.with(HARVEST_RARITY, -1f)
				.with(VOLUNTARY_RARITY, -1f)
				.with(LEAVES_RARITY, -1f)
				.with(SEED, ItemStack.EMPTY);
	}

	private float rarityOrDefault(DropCreatorConfiguration configuration, ConfigurationProperty<Float> rarityProperty) {
		final float rarityOverride = configuration.get(rarityProperty);
		return rarityOverride == -1f ? configuration.get(RARITY) : rarityOverride;
	}

	@Override
	public void appendHarvestDrops(DropCreatorConfiguration configuration, DropContext context) {
		float rarity = this.rarityOrDefault(configuration, HARVEST_RARITY);
		rarity *= (context.fortune() + 1) / 64f;
		rarity *= Math.min(context.species().seasonalSeedDropFactor(context.world(), context.pos()) + 0.15f, 1.0);

		if (rarity > context.random().nextFloat()) {//1 in 64 chance to drop a seed on destruction..
			context.drops().add(getSeedStack(context.species(), configuration));
		}
	}

	@Override
	public void appendVoluntaryDrops(DropCreatorConfiguration configuration, DropContext context) {
		if (this.rarityOrDefault(configuration, VOLUNTARY_RARITY) * DTConfigs.SEED_DROP_RATE.get() *
				context.species().seasonalSeedDropFactor(context.world(), context.pos())
				> context.random().nextFloat()) {
			context.drops().add(getSeedStack(context.species(), configuration));
			VoluntarySeedDropEvent seedDropEvent = new VoluntarySeedDropEvent(context.world(), context.pos(), context.species(), context.drops());
			MinecraftForge.EVENT_BUS.post(seedDropEvent);
			if (seedDropEvent.isCanceled()) {
				context.drops().clear();
			}
		}
	}

	@Override
	public void appendLeavesDrops(DropCreatorConfiguration configuration, DropContext context) {
		int chance = 20; // See BlockLeaves#getSaplingDropChance(state);
		// Hokey fortune stuff here to match Vanilla logic.
		if (context.fortune() > 0) {
			chance -= 2 << context.fortune();
			if (chance < 10) {
				chance = 10;
			}
		}

		float seasonFactor = 1.0f;

		if (!context.world().isClient) {
			seasonFactor = context.species().seasonalSeedDropFactor(context.world(), context.pos());
		}

		if (context.random().nextInt((int) (chance / this.rarityOrDefault(configuration, LEAVES_RARITY))) == 0) {
			if (seasonFactor > context.random().nextFloat()) {
				context.drops().add(this.getSeedStack(context.species(), configuration));
			}
		}
	}

}
