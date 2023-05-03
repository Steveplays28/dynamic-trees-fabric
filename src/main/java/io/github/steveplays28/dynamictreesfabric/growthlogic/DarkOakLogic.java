package io.github.steveplays28.dynamictreesfabric.growthlogic;

import io.github.steveplays28.dynamictreesfabric.growthlogic.context.DirectionManipulationContext;
import io.github.steveplays28.dynamictreesfabric.growthlogic.context.PositionalSpeciesContext;
import io.github.steveplays28.dynamictreesfabric.util.CoordUtils;

import net.minecraft.util.Identifier;
import net.minecraft.util.math.Direction;

public class DarkOakLogic extends GrowthLogicKit {

	public DarkOakLogic(final Identifier registryName) {
		super(registryName);
	}

	@Override
	public int[] populateDirectionProbabilityMap(GrowthLogicKitConfiguration configuration, DirectionManipulationContext context) {
		final int[] probMap = super.populateDirectionProbabilityMap(configuration, context);
		probMap[Direction.UP.getId()] = 4;

		//Disallow up/down turns after having turned out of the trunk once.
		if (!context.signal().isInTrunk()) {
			probMap[Direction.UP.getId()] = 0;
			probMap[Direction.DOWN.getId()] = 0;
			probMap[context.signal().dir.ordinal()] *= 0.35;//Promotes the zag of the horizontal branches
		}

		//Amplify cardinal directions to encourage spread the higher we get
		float energyRatio = context.signal().delta.getY() / context.species().getEnergy(context.world(), context.pos());
		float spreadPush = energyRatio * 2;
		spreadPush = Math.max(spreadPush, 1.0f);
		for (Direction dir : CoordUtils.HORIZONTALS) {
			probMap[dir.ordinal()] *= spreadPush;
		}

		//Ensure that the branch gets out of the trunk at least two blocks so it won't interfere with new side branches at the same level
		if (context.signal().numTurns == 1 && context.signal().delta.getSquaredDistanceFromCenter(0, context.signal().delta.getY(), 0) == 1.0) {
			for (Direction dir : CoordUtils.HORIZONTALS) {
				if (context.signal().dir != dir) {
					probMap[dir.ordinal()] = 0;
				}
			}
		}

		//If the side branches are too swole then give some other branches a chance
		if (context.signal().isInTrunk()) {
			for (Direction dir : CoordUtils.HORIZONTALS) {
				if (probMap[dir.ordinal()] >= 7) {
					probMap[dir.ordinal()] = 2;
				}
			}
			if (context.signal().delta.getY() > context.species().getLowestBranchHeight() + 5) {
				probMap[Direction.UP.ordinal()] = 0;
				context.signal().energy = 2;
			}
		}

		return probMap;
	}

	@Override
	public float getEnergy(GrowthLogicKitConfiguration configuration, PositionalSpeciesContext context) {
		return super.getEnergy(configuration, context) *
				context.species().biomeSuitability(context.world(), context.pos());
	}

	@Override
	public int getLowestBranchHeight(GrowthLogicKitConfiguration configuration, PositionalSpeciesContext context) {
		return (int) (super.getLowestBranchHeight(configuration, context) *
				context.species().biomeSuitability(context.world(), context.pos()));
	}
}
