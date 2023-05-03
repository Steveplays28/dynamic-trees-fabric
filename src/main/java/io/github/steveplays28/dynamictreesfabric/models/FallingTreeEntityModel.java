package io.github.steveplays28.dynamictreesfabric.models;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import io.github.steveplays28.dynamictreesfabric.api.TreeHelper;
import io.github.steveplays28.dynamictreesfabric.blocks.branches.BranchBlock;
import io.github.steveplays28.dynamictreesfabric.blocks.rootyblocks.RootyBlock;
import io.github.steveplays28.dynamictreesfabric.client.QuadManipulator;
import io.github.steveplays28.dynamictreesfabric.entities.FallingTreeEntity;
import io.github.steveplays28.dynamictreesfabric.models.modeldata.ModelConnections;
import io.github.steveplays28.dynamictreesfabric.trees.Species;
import io.github.steveplays28.dynamictreesfabric.util.BranchDestructionData;
import net.minecraftforge.client.model.data.ModelData;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.block.BlockRenderManager;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.BakedQuad;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class FallingTreeEntityModel extends EntityModel<FallingTreeEntity> {

	protected final List<TreeQuadData> quads;
	//	protected Map<BakedQuad, Integer> quadTints;
	protected final int entityId;
	protected final Species species;

	public FallingTreeEntityModel(FallingTreeEntity entity) {
		World world = entity.getEntityWorld();
		BranchDestructionData destructionData = entity.getDestroyData();
		Species species = destructionData.species;

		quads = generateTreeQuads(entity);
//		quadTints = entity.getQuadTints();
		this.species = species;
		entityId = entity.getId();
	}

	public static int getBrightness(FallingTreeEntity entity) {
		final BranchDestructionData destructionData = entity.getDestroyData();
		final World world = entity.world;
		return world.getBlockState(destructionData.cutPos).getLuminance(world, destructionData.cutPos);
	}

	public static List<TreeQuadData> generateTreeQuads(FallingTreeEntity entity) {
		BlockRenderManager dispatcher = MinecraftClient.getInstance().getBlockRenderManager();
		BranchDestructionData destructionData = entity.getDestroyData();
		Direction cutDir = destructionData.cutDir;

		ArrayList<TreeQuadData> treeQuads = new ArrayList<>();

		int[] connectionArray = new int[6];

		if (destructionData.getNumBranches() > 0) {
			BlockState exState = destructionData.getBranchBlockState(0);
			BlockPos rootPos = destructionData.cutPos;
			if (exState != null) {
				Species species = destructionData.species;

				//Draw the rooty block if it is set to fall too
				BlockPos bottomPos = entity.getBlockPos().down();
				BlockState bottomState = entity.world.getBlockState(bottomPos);
				boolean rootyBlockAdded = false;
				if (TreeHelper.isRooty(bottomState)) {
					RootyBlock rootyBlock = TreeHelper.getRooty(bottomState);
					if (rootyBlock != null && rootyBlock.fallWithTree(bottomState, entity.world, bottomPos)) {
						BakedModel rootyModel = dispatcher.getModel(bottomState);
						treeQuads.addAll(toTreeQuadData(QuadManipulator.getQuads(rootyModel, bottomState, new Vec3d(0, -1, 0), ModelData.EMPTY),
								destructionData.species.getFamily().getRootColor(bottomState, rootyBlock.getColorFromBark()),
								bottomState));
						rootyBlockAdded = true;
					}
				}

				BakedModel branchModel = dispatcher.getModel(exState);
				//Draw the ring texture cap on the cut block if the bottom connection is above 0
				destructionData.getConnections(0, connectionArray);
				boolean bottomRingsAdded = false;
				if (!rootyBlockAdded && connectionArray[cutDir.getId()] > 0) {
					BlockPos offsetPos = BlockPos.ORIGIN.offset(cutDir);
					float offset = (8 - Math.min(((BranchBlock) exState.getBlock()).getRadius(exState), BranchBlock.MAX_RADIUS)) / 16f;
					treeQuads.addAll(toTreeQuadData(QuadManipulator.getQuads(branchModel, exState, new Vec3d(offsetPos.getX(), offsetPos.getY(), offsetPos.getZ()).multiply(offset), new Direction[]{null},
									new ModelConnections(cutDir).setFamily(TreeHelper.getBranch(exState)).toModelData()),
							exState));
					bottomRingsAdded = true;
				}

				//Draw the rest of the tree/branch
				for (int index = 0; index < destructionData.getNumBranches(); index++) {
					Block previousBranch = exState.getBlock();
					exState = destructionData.getBranchBlockState(index);
					if (!previousBranch.equals(exState.getBlock())) //Update the branch model only if the block is different
					{
						branchModel = dispatcher.getModel(exState);
					}
					BlockPos relPos = destructionData.getBranchRelPos(index);
					destructionData.getConnections(index, connectionArray);
					ModelConnections modelConnections = new ModelConnections(connectionArray).setFamily(TreeHelper.getBranch(exState));
					if (index == 0 && bottomRingsAdded) {
						modelConnections.setForceRing(cutDir);
					}
					treeQuads.addAll(toTreeQuadData(QuadManipulator.getQuads(branchModel, exState, new Vec3d(relPos.getX(), relPos.getY(), relPos.getZ()), modelConnections.toModelData()),
							exState));
				}

				//Draw the leaves
				final HashMap<BlockPos, BlockState> leavesClusters = species.getFellingLeavesClusters(destructionData);
				if (leavesClusters != null) {
					for (Map.Entry<BlockPos, BlockState> leafLoc : leavesClusters.entrySet()) {
						BlockState leafState = leafLoc.getValue();
						treeQuads.addAll(toTreeQuadData(QuadManipulator.getQuads(dispatcher.getModel(leafState), leafState, new Vec3d(leafLoc.getKey().getX(), leafLoc.getKey().getY(), leafLoc.getKey().getZ()), ModelData.EMPTY),
								species.leafColorMultiplier(entity.world, rootPos.add(leafLoc.getKey())), leafState));
					}
				} else {
					for (int index = 0; index < destructionData.getNumLeaves(); index++) {
						BlockPos relPos = destructionData.getLeavesRelPos(index);
						BlockState leafState = destructionData.getLeavesBlockState(index);
						BakedModel leavesModel = dispatcher.getModel(leafState);
						treeQuads.addAll(toTreeQuadData(QuadManipulator.getQuads(leavesModel, leafState, new Vec3d(relPos.getX(), relPos.getY(), relPos.getZ()), ModelData.EMPTY),
								destructionData.getLeavesProperties(index).treeFallColorMultiplier(leafState, entity.world, rootPos.add(relPos)), leafState));
					}
				}

			}
		}

		return treeQuads;
	}

	public static List<TreeQuadData> toTreeQuadData(List<BakedQuad> bakedQuads, BlockState state) {
		return toTreeQuadData(bakedQuads, 0xFFFFFF, state);
	}

	public static List<TreeQuadData> toTreeQuadData(List<BakedQuad> bakedQuads, int defaultColor, BlockState state) {
		return bakedQuads.stream().map(bakedQuad -> new TreeQuadData(bakedQuad, defaultColor, state)).collect(Collectors.toList());
	}

	public List<TreeQuadData> getQuads() {
		return quads;
	}

	public int getEntityId() {
		return entityId;
	}

	@Override
	public void setupAnim(FallingTreeEntity entityIn, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
	}

	@Override
	public void render(MatrixStack matrixStack, VertexConsumer buffer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
		int color;
		float r, g, b;
		for (TreeQuadData treeQuad : getQuads()) {
			r = 1;
			g = 1;
			b = 1;
			BakedQuad bakedQuad = treeQuad.bakedQuad;
			if (bakedQuad.hasColor()) {
				color = (species == null) ? treeQuad.color : species.colorTreeQuads(treeQuad.color, treeQuad);
				r = (float) (color >> 16 & 255) / 255.0F;
				g = (float) (color >> 8 & 255) / 255.0F;
				b = (float) (color & 255) / 255.0F;
			}
			if (bakedQuad.hasShade()) {
				float diffuse = 0.8f;
				r *= diffuse;
				g *= diffuse;
				b *= diffuse;
			}
			buffer.quad(matrixStack.peek(), bakedQuad, r, g, b, packedLight, packedOverlay);
		}
	}

	public static final class TreeQuadData {
		public final BakedQuad bakedQuad;
		public final BlockState state;
		public final int color;

		public TreeQuadData(BakedQuad bakedQuad, int color, BlockState state) {
			this.bakedQuad = bakedQuad;
			this.state = state;
			this.color = color;
		}
	}
}
