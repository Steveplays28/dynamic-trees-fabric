package io.github.steveplays28.dynamictreesfabric.models.bakedmodels;

import io.github.steveplays28.dynamictreesfabric.blocks.branches.BranchBlock;
import io.github.steveplays28.dynamictreesfabric.blocks.branches.ThickBranchBlock;
import io.github.steveplays28.dynamictreesfabric.client.ModelUtils;
import io.github.steveplays28.dynamictreesfabric.models.modeldata.ModelConnections;
import io.github.steveplays28.dynamictreesfabric.trees.Family;
import io.github.steveplays28.dynamictreesfabric.util.CoordUtils;
import io.github.steveplays28.dynamictreesfabric.util.CoordUtils.Surround;
import com.google.common.collect.Maps;
import com.mojang.math.Vector3f;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.BlockElement;
import net.minecraft.client.renderer.block.model.BlockElementFace;
import net.minecraft.client.renderer.block.model.BlockFaceUV;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.BlockModelRotation;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.core.Vec3i;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
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
public class ThickBranchBlockBakedModel extends BasicBranchBlockBakedModel {

    protected final ResourceLocation thickRingsResLoc;

    private final BakedModel[] trunksBark = new BakedModel[16]; // The trunk will always feature bark on its sides.
    private final BakedModel[] trunksTopBark = new BakedModel[16]; // The trunk will feature bark on its top when there's a branch on top of it.
    private final BakedModel[] trunksTopRings = new BakedModel[16]; // The trunk will feature rings on its top when there's no branches on top of it.
    private final BakedModel[] trunksBotRings = new BakedModel[16]; // The trunk will always feature rings on its bottom surface if nothing is below it.

    public ThickBranchBlockBakedModel(ResourceLocation modelResLoc, ResourceLocation barkResLoc, ResourceLocation ringsResLoc, ResourceLocation thickRingsResLoc) {
        super(modelResLoc, barkResLoc, ringsResLoc);
        this.thickRingsResLoc = thickRingsResLoc;
    }

    private boolean isTextureNull(@Nullable TextureAtlasSprite sprite) {
        return sprite == null || sprite.equals(ModelUtils.getTexture(new ResourceLocation("")));
    }

    @Override
    public void setupModels() {
        super.setupModels();

        TextureAtlasSprite thickRingsTexture = ModelUtils.getTexture(this.thickRingsResLoc);

        //if (isTextureNull(thickRingsTexture)){
        //thickRingsTexture = ThickRingTextureManager.uploader.getTextureAtlas().getSprite(thickRingsResLoc);
        //thickRingsTexture = ModelUtils.getTexture(thickRingsResLoc, ThickRingTextureManager.LOCATION_THICKRINGS_TEXTURE);

        if (isTextureNull(thickRingsTexture)) {
            thickRingsTexture = this.ringsTexture;
        }
        //}

        for (int i = 0; i < ThickBranchBlock.MAX_RADIUS_TICK - ThickBranchBlock.MAX_RADIUS; i++) {
            int radius = i + ThickBranchBlock.MAX_RADIUS + 1;
            trunksBark[i] = bakeTrunkBark(radius, this.barkTexture, true);
            trunksTopBark[i] = bakeTrunkBark(radius, this.barkTexture, false);
            trunksTopRings[i] = bakeTrunkRings(radius, thickRingsTexture, Direction.UP);
            trunksBotRings[i] = bakeTrunkRings(radius, thickRingsTexture, Direction.DOWN);
        }
    }

    public BakedModel bakeTrunkBark(int radius, TextureAtlasSprite bark, boolean side) {

        IModelBuilder<?> builder = ModelUtils.getModelBuilder(this.blockModel.customData, bark);
        AABB wholeVolume = new AABB(8 - radius, 0, 8 - radius, 8 + radius, 16, 8 + radius);

        final Direction[] run = side ? CoordUtils.HORIZONTALS : new Direction[]{Direction.UP, Direction.DOWN};
        ArrayList<Vec3i> offsets = new ArrayList<>();

        for (Surround dir : Surround.values()) {
            offsets.add(dir.getOffset()); // 8 surrounding component pieces
        }
        offsets.add(new Vec3i(0, 0, 0));//Center

        for (Direction face : run) {
            final Vec3i dirVector = face.getNormal();

            for (Vec3i offset : offsets) {
                if (face.getAxis() == Axis.Y || new Vec3(dirVector.getX(), dirVector.getY(), dirVector.getZ()).add(new Vec3(offset.getX(), offset.getY(), offset.getZ())).lengthSqr() > 2.25) { //This means that the dir and face share a common direction
                    Vec3 scaledOffset = new Vec3(offset.getX() * 16, offset.getY() * 16, offset.getZ() * 16);//Scale the dimensions to match standard minecraft texels
                    AABB partBoundary = new AABB(0, 0, 0, 16, 16, 16).move(scaledOffset).intersect(wholeVolume);

                    Vector3f[] limits = ModelUtils.AABBLimits(partBoundary);

                    Map<Direction, BlockElementFace> mapFacesIn = Maps.newEnumMap(Direction.class);

                    BlockFaceUV uvface = new BlockFaceUV(ModelUtils.modUV(ModelUtils.getUVs(partBoundary, face)), getFaceAngle(Axis.Y, face));
                    mapFacesIn.put(face, new BlockElementFace(null, -1, null, uvface));

                    BlockElement part = new BlockElement(limits[0], limits[1], mapFacesIn, null, true);
                    builder.addCulledFace(face, ModelUtils.makeBakedQuad(part, part.faces.get(face), bark, face, BlockModelRotation.X0_Y0, this.modelResLoc));
                }

            }
        }

        return builder.build();
    }

    public BakedModel bakeTrunkRings(int radius, TextureAtlasSprite ring, Direction face) {
        IModelBuilder<?> builder = ModelUtils.getModelBuilder(this.blockModel.customData, ring);
        AABB wholeVolume = new AABB(8 - radius, 0, 8 - radius, 8 + radius, 16, 8 + radius);
        int wholeVolumeWidth = 48;

        ArrayList<Vec3i> offsets = new ArrayList<>();

        for (Surround dir : Surround.values()) {
            offsets.add(dir.getOffset()); // 8 surrounding component pieces
        }
        offsets.add(new Vec3i(0, 0, 0)); // Center

        for (Vec3i offset : offsets) {
            Vec3 scaledOffset = new Vec3(offset.getX() * 16, offset.getY() * 16, offset.getZ() * 16); // Scale the dimensions to match standard minecraft texels
            AABB partBoundary = new AABB(0, 0, 0, 16, 16, 16).move(scaledOffset).intersect(wholeVolume);

            Vector3f posFrom = new Vector3f((float) partBoundary.minX, (float) partBoundary.minY, (float) partBoundary.minZ);
            Vector3f posTo = new Vector3f((float) partBoundary.maxX, (float) partBoundary.maxY, (float) partBoundary.maxZ);

            Map<Direction, BlockElementFace> mapFacesIn = Maps.newEnumMap(Direction.class);
            float textureOffsetX = -16f;
            float textureOffsetZ = -16f;

            float minX = ((float) ((partBoundary.minX - textureOffsetX) / wholeVolumeWidth)) * 16f;
            float maxX = ((float) ((partBoundary.maxX - textureOffsetX) / wholeVolumeWidth)) * 16f;
            float minZ = ((float) ((partBoundary.minZ - textureOffsetZ) / wholeVolumeWidth)) * 16f;
            float maxZ = ((float) ((partBoundary.maxZ - textureOffsetZ) / wholeVolumeWidth)) * 16f;

            if (face == Direction.DOWN) {
                minZ = ((float) ((partBoundary.maxZ - textureOffsetZ) / wholeVolumeWidth)) * 16f;
                maxZ = ((float) ((partBoundary.minZ - textureOffsetZ) / wholeVolumeWidth)) * 16f;
            }

            float[] uvs = new float[]{minX, minZ, maxX, maxZ};

            BlockFaceUV uvface = new BlockFaceUV(uvs, getFaceAngle(Axis.Y, face));
            mapFacesIn.put(face, new BlockElementFace(null, -1, null, uvface));

            BlockElement part = new BlockElement(posFrom, posTo, mapFacesIn, null, true);
            builder.addCulledFace(face, ModelUtils.makeBakedQuad(part, part.faces.get(face), ring, face, BlockModelRotation.X0_Y0, this.modelResLoc));
        }

        return builder.build();
    }

    @Nonnull
    @Override
    public List<BakedQuad> getQuads(@Nullable final BlockState state, @Nullable final Direction side, final RandomSource rand, final ModelData extraData, @Nullable RenderType renderType) {
        if (state == null || side != null) {
            return Collections.emptyList();
        }

        int coreRadius = this.getRadius(state);

        if (coreRadius <= BranchBlock.MAX_RADIUS) {
            return super.getQuads(state, null, rand, extraData, renderType);
        }

        coreRadius = Mth.clamp(coreRadius, 9, 24);

        List<BakedQuad> quads = new ArrayList<>(30);

        int[] connections = new int[]{0, 0, 0, 0, 0, 0};
        Direction forceRingDir = null;
        int twigRadius = 1;

        ModelConnections connectionsData = extraData.get(ModelConnections.CONNECTIONS_PROPERTY);
        if (connectionsData != null) {
            connections = connectionsData.getAllRadii();
            forceRingDir = connectionsData.getRingOnly();
            Family family = connectionsData.getFamily();
            if (family.isValid()) {
                twigRadius = family.getPrimaryThickness();
            }
        }

        //Count number of connections
        int numConnections = 0;
        for (int i : connections) {
            numConnections += (i != 0) ? 1 : 0;
        }

        if (numConnections == 0 && forceRingDir != null) {
            return quads;
        }

        if (forceRingDir != null) {
            connections[forceRingDir.get3DDataValue()] = 0;
            quads.addAll(this.trunksBotRings[coreRadius - 9].getQuads(state, forceRingDir, rand, extraData, renderType));
        }

        boolean branchesAround = connections[2] + connections[3] + connections[4] + connections[5] != 0;
        for (Direction face : Direction.values()) {
            quads.addAll(this.trunksBark[coreRadius - 9].getQuads(state, face, rand, extraData, renderType));
            if (face == Direction.UP || face == Direction.DOWN) {
                if (connections[face.get3DDataValue()] < twigRadius && !branchesAround) {
                    quads.addAll(this.trunksTopRings[coreRadius - 9].getQuads(state, face, rand, extraData, renderType));
                } else if (connections[face.get3DDataValue()] < coreRadius) {
                    quads.addAll(this.trunksTopBark[coreRadius - 9].getQuads(state, face, rand, extraData, renderType));
                }
            }
        }

        return quads;
    }

}
