package io.github.steveplays28.dynamictreesfabric.trees.species;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.jetbrains.annotations.Nullable;

import io.github.steveplays28.dynamictreesfabric.api.TreeHelper;
import io.github.steveplays28.dynamictreesfabric.api.network.MapSignal;
import io.github.steveplays28.dynamictreesfabric.api.registry.TypedRegistry;
import io.github.steveplays28.dynamictreesfabric.blocks.branches.BranchBlock;
import io.github.steveplays28.dynamictreesfabric.blocks.leaves.DynamicLeavesBlock;
import io.github.steveplays28.dynamictreesfabric.blocks.leaves.LeavesProperties;
import io.github.steveplays28.dynamictreesfabric.blocks.leaves.PalmLeavesProperties;
import io.github.steveplays28.dynamictreesfabric.growthlogic.GrowthLogicKits;
import io.github.steveplays28.dynamictreesfabric.systems.genfeatures.context.PostGenerationContext;
import io.github.steveplays28.dynamictreesfabric.systems.nodemappers.FindEndsNode;
import io.github.steveplays28.dynamictreesfabric.trees.Family;
import io.github.steveplays28.dynamictreesfabric.trees.Species;
import io.github.steveplays28.dynamictreesfabric.util.BranchDestructionData;
import io.github.steveplays28.dynamictreesfabric.util.CoordUtils;
import io.github.steveplays28.dynamictreesfabric.util.SafeChunkBounds;

import net.minecraft.block.BlockState;
import net.minecraft.block.LeavesBlock;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;

public class PalmSpecies extends Species {
	public static final TypedRegistry.EntryType<Species> TYPE = createDefaultType(PalmSpecies::new);

	public PalmSpecies(Identifier name, Family family, LeavesProperties leavesProperties) {
		super(name, family, leavesProperties);
		setGrowthLogicKit(GrowthLogicKits.PALM); //palm growth logic kit by default
	}

	@Override
	public boolean postGrow(World world, BlockPos rootPos, BlockPos treePos, int fertility, boolean natural) {
		BlockState trunkBlockState = world.getBlockState(treePos);
		BranchBlock branch = TreeHelper.getBranch(trunkBlockState);
		if (branch == null) {
			return false;
		}
		FindEndsNode endFinder = new FindEndsNode();
		MapSignal signal = new MapSignal(endFinder);
		branch.analyse(trunkBlockState, world, treePos, Direction.DOWN, signal);
		List<BlockPos> endPoints = endFinder.getEnds();

		for (BlockPos endPoint : endPoints) {
			TreeHelper.ageVolume(world, endPoint, 2, 3, 3, SafeChunkBounds.ANY);
		}

		// Make sure the bottom block is always just a little thicker that the block above it.
		int radius = branch.getRadius(world.getBlockState(treePos.up()));
		if (radius != 0) {
			branch.setRadius(world, treePos, radius + 1, null);
		}

		return super.postGrow(world, rootPos, treePos, fertility, natural);
	}

	public boolean transitionToTree(World world, BlockPos pos) {
		//Ensure planting conditions are right
		Family family = getFamily();
		if (world.isAir(pos.up()) && isAcceptableSoil(world, pos.down(), world.getBlockState(pos.down()))) {
			family.getBranch().ifPresent(branch ->
					// Set to a single branch with 1 radius.
					branch.setRadius(world, pos, family.getPrimaryThickness(), null)
			);
			world.setBlockState(pos.up(), getLeavesProperties().getDynamicLeavesState().with(DynamicLeavesBlock.DISTANCE, 4));//Place 2 leaf blocks on top
			world.setBlockState(pos.up(2), getLeavesProperties().getDynamicLeavesState().with(DynamicLeavesBlock.DISTANCE, 3));
			placeRootyDirtBlock(world, pos.down(), 15);//Set to fully fertilized rooty dirt underneath
			return true;
		}
		return false;
	}

	@Override
	public void postGeneration(PostGenerationContext context) {
		final WorldAccess world = context.world();

		if (!context.endPoints().isEmpty()) {
			BlockPos tip = context.endPoints().get(0).up(2);
			if (context.bounds().inBounds(tip, true)) {
				if (world.getBlockState(tip).getBlock() instanceof DynamicLeavesBlock) {
					for (CoordUtils.Surround surr : CoordUtils.Surround.values()) {
						BlockPos leafPos = tip.add(surr.getOffset());
						BlockState leafState = world.getBlockState(leafPos);
						if (leafState.getBlock() instanceof DynamicLeavesBlock) {
							DynamicLeavesBlock block = (DynamicLeavesBlock) leafState.getBlock();
							world.setBlockState(leafPos, block.getLeavesBlockStateForPlacement(world, leafPos, leafState, leafState.get(LeavesBlock.DISTANCE), true), 2);
						}
					}
				}
			}
		}
		super.postGeneration(context);
	}

	@Nullable
	@Override
	public HashMap<BlockPos, BlockState> getFellingLeavesClusters(BranchDestructionData destructionData) {

		int endPointsNum = destructionData.getNumEndpoints();

		if (endPointsNum < 1) {
			return null;
		}

		HashMap<BlockPos, BlockState> leaves = new HashMap<>();

		for (int i = 0; i < endPointsNum; i++) {
			BlockPos relPos = destructionData.getEndPointRelPos(i).up(2);//A palm tree is only supposed to have one endpoint at it's top.
			relPos = relPos.down();
			LeavesProperties leavesProperties = destructionData.species.getLeavesProperties();

			Set<BlockPos> existingLeaves = new HashSet<>();
			for (int j = 0; j < destructionData.getNumLeaves(); j++) {
				existingLeaves.add(destructionData.getLeavesRelPos(j));
			}

			if (existingLeaves.contains(relPos)) {
				leaves.put(relPos, leavesProperties.getDynamicLeavesState(4));//The barky overlapping part of the palm frond cluster
			}
			if (existingLeaves.contains(relPos.up())) {
				leaves.put(relPos.up(), leavesProperties.getDynamicLeavesState(3));//The leafy top of the palm frond cluster
			}

			//The 4 corners and 4 sides of the palm frond cluster
			for (int hydro = 1; hydro <= 2; hydro++) {
				BlockState state = leavesProperties.getDynamicLeavesState(hydro);
				for (CoordUtils.Surround surr : PalmLeavesProperties.DynamicPalmLeavesBlock.hydroSurroundMap[hydro]) {
					BlockPos leafPos = relPos.up().add(surr.getOpposite().getOffset());
					if (existingLeaves.contains(leafPos)) {
						leaves.put(leafPos, PalmLeavesProperties.DynamicPalmLeavesBlock.getDirectionState(state, surr));
					}
				}
			}
		}

		return leaves;
	}

}
