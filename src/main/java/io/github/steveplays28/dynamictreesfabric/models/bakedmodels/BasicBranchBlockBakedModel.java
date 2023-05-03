package io.github.steveplays28.dynamictreesfabric.models.bakedmodels;

import io.github.steveplays28.dynamictreesfabric.blocks.branches.BasicBranchBlock;
import io.github.steveplays28.dynamictreesfabric.blocks.branches.BranchBlock;
import io.github.steveplays28.dynamictreesfabric.client.ModelUtils;
import io.github.steveplays28.dynamictreesfabric.models.modeldata.ModelConnections;
import com.google.common.collect.Maps;
import com.mojang.math.Vector3f;
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
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Direction.Axis;
import net.minecraft.util.math.Direction.AxisDirection;
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
import java.util.concurrent.atomic.AtomicInteger;

@OnlyIn(Dist.CLIENT)
public class BasicBranchBlockBakedModel extends BranchBlockBakedModel {

    protected Sprite barkTexture;
    protected Sprite ringsTexture;

    // 74 Baked models per tree family to achieve this. I guess it's not my problem.  Wasn't my idea anyway.
    private final BakedModel[][] sleeves = new BakedModel[6][7];
    private final BakedModel[][] cores = new BakedModel[3][8]; // 8 Cores for 3 axis with the bark texture all all 6 sides rotated appropriately.
    private final BakedModel[] rings = new BakedModel[8]; // 8 Cores with the ring textures on all 6 sides.

    public BasicBranchBlockBakedModel(Identifier modelResLoc, Identifier barkResLoc, Identifier ringsResLoc) {
        super(modelResLoc, barkResLoc, ringsResLoc);
    }

    @Override
    public void setupModels() {
        this.barkTexture = ModelUtils.getTexture(this.barkResLoc);
        this.ringsTexture = ModelUtils.getTexture(this.ringsResLoc);

        for (int i = 0; i < 8; i++) {
            int radius = i + 1;
            if (radius < 8) {
                for (Direction dir : Direction.values()) {
                    sleeves[dir.getId()][i] = bakeSleeve(radius, dir, barkTexture);
                }
            }
            cores[0][i] = bakeCore(radius, Axis.Y, barkTexture); //DOWN<->UP
            cores[1][i] = bakeCore(radius, Axis.Z, barkTexture); //NORTH<->SOUTH
            cores[2][i] = bakeCore(radius, Axis.X, barkTexture); //WEST<->EAST

            rings[i] = bakeCore(radius, Axis.Y, this.ringsTexture);
        }
    }

    public BakedModel bakeSleeve(int radius, Direction dir, Sprite bark) {
        //Work in double units(*2)
        int dradius = radius * 2;
        int halfSize = (16 - dradius) / 2;
        int halfSizeX = dir.getOffsetX() != 0 ? halfSize : dradius;
        int halfSizeY = dir.getOffsetY() != 0 ? halfSize : dradius;
        int halfSizeZ = dir.getOffsetZ() != 0 ? halfSize : dradius;
        int move = 16 - halfSize;
        int centerX = 16 + (dir.getOffsetX() * move);
        int centerY = 16 + (dir.getOffsetY() * move);
        int centerZ = 16 + (dir.getOffsetZ() * move);

        Vector3f posFrom = new Vector3f((centerX - halfSizeX) / 2f, (centerY - halfSizeY) / 2f, (centerZ - halfSizeZ) / 2f);
        Vector3f posTo = new Vector3f((centerX + halfSizeX) / 2f, (centerY + halfSizeY) / 2f, (centerZ + halfSizeZ) / 2f);

        boolean negative = dir.getDirection() == AxisDirection.NEGATIVE;
        if (dir.getAxis() == Axis.Z) {//North/South
            negative = !negative;
        }

        Map<Direction, ModelElementFace> mapFacesIn = Maps.newEnumMap(Direction.class);

        for (Direction face : Direction.values()) {
            if (dir.getOpposite() != face) { //Discard side of sleeve that faces core
                ModelElementTexture uvface = null;
                if (dir == face) {//Side of sleeve that faces away from core
                    if (radius == 1) { //We're only interested in end faces for radius == 1
                        uvface = new ModelElementTexture(new float[]{8 - radius, 8 - radius, 8 + radius, 8 + radius}, 0);
                    }
                } else { //UV for Bark texture
                    uvface = new ModelElementTexture(new float[]{8 - radius, negative ? 16 - halfSize : 0, 8 + radius, negative ? 16 : halfSize}, getFaceAngle(dir.getAxis(), face));
                }
                if (uvface != null) {
                    mapFacesIn.put(face, new ModelElementFace(null, -1, null, uvface));
                }
            }
        }

        ModelElement part = new ModelElement(posFrom, posTo, mapFacesIn, null, true);
        IModelBuilder<?> builder = ModelUtils.getModelBuilder(this.blockModel.customData, bark);

        for (Map.Entry<Direction, ModelElementFace> e : part.faces.entrySet()) {
            Direction face = e.getKey();
            builder.addCulledFace(face, ModelUtils.makeBakedQuad(part, e.getValue(), bark, face, ModelRotation.X0_Y0, this.modelResLoc));
        }

        return builder.build();
    }

    public BakedModel bakeCore(int radius, Axis axis, Sprite icon) {

        Vector3f posFrom = new Vector3f(8 - radius, 8 - radius, 8 - radius);
        Vector3f posTo = new Vector3f(8 + radius, 8 + radius, 8 + radius);

        Map<Direction, ModelElementFace> mapFacesIn = Maps.newEnumMap(Direction.class);

        for (Direction face : Direction.values()) {
            ModelElementTexture uvface = new ModelElementTexture(new float[]{8 - radius, 8 - radius, 8 + radius, 8 + radius}, getFaceAngle(axis, face));
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

    /**
     * A Hack to determine the UV face angle for a block column on a certain axis
     *
     * @param axis
     * @param face
     * @return
     */
    public int getFaceAngle(Axis axis, Direction face) {
        if (axis == Axis.Y) { //UP / DOWN
            return 0;
        } else if (axis == Axis.Z) {//NORTH / SOUTH
            return switch (face) {
                case UP -> 0;
                case WEST -> 270;
                case DOWN -> 180;
                default -> 90;
            };
        } else { //EAST/WEST
            return (face == Direction.NORTH) ? 270 : 90;
        }
    }

    @Nonnull
    @Override
    public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, @Nonnull Random rand, @Nonnull ModelData extraData, @Nullable RenderLayer renderType) {
        if (state == null || side != null) {
            return Collections.emptyList();
        }

        final List<BakedQuad> quadsList = new ArrayList<>(24);

        final int coreRadius = getRadius(state);

        if (coreRadius > 8) {
            return Collections.emptyList();
        }

        int[] connections = new int[]{0, 0, 0, 0, 0, 0};
        Direction forceRingDir = null;
        final AtomicInteger twigRadius = new AtomicInteger(1);

        ModelConnections connectionsData = extraData.get(ModelConnections.CONNECTIONS_PROPERTY);
        if (connectionsData != null) {
            connections = connectionsData.getAllRadii();
            forceRingDir = connectionsData.getRingOnly();

            connectionsData.getFamily().ifValid(family ->
                    twigRadius.set(family.getPrimaryThickness()));
        }

        // Count number of connections.
        int numConnections = 0;
        for (int i : connections) {
            numConnections += (i != 0) ? 1 : 0;
        }

        if (numConnections == 0 && forceRingDir != null) {
            quadsList.addAll(rings[coreRadius - 1].getQuads(state, forceRingDir, rand, extraData, renderType));
        } else {
            // The source direction is the biggest connection from one of the 6 directions.
            final Direction sourceDir = getSourceDir(coreRadius, connections);
            final int coreDir = resolveCoreDir(sourceDir);

            // This is for drawing the rings on a terminating branch.
            final Direction coreRingDir = (numConnections == 1 && sourceDir != null) ? sourceDir.getOpposite() : null;

            for (Direction face : Direction.values()) {
                // Get quads for core model.
                if (coreRadius != connections[face.getId()]) {
                    if ((coreRingDir == null || coreRingDir != face)) {
                        quadsList.addAll(cores[coreDir][coreRadius - 1].getQuads(state, face, rand, extraData, renderType));
                    } else {
                        quadsList.addAll(rings[coreRadius - 1].getQuads(state, face, rand, extraData, renderType));
                    }
                }
                // Get quads for sleeves models.
                if (coreRadius != 8) { // Special case for r!=8.. If it's a solid block so it has no sleeves.
                    for (Direction connDir : Direction.values()) {
                        final int idx = connDir.getId();
                        final int connRadius = connections[idx];
                        // If the connection side matches the quadpull side then cull the sleeve face.  Don't cull radius 1 connections for leaves (which are partly transparent).
                        if (connRadius > 0 && (connRadius == twigRadius.get() || face != connDir)) {
                            quadsList.addAll(sleeves[idx][connRadius - 1].getQuads(state, face, rand, extraData, renderType));
                        }
                    }
                }

            }
        }

        return quadsList;
    }


    /**
     * Checks all neighboring tree parts to determine the connection radius for each side of this branch block.
     */
    @Nonnull
    @Override
    public ModelData getModelData(@Nonnull BlockRenderView world, @Nonnull BlockPos pos, @Nonnull BlockState state, @Nonnull ModelData tileData) {
        ModelConnections modelConnections;
        if (state.getBlock() instanceof BranchBlock branchBlock) {
            modelConnections = new ModelConnections(branchBlock.getConnectionData(world, pos, state)).setFamily(branchBlock.getFamily());
        } else {
            modelConnections = new ModelConnections();
        }

        return modelConnections.toModelData(tileData);
    }

    /**
     * Locates the side with the largest neighbor radius that's equal to or greater than this branch block
     *
     * @param coreRadius
     * @param connections an array of 6 integers, one for the radius of each connecting side. DUNSWE.
     * @return
     */
    @Nullable
    protected Direction getSourceDir(int coreRadius, int[] connections) {
        int largestConnection = 0;
        Direction sourceDir = null;

        for (Direction dir : Direction.values()) {
            int connRadius = connections[dir.getId()];
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
    protected int resolveCoreDir(@Nullable Direction dir) {
        if (dir == null) {
            return 0;
        }
        return dir.getId() >> 1;
    }

    protected int getRadius(BlockState blockState) {
        // This way works with branches that don't have the RADIUS property, like cactus
        return ((BasicBranchBlock) blockState.getBlock()).getRadius(blockState);
    }

    @Override
    public boolean useAmbientOcclusion() {
        return true;
    }

    @Override
    public Sprite getParticleIcon() {
        return barkTexture;
    }

    @Override
    public boolean isGui3d() {
        return false;
    }

    @Override
    public boolean isCustomRenderer() {
        return false;
    }

    @Nonnull
    @Override
    public ModelOverrideList getOverrides() {
        return ModelOverrideList.EMPTY;
    }

    @Override
    public boolean usesBlockLight() {
        return false;
    }

}
