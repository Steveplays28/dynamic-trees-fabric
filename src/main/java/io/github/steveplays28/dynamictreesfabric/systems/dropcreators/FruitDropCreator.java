package io.github.steveplays28.dynamictreesfabric.systems.dropcreators;

import java.util.List;

import io.github.steveplays28.dynamictreesfabric.api.configurations.ConfigurationProperty;
import io.github.steveplays28.dynamictreesfabric.systems.dropcreators.context.DropContext;

import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.Identifier;

/**
 * A drop creator that drops fruit just like Vanilla apples.
 *
 * @author ferreusveritas
 */
public class FruitDropCreator extends DropCreator {

	public static final ConfigurationProperty<ItemStack> FRUIT = ConfigurationProperty.property("fruit_item", ItemStack.class);

	public FruitDropCreator(Identifier registryName) {
		super(registryName);
	}

	@Override
	protected void registerProperties() {
		this.register(FRUIT, RARITY);
	}

	@Override
	protected DropCreatorConfiguration createDefaultConfiguration() {
		return super.createDefaultConfiguration()
				.with(FRUIT, new ItemStack(Items.APPLE))
				.with(RARITY, 1f);
	}

	@Override
	public void appendHarvestDrops(DropCreatorConfiguration configuration, DropContext context) {
		this.appendFruit(context.drops(), configuration, context);
	}

	@Override
	public void appendLeavesDrops(DropCreatorConfiguration configuration, DropContext context) {
		this.appendFruit(context.drops(), configuration, context);
	}

	private void appendFruit(List<ItemStack> dropList, DropCreatorConfiguration configuration, DropContext context) {
		// More fortune contrivances here.  Vanilla compatible returns.
		int chance = 200; // 1 in 200 chance of returning an "apple"
		if (context.fortune() > 0) {
			chance -= 10 << context.fortune();
			if (chance < 40) {
				chance = 40;
			}
		}

		if (context.random().nextInt((int) (chance / configuration.get(RARITY))) == 0) {
			dropList.add(configuration.get(FRUIT));
		}
	}

}
