package io.github.steveplays28.dynamictreesfabric.systems.genfeatures;

import io.github.steveplays28.dynamictreesfabric.api.TreeHelper;
import io.github.steveplays28.dynamictreesfabric.api.configurations.ConfigurationProperty;
import io.github.steveplays28.dynamictreesfabric.systems.genfeatures.context.PostGenerationContext;
import io.github.steveplays28.dynamictreesfabric.systems.genfeatures.context.PostGrowContext;
import io.github.steveplays28.dynamictreesfabric.trees.Family;
import io.github.steveplays28.dynamictreesfabric.trees.Species;

import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.WorldAccess;

public class BottomFlareGenFeature extends GenFeature {

	// Min radius for the flare.
	public static final ConfigurationProperty<Integer> MIN_RADIUS = ConfigurationProperty.integer("min_radius");

	public BottomFlareGenFeature(Identifier registryName) {
		super(registryName);
	}

	@Override
	protected void registerProperties() {
		this.register(MIN_RADIUS);
	}

	@Override
	public GenFeatureConfiguration createDefaultConfiguration() {
		return super.createDefaultConfiguration()
				.with(MIN_RADIUS, 6);
	}

	@Override
	protected boolean postGrow(GenFeatureConfiguration configuration, PostGrowContext context) {
		if (context.fertility() > 0) {
			this.flareBottom(configuration, context.world(), context.pos(), context.species());
			return true;
		}
		return false;
	}

	@Override
	protected boolean postGenerate(GenFeatureConfiguration configuration, PostGenerationContext context) {
		this.flareBottom(configuration, context.world(), context.pos(), context.species());
		return true;
	}

	/**
	 * Put a cute little flare on the bottom of the dark oaks
	 *
	 * @param world   The world
	 * @param rootPos The position of the rooty dirt block of the tree
	 */
	public void flareBottom(GenFeatureConfiguration configuration, WorldAccess world, BlockPos rootPos, Species species) {
		Family family = species.getFamily();

		//Put a cute little flare on the bottom of the dark oaks
		int radius3 = TreeHelper.getRadius(world, rootPos.up(3));

		if (radius3 > configuration.get(MIN_RADIUS)) {
			family.getBranch().ifPresent(branch -> {
				branch.setRadius(world, rootPos.up(2), radius3 + 1, Direction.UP);
				branch.setRadius(world, rootPos.up(1), radius3 + 2, Direction.UP);
			});
		}
	}

}
