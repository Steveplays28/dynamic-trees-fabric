package io.github.steveplays28.dynamictreesfabric.systems.genfeatures;

import java.util.List;
import java.util.function.Supplier;

import io.github.steveplays28.dynamictreesfabric.api.GeneratesFruit;
import io.github.steveplays28.dynamictreesfabric.api.TreeHelper;
import io.github.steveplays28.dynamictreesfabric.api.configurations.ConfigurationProperty;
import io.github.steveplays28.dynamictreesfabric.api.network.MapSignal;
import io.github.steveplays28.dynamictreesfabric.blocks.FruitBlock;
import io.github.steveplays28.dynamictreesfabric.blocks.branches.BranchBlock;
import io.github.steveplays28.dynamictreesfabric.compat.seasons.SeasonHelper;
import io.github.steveplays28.dynamictreesfabric.init.DTRegistries;
import io.github.steveplays28.dynamictreesfabric.systems.genfeatures.context.PostGenerationContext;
import io.github.steveplays28.dynamictreesfabric.systems.genfeatures.context.PostGrowContext;
import io.github.steveplays28.dynamictreesfabric.systems.nodemappers.FindEndsNode;
import io.github.steveplays28.dynamictreesfabric.trees.Species;
import io.github.steveplays28.dynamictreesfabric.util.CoordUtils;
import io.github.steveplays28.dynamictreesfabric.util.SafeChunkBounds;

import net.minecraft.block.BlockState;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;

@GeneratesFruit
public class FruitGenFeature extends GenFeature {

	@SuppressWarnings("unchecked")
	public static final ConfigurationProperty<Supplier<FruitBlock>> FRUIT_BLOCK = ConfigurationProperty.property("fruit_block", (Class<Supplier<FruitBlock>>) (Class) Supplier.class);

	public FruitGenFeature(Identifier registryName) {
		super(registryName);
	}

	@Override
	protected void registerProperties() {
		this.register(FRUIT_BLOCK, VERTICAL_SPREAD, QUANTITY, RAY_DISTANCE, FRUITING_RADIUS, PLACE_CHANCE);
	}

	@Override
	public GenFeatureConfiguration createDefaultConfiguration() {
		return super.createDefaultConfiguration()
				.with(FRUIT_BLOCK, DTRegistries.APPLE_FRUIT)
				.with(VERTICAL_SPREAD, 30f)
				.with(QUANTITY, 4)
				.with(FRUITING_RADIUS, 8)
				.with(PLACE_CHANCE, 1f);
	}

	@Override
	protected boolean postGenerate(GenFeatureConfiguration configuration, PostGenerationContext context) {
		if (!context.endPoints().isEmpty()) {
			int qty = configuration.get(QUANTITY);
			qty *= context.fruitProductionFactor();
			for (int i = 0; i < qty; i++) {
				final BlockPos endPoint = context.endPoints().get(context.random().nextInt(context.endPoints().size()));
				this.addFruit(configuration, context.world(), context.species(), context.pos().up(), endPoint, true,
						false, context.bounds(), context.seasonValue());
			}
			return true;
		}
		return false;
	}

	@Override
	protected boolean postGrow(GenFeatureConfiguration configuration, PostGrowContext context) {
		final World world = context.world();
		final BlockState blockState = world.getBlockState(context.treePos());
		final BranchBlock branch = TreeHelper.getBranch(blockState);

		if (branch != null && branch.getRadius(blockState) >= configuration.get(FRUITING_RADIUS) && context.natural()) {
			final BlockPos rootPos = context.pos();
			final float fruitingFactor = context.species().seasonalFruitProductionFactor(world, rootPos);

			if (fruitingFactor > configuration.get(FRUIT_BLOCK).get().getMinimumSeasonalValue() && fruitingFactor > world.random.nextFloat()) {
				final FindEndsNode endFinder = new FindEndsNode();
				TreeHelper.startAnalysisFromRoot(world, rootPos, new MapSignal(endFinder));
				final List<BlockPos> endPoints = endFinder.getEnds();
				int qty = configuration.get(QUANTITY);
				if (!endPoints.isEmpty()) {
					for (int i = 0; i < qty; i++) {
						final BlockPos endPoint = endPoints.get(world.random.nextInt(endPoints.size()));
						this.addFruit(configuration, world, context.species(), rootPos.up(), endPoint, false, true,
								SafeChunkBounds.ANY, SeasonHelper.getSeasonValue(world, rootPos));
					}
				}
			}
		}

		return true;
	}

	protected void addFruit(GenFeatureConfiguration configuration, WorldAccess world, Species species, BlockPos treePos, BlockPos branchPos, boolean worldGen, boolean enableHash, SafeChunkBounds safeBounds, Float seasonValue) {
		final BlockPos fruitPos = CoordUtils.getRayTraceFruitPos(world, species, treePos, branchPos, safeBounds);
		if (fruitPos != BlockPos.ORIGIN &&
				(!enableHash || ((CoordUtils.coordHashCode(fruitPos, 0) & 3) == 0)) &&
				world.getRandom().nextFloat() <= configuration.get(PLACE_CHANCE)) {
			FruitBlock fruitBlock = configuration.get(FRUIT_BLOCK).get();
			BlockState setState = fruitBlock.getStateForAge(worldGen ? fruitBlock.getAgeForSeasonalWorldGen(world, fruitPos, seasonValue) : 0);
			world.setBlockState(fruitPos, setState, 3);
		}
	}

}
