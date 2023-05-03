package io.github.steveplays28.dynamictreesfabric.systems.genfeatures;

import io.github.steveplays28.dynamictreesfabric.api.configurations.ConfigurationProperty;
import io.github.steveplays28.dynamictreesfabric.systems.genfeatures.context.PostRotContext;
import net.minecraftforge.common.IPlantable;

import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.LightType;
import net.minecraft.world.WorldAccess;

/**
 * A {@link GenFeature} handling the default post rot behaviour: turning the rotted branch into the {@link #MUSHROOM}
 * set
 * in the {@link GenFeatureConfiguration} object.
 *
 * @author Harley O'Connor
 */
public class MushroomRotGenFeature extends GenFeature {

	public static final ConfigurationProperty<Block> MUSHROOM = ConfigurationProperty.block("mushroom");
	public static final ConfigurationProperty<Block> ALTERNATE_MUSHROOM = ConfigurationProperty.block("alternate_mushroom");
	public static final ConfigurationProperty<Float> ALTERNATE_MUSHROOM_CHANCE = ConfigurationProperty.floatProperty("alternate_mushroom_chance");

	public MushroomRotGenFeature(final Identifier registryName) {
		super(registryName);
	}

	@Override
	protected void registerProperties() {
		this.register(MUSHROOM, ALTERNATE_MUSHROOM, ALTERNATE_MUSHROOM_CHANCE);
	}

	@Override
	protected GenFeatureConfiguration createDefaultConfiguration() {
		return super.createDefaultConfiguration()
				.with(MUSHROOM, Blocks.BROWN_MUSHROOM)
				.with(ALTERNATE_MUSHROOM, Blocks.RED_MUSHROOM)
				.with(ALTERNATE_MUSHROOM_CHANCE, .25f);
	}

	@Override
	protected boolean postRot(GenFeatureConfiguration configuration, PostRotContext context) {
		final WorldAccess world = context.world();
		final BlockPos pos = context.pos();
		final Block mushroom = configuration.get(ALTERNATE_MUSHROOM_CHANCE) > context.random().nextFloat() ?
				configuration.get(MUSHROOM) : configuration.get(ALTERNATE_MUSHROOM);

		if (context.radius() <= 4 || !this.canSustainMushroom(world, pos, mushroom) ||
				world.getLightLevel(LightType.SKY, pos) >= 4) {
			return false;
		}

		world.setBlockState(pos, mushroom.getDefaultState(), 3);
		return true;
	}

	private boolean canSustainMushroom(final WorldAccess world, final BlockPos pos, final Block block) {
		return block instanceof IPlantable && world.getBlockState(pos).canSustainPlant(world, pos, Direction.UP, (IPlantable) block);
	}

}
