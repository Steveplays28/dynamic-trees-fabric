package io.github.steveplays28.dynamictreesfabric.models.bakedmodels;

import com.google.common.collect.Maps;
import com.mojang.math.Vector3f;
import io.github.steveplays28.dynamictreesfabric.blocks.branches.SurfaceRootBlock;
import io.github.steveplays28.dynamictreesfabric.client.ModelUtils;
import io.github.steveplays28.dynamictreesfabric.util.CoordUtils;
import io.github.steveplays28.dynamictreesfabric.util.RootConnections;

import net.minecraft.block.BlockState;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.BakedQuad;
import net.minecraft.client.render.model.ModelRotation;
import net.minecraft.client.render.model.json.ModelElement;
import net.minecraft.client.render.model.json.ModelElementFace;
import net.minecraft.client.render.model.json.ModelElementTexture;
import net.minecraft.client.render.model.json.ModelOverrideList;
import net.minecraft.client.texture.Sprite;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.BlockRenderView;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.model.IModelBuilder;
import net.minecraftforge.client.model.data.ModelData;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@OnlyIn(Dist.CLIENT)
public class RootBlockBakedModel extends BranchBlockBakedModel {

	private final BakedModel[][] sleeves = new BakedModel[4][7];
	private final BakedModel[][] cores = new BakedModel[2][8]; //8 Cores for 2 axis(X, Z) with the bark texture on all 6 sides rotated appropriately.
	private final BakedModel[][] verts = new BakedModel[4][8];
	private Sprite barkTexture;

	public RootBlockBakedModel(Identifier modelResLoc, Identifier barkResLoc) {
		super(modelResLoc, barkResLoc, null);
	}

	@Override
	public void setupModels() {
		this.barkTexture = ModelUtils.getTexture(this.barkResLoc);

		for (int r = 0; r < 8; r++) {
			int radius = r + 1;
			if (radius < 8) {
				for (Direction dir : CoordUtils.HORIZONTALS) {
					int horIndex = dir.getHorizontal();
					sleeves[horIndex][r] = bakeSleeve(radius, dir);
					verts[horIndex][r] = bakeVert(radius, dir);
				}
			}
			cores[0][r] = bakeCore(radius, Direction.Axis.Z, this.barkTexture); //NORTH<->SOUTH
			cores[1][r] = bakeCore(radius, Direction.Axis.X, this.barkTexture); //WEST<->EAST
		}
	}

	public int getRadialHeight(int radius) {
		return radius * 2;
	}

	public BakedModel bakeSleeve(int radius, Direction dir) {
		int radialHeight = getRadialHeight(radius);

		//Work in double units(*2)
		int dradius = radius * 2;
		int halfSize = (16 - dradius) / 2;
		int halfSizeX = dir.getOffsetX() != 0 ? halfSize : dradius;
		int halfSizeZ = dir.getOffsetZ() != 0 ? halfSize : dradius;
		int move = 16 - halfSize;
		int centerX = 16 + (dir.getOffsetX() * move);
		int centerZ = 16 + (dir.getOffsetZ() * move);

		Vector3f posFrom = new Vector3f((centerX - halfSizeX) / 2, 0, (centerZ - halfSizeZ) / 2);
		Vector3f posTo = new Vector3f((centerX + halfSizeX) / 2, radialHeight, (centerZ + halfSizeZ) / 2);

		boolean sleeveNegative = dir.getDirection() == Direction.AxisDirection.NEGATIVE;
		if (dir.getAxis() == Direction.Axis.Z) {// North/South
			sleeveNegative = !sleeveNegative;
		}

		Map<Direction, ModelElementFace> mapFacesIn = Maps.newEnumMap(Direction.class);

		for (Direction face : Direction.values()) {
			if (dir.getOpposite() != face) { //Discard side of sleeve that faces core
				ModelElementTexture uvface = null;
				if (face.getAxis().isHorizontal()) {
					boolean facePositive = face.getDirection() == Direction.AxisDirection.POSITIVE;
					uvface = new ModelElementTexture(new float[]{facePositive ? 16 - radialHeight : 0, (sleeveNegative ? 16 - halfSize : 0), facePositive ? 16 : radialHeight, (sleeveNegative ? 16 : halfSize)}, ModelUtils.getFaceAngle(dir.getAxis(), face));
				} else {
					uvface = new ModelElementTexture(new float[]{8 - radius, sleeveNegative ? 16 - halfSize : 0, 8 + radius, sleeveNegative ? 16 : halfSize}, ModelUtils.getFaceAngle(dir.getAxis(), face));
				}
				if (uvface != null) {
					mapFacesIn.put(face, new ModelElementFace(null, -1, null, uvface));
				}
			}
		}

		ModelElement part = new ModelElement(posFrom, posTo, mapFacesIn, null, true);
		IModelBuilder<?> builder = ModelUtils.getModelBuilder(this.blockModel.customData, this.barkTexture);

		for (Map.Entry<Direction, ModelElementFace> e : part.faces.entrySet()) {
			Direction face = e.getKey();
			builder.addCulledFace(face, ModelUtils.makeBakedQuad(part, e.getValue(), this.barkTexture, face, ModelRotation.X0_Y0, this.modelResLoc));
		}

		return builder.build();
	}

	private BakedModel bakeVert(int radius, Direction dir) {
		int radialHeight = getRadialHeight(radius);
		IModelBuilder<?> builder = ModelUtils.getModelBuilder(this.blockModel.customData, this.barkTexture);

		Box partBoundary = new Box(8 - radius, radialHeight, 8 - radius, 8 + radius, 16 + radialHeight, 8 + radius)
				.offset(dir.getOffsetX() * 7, 0, dir.getOffsetZ() * 7);

		for (int i = 0; i < 2; i++) {
			Box pieceBoundary = partBoundary.intersection(new Box(0, 0, 0, 16, 16, 16).offset(0, 16 * i, 0));

			for (Direction face : Direction.values()) {
				Map<Direction, ModelElementFace> mapFacesIn = Maps.newEnumMap(Direction.class);

				ModelElementTexture uvface = new ModelElementTexture(ModelUtils.modUV(ModelUtils.getUVs(pieceBoundary, face)), ModelUtils.getFaceAngle(Direction.Axis.Y, face));
				mapFacesIn.put(face, new ModelElementFace(null, -1, null, uvface));

				Vector3f[] limits = ModelUtils.AABBLimits(pieceBoundary);

				ModelElement part = new ModelElement(limits[0], limits[1], mapFacesIn, null, true);
				builder.addCulledFace(face, ModelUtils.makeBakedQuad(part, part.faces.get(face), this.barkTexture, face, ModelRotation.X0_Y0, this.modelResLoc));
			}
		}

		return builder.build();
	}

	public BakedModel bakeCore(int radius, Direction.Axis axis, Sprite icon) {
		int radialHeight = getRadialHeight(radius);

		Vector3f posFrom = new Vector3f(8 - radius, 0, 8 - radius);
		Vector3f posTo = new Vector3f(8 + radius, radialHeight, 8 + radius);

		Map<Direction, ModelElementFace> mapFacesIn = Maps.newEnumMap(Direction.class);

		for (Direction face : Direction.values()) {
			ModelElementTexture uvface;
			if (face.getAxis().isHorizontal()) {
				boolean positive = face.getDirection() == Direction.AxisDirection.POSITIVE;
				uvface = new ModelElementTexture(new float[]{positive ? 16 - radialHeight : 0, 8 - radius, positive ? 16 : radialHeight, 8 + radius}, ModelUtils.getFaceAngle(axis, face));
			} else {
				uvface = new ModelElementTexture(new float[]{8 - radius, 8 - radius, 8 + radius, 8 + radius}, ModelUtils.getFaceAngle(axis, face));
			}

			mapFacesIn.put(face, new ModelElementFace(null, -1, null, uvface));
		}

		ModelElement part = new ModelElement(posFrom, posTo, mapFacesIn, null, true);
		IModelBuilder<?> builder = ModelUtils.getModelBuilder(this.blockModel.customData, icon);

		for (Map.Entry<Direction, ModelElementFace> e : part.faces.entrySet()) {
			Direction face = e.getKey();
			builder.addCulledFace(face, ModelUtils.makeBakedQuad(part, e.getValue(), icon, face, ModelRotation.X0_Y0, this.modelResLoc));
		}

		return builder.build();
	}

	@Nonnull
	@Override
	public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, @Nonnull Random rand, @Nonnull ModelData extraData, @Nullable RenderLayer renderType) {
		if (side != null || state == null) {
			return Collections.emptyList();
		}

		final List<BakedQuad> quads = new ArrayList<>(24);

		int coreRadius = this.getRadius(state);

		int[] connections = new int[]{0, 0, 0, 0};
		RootConnections.ConnectionLevel[] connectionLevels = RootConnections.PLACEHOLDER_CONNECTION_LEVELS.clone();
		RootConnections connectionData = extraData.get(RootConnections.ROOT_CONNECTIONS_PROPERTY);
		if (connectionData != null) {
			connections = connectionData.getAllRadii();
			connectionLevels = connectionData.getConnectionLevels();
		}

		for (int i = 0; i < connections.length; i++) {
			connections[i] = MathHelper.clamp(connections[i], 0, coreRadius);
		}

		//The source direction is the biggest connection from one of the 6 directions
		Direction sourceDir = this.getSourceDir(coreRadius, connections);
		if (sourceDir == null) {
			sourceDir = Direction.DOWN;
		}
		int coreDir = this.resolveCoreDir(sourceDir);

		boolean isGrounded = state.get(SurfaceRootBlock.GROUNDED) == Boolean.TRUE;

		for (Direction face : Direction.values()) {
			//Get quads for core model
			if (isGrounded) {
				quads.addAll(cores[coreDir][coreRadius - 1].getQuads(state, face, rand, extraData, renderType));
			}

			//Get quads for sleeves models
			if (coreRadius != 8) { //Special case for r!=8.. If it's a solid block so it has no sleeves
				for (Direction connDir : CoordUtils.HORIZONTALS) {
					int idx = connDir.getHorizontal();
					int connRadius = connections[idx];
					//If the connection side matches the quadpull side then cull the sleeve face.  Don't cull radius 1 connections for leaves(which are partly transparent).
					if (connRadius > 0) {//  && (connRadius == 1 || side != connDir)) {
						if (isGrounded) {
							quads.addAll(sleeves[idx][connRadius - 1].getQuads(state, face, rand, extraData, renderType));
						}
						if (connectionLevels[idx] == RootConnections.ConnectionLevel.HIGH) {
							quads.addAll(verts[idx][connRadius - 1].getQuads(state, face, rand, extraData, renderType));
						}
					}
				}
			}
		}

		return quads;
	}

	@Nonnull
	@Override
	public ModelData getModelData(@Nonnull BlockRenderView world, @Nonnull BlockPos pos, @Nonnull BlockState state, @Nonnull ModelData tileData) {
		RootConnections rootConnections = state.getBlock() instanceof SurfaceRootBlock surfaceRootBlock
				? new RootConnections(surfaceRootBlock.getConnectionData(world, pos))
				: new RootConnections();
		return ModelData.builder().with(RootConnections.ROOT_CONNECTIONS_PROPERTY, rootConnections).build();
	}

	/**
	 * Locates the side with the largest neighbor radius that's equal to or greater than this branch block
	 *
	 * @param coreRadius
	 * @param connections an array of 6 integers, one for the radius of each connecting side. DUNSWE.
	 * @return
	 */
	protected Direction getSourceDir(int coreRadius, int[] connections) {
		int largestConnection = 0;
		Direction sourceDir = null;

		for (Direction dir : CoordUtils.HORIZONTALS) {
			int horIndex = dir.getHorizontal();
			int connRadius = connections[horIndex];
			if (connRadius > largestConnection) {
				largestConnection = connRadius;
				sourceDir = dir;
			}
		}

		if (largestConnection < coreRadius) {
			sourceDir = null;//Has no source node
		}
		return sourceDir;
	}

	/**
	 * Converts direction DUNSWE to 3 axis numbers for Y,Z,X
	 *
	 * @param dir
	 * @return
	 */
	protected int resolveCoreDir(Direction dir) {
		return dir.getAxis() == Direction.Axis.X ? 1 : 0;
	}

	protected int getRadius(BlockState blockState) {
		// This way works with branches that don't have the RADIUS property, like cactus
		return ((SurfaceRootBlock) blockState.getBlock()).getRadius(blockState);
	}

	@Override
	public boolean useAmbientOcclusion() {
		return true;
	}

	@Override
	public boolean isGui3d() {
		return false;
	}

	@Override
	public boolean isCustomRenderer() {
		return true;
	}

	@Override
	public Sprite getParticleIcon() {
		return this.barkTexture;
	}

	@Override
	public ModelOverrideList getOverrides() {
		return ModelOverrideList.EMPTY;
	}

	@Override
	public boolean usesBlockLight() {
		return false;
	}

}
