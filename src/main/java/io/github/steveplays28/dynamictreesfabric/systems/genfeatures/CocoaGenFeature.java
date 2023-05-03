package io.github.steveplays28.dynamictreesfabric.systems.genfeatures;

import io.github.steveplays28.dynamictreesfabric.api.GeneratesFruit;
import io.github.steveplays28.dynamictreesfabric.api.TreeHelper;
import io.github.steveplays28.dynamictreesfabric.api.network.MapSignal;
import io.github.steveplays28.dynamictreesfabric.systems.genfeatures.context.PostGenerationContext;
import io.github.steveplays28.dynamictreesfabric.systems.genfeatures.context.PostGrowContext;
import io.github.steveplays28.dynamictreesfabric.systems.nodemappers.CocoaFruitNode;

import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;

@GeneratesFruit
public class CocoaGenFeature extends GenFeature {

	public CocoaGenFeature(Identifier registryName) {
		super(registryName);
	}

	@Override
	protected void registerProperties() {
	}

	@Override
	protected boolean postGrow(GenFeatureConfiguration configuration, PostGrowContext context) {
		if (context.fertility() == 0 && context.random().nextInt() % 16 == 0) {
			final World world = context.world();
			if (context.species().seasonalFruitProductionFactor(world, context.treePos()) > context.random().nextFloat()) {
				this.addCocoa(world, context.pos(), false);
			}
		}
		return false;
	}

	@Override
	protected boolean postGenerate(GenFeatureConfiguration configuration, PostGenerationContext context) {
		if (context.random().nextInt() % 8 == 0) {
			this.addCocoa(context.world(), context.pos(), true);
			return true;
		}
		return false;
	}

	private void addCocoa(WorldAccess world, BlockPos rootPos, boolean worldGen) {
		TreeHelper.startAnalysisFromRoot(world, rootPos, new MapSignal(new CocoaFruitNode().setWorldGen(worldGen)));
	}

}
