package io.github.steveplays28.dynamictreesfabric.growthlogic;

import io.github.steveplays28.dynamictreesfabric.api.TreeHelper;
import io.github.steveplays28.dynamictreesfabric.api.configurations.ConfigurationProperty;
import io.github.steveplays28.dynamictreesfabric.growthlogic.context.DirectionManipulationContext;
import io.github.steveplays28.dynamictreesfabric.growthlogic.context.DirectionSelectionContext;
import io.github.steveplays28.dynamictreesfabric.growthlogic.context.PositionalSpeciesContext;
import io.github.steveplays28.dynamictreesfabric.util.CoordUtils;

import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

public class NetherFungusLogic extends GrowthLogicKit {

	public static final ConfigurationProperty<Integer> MIN_CAP_HEIGHT = ConfigurationProperty.integer("min_cap_height");

	public NetherFungusLogic(final Identifier registryName) {
		super(registryName);
	}

	@Override
	protected GrowthLogicKitConfiguration createDefaultConfiguration() {
		return super.createDefaultConfiguration()
				.with(MIN_CAP_HEIGHT, 3)
				.with(HEIGHT_VARIATION, 8);
	}

	@Override
	protected void registerProperties() {
		this.register(MIN_CAP_HEIGHT, HEIGHT_VARIATION);
	}

	@Override
	public Direction selectNewDirection(GrowthLogicKitConfiguration configuration, DirectionSelectionContext context) {
		final Direction newDir = super.selectNewDirection(configuration, context);
		if (context.signal().isInTrunk() && newDir != Direction.UP) { // Turned out of trunk
			context.signal().energy = Math.min(context.signal().energy, context.species().isMegaSpecies() ? 3 : 2);
		}
		return newDir;
	}

	@Override
	public int[] populateDirectionProbabilityMap(GrowthLogicKitConfiguration configuration,
	                                             DirectionManipulationContext context) {
		final int[] probMap = super.populateDirectionProbabilityMap(configuration, context);

		if (context.signal().isInTrunk()) {
			if (TreeHelper.isBranch(context.world().getBlockState(context.pos().up())) &&
					!TreeHelper.isBranch(context.world().getBlockState(context.pos().up(3)))) {
				context.probMap(new int[]{0, 0, 0, 0, 0, 0});
			} else if (!context.species().isMegaSpecies()) {
				for (Direction direction : CoordUtils.HORIZONTALS) {
					if (TreeHelper.isBranch(
							context.world().getBlockState(context.pos().add(direction.getOpposite().getVector())))) {
						probMap[direction.getId()] = 0;
					}
				}
			}
			probMap[Direction.UP.getId()] = 4;
		} else {
			probMap[Direction.UP.getId()] = 0;
		}
		return probMap;
	}

	private float getHashedVariation(GrowthLogicKitConfiguration configuration, World world, BlockPos pos) {
		long day = world.getTime() / 24000L;
		int month = (int) day / 30;//Change the hashs every in-game month
		return (CoordUtils.coordHashCode(pos.up(month), 2) %
				configuration.get(HEIGHT_VARIATION));//Vary the height energy by a psuedorandom hash function
	}

	@Override
	public float getEnergy(GrowthLogicKitConfiguration configuration, PositionalSpeciesContext context) {
		return Math.min(configuration.getLowestBranchHeight(
						new PositionalSpeciesContext(context.world(), context.pos(), context.species())) +
						configuration.get(MIN_CAP_HEIGHT) +
						getHashedVariation(configuration, context.world(), context.pos()) / 1.5f,
				super.getEnergy(configuration, context));
	}

	@Override
	public int getLowestBranchHeight(GrowthLogicKitConfiguration configuration, PositionalSpeciesContext context) {
		// Vary the lowest branch height by a psuedorandom hash function
		return (int) (super.getLowestBranchHeight(configuration, context) *
				context.species().biomeSuitability(context.world(), context.pos()) +
				getHashedVariation(configuration, context.world(), context.pos()));
	}

}
