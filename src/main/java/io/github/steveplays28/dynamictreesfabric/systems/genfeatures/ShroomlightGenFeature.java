package io.github.steveplays28.dynamictreesfabric.systems.genfeatures;

import java.util.LinkedList;
import java.util.List;

import javax.annotation.Nullable;

import io.github.steveplays28.dynamictreesfabric.api.TreeHelper;
import io.github.steveplays28.dynamictreesfabric.api.configurations.ConfigurationProperty;
import io.github.steveplays28.dynamictreesfabric.blocks.leaves.DynamicLeavesBlock;
import io.github.steveplays28.dynamictreesfabric.systems.genfeatures.context.PostGenerationContext;
import io.github.steveplays28.dynamictreesfabric.systems.genfeatures.context.PostGrowContext;
import io.github.steveplays28.dynamictreesfabric.util.CoordUtils;
import io.github.steveplays28.dynamictreesfabric.util.function.TetraFunction;

import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.WorldAccess;

/**
 * Gen feature for shroomlight but works for any block. Can be fully customized with a custom predicate for natural
 * growth. It is recommended for the generated block to be made connectable using {@link
 * io.github.steveplays28.dynamictreesfabric.systems.BranchConnectables#makeBlockConnectable(Block, TetraFunction)}
 *
 * @author Max Hyper
 */
public class ShroomlightGenFeature extends GenFeature {

	public static final ConfigurationProperty<Block> SHROOMLIGHT_BLOCK = ConfigurationProperty.block("shroomlight");

	private static final Direction[] HORIZONTALS = CoordUtils.HORIZONTALS;
	private static final double VANILLA_GROW_CHANCE = .005f;

	public ShroomlightGenFeature(Identifier registryName) {
		super(registryName);
	}

	@Override
	protected void registerProperties() {
		this.register(SHROOMLIGHT_BLOCK, MAX_HEIGHT, CAN_GROW_PREDICATE, PLACE_CHANCE, MAX_COUNT);
	}

	@Override
	protected GenFeatureConfiguration createDefaultConfiguration() {
		return super.createDefaultConfiguration()
				.with(SHROOMLIGHT_BLOCK, Blocks.SHROOMLIGHT)
				.with(MAX_HEIGHT, 32)
				.with(CAN_GROW_PREDICATE, (world, blockPos) ->
						world.getRandom().nextFloat() <= VANILLA_GROW_CHANCE)
				.with(PLACE_CHANCE, .4f)
				.with(MAX_COUNT, 4);
	}

	@Override
	protected boolean postGenerate(GenFeatureConfiguration configuration, PostGenerationContext context) {
		return this.placeShroomlightsInValidPlace(configuration, context.world(), context.pos(), true);
	}

	@Override
	protected boolean postGrow(GenFeatureConfiguration configuration, PostGrowContext context) {
		return context.natural() && configuration.get(CAN_GROW_PREDICATE).test(context.world(), context.pos().up())
				&& context.fertility() != 0 && this.placeShroomlightsInValidPlace(configuration, context.world(), context.pos(), false);
	}

	private boolean placeShroomlightsInValidPlace(GenFeatureConfiguration configuration, WorldAccess world, BlockPos rootPos, boolean worldGen) {
		int treeHeight = getTreeHeight(world, rootPos, configuration.get(MAX_HEIGHT));
		Block shroomlightBlock = configuration.get(SHROOMLIGHT_BLOCK);

		List<BlockPos> validSpaces = findBranchPits(configuration, world, rootPos, treeHeight);
		if (validSpaces == null) {
			return false;
		}
		if (validSpaces.size() > 0) {
			if (worldGen) {
				int placed = 0;
				for (BlockPos chosenSpace : validSpaces) {
					if (world.getRandom().nextFloat() <= configuration.get(PLACE_CHANCE)) {
						world.setBlockState(chosenSpace, shroomlightBlock.getDefaultState(), 2);
						placed++;
						if (placed > configuration.get(MAX_COUNT)) {
							break;
						}
					}
				}
			} else {
				BlockPos chosenSpace = validSpaces.get(world.getRandom().nextInt(validSpaces.size()));
				world.setBlockState(chosenSpace, shroomlightBlock.getDefaultState(), 2);
			}
			return true;
		}
		return false;
	}

	private int getTreeHeight(WorldAccess world, BlockPos rootPos, int maxHeight) {
		for (int i = 1; i < maxHeight; i++) {
			if (!TreeHelper.isBranch(world.getBlockState(rootPos.up(i)))) {
				return i - 1;
			}
		}
		return maxHeight;
	}

	//Like the BeeNestGenFeature, the valid places are empty blocks under branches next to the trunk.
	@Nullable
	private List<BlockPos> findBranchPits(GenFeatureConfiguration configuration, WorldAccess world, BlockPos rootPos, int maxHeight) {
		int existingBlocks = 0;
		List<BlockPos> validSpaces = new LinkedList<>();
		for (int y = 2; y < maxHeight; y++) {
			BlockPos trunkPos = rootPos.up(y);
			for (Direction dir : HORIZONTALS) {
				BlockPos sidePos = trunkPos.offset(dir);
				if ((world.isAir(sidePos) || world.getBlockState(sidePos).getBlock() instanceof DynamicLeavesBlock) && TreeHelper.isBranch(world.getBlockState(sidePos.up()))) {
					validSpaces.add(sidePos);
				} else if (world.getBlockState(sidePos).getBlock() == configuration.get(SHROOMLIGHT_BLOCK)) {
					existingBlocks++;
					if (existingBlocks > configuration.get(MAX_COUNT)) {
						return null;
					}
				}
			}
		}
		return validSpaces;
	}

}
