package io.github.steveplays28.dynamictreesfabric.growthlogic;

import io.github.steveplays28.dynamictreesfabric.growthlogic.context.DirectionManipulationContext;
import io.github.steveplays28.dynamictreesfabric.growthlogic.context.PositionalSpeciesContext;
import io.github.steveplays28.dynamictreesfabric.util.CoordUtils;

import net.minecraft.util.Identifier;
import net.minecraft.util.math.Direction;

public class PalmGrowthLogic extends GrowthLogicKit {

	public PalmGrowthLogic(Identifier registryName) {
		super(registryName);
	}

	@Override
	public int[] populateDirectionProbabilityMap(GrowthLogicKitConfiguration configuration,
	                                             DirectionManipulationContext context) {
		final int[] probMap = super.populateDirectionProbabilityMap(configuration, context);
		Direction originDir = context.signal().dir.getOpposite();

		// Alter probability map for direction change
		probMap[0] = 0; // Down is always disallowed for palm
		probMap[1] = 10;
		probMap[2] = probMap[3] = probMap[4] = probMap[5] = 0;
		probMap[originDir.ordinal()] = 0; // Disable the direction we came from

		return probMap;
	}

	@Override
	public float getEnergy(GrowthLogicKitConfiguration configuration, PositionalSpeciesContext context) {
		long day = context.world().getTime() / 24000L;
		int month = (int) day / 30; // Change the hashs every in-game month
		return super.getEnergy(configuration, context) *
				context.species().biomeSuitability(context.world(), context.pos()) +
				(CoordUtils.coordHashCode(context.pos().up(month), 3) %
						3); // Vary the height energy by a psuedorandom hash function

	}

}
