package io.github.steveplays28.dynamictreesfabric.client;

import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.VertexFormatElement;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.BakedModelManager;
import net.minecraft.client.render.model.BakedQuad;
import net.minecraft.client.texture.Sprite;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.model.data.ModelData;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

@OnlyIn(Dist.CLIENT)
public class QuadManipulator {

    public static final Direction[] everyFace = {Direction.DOWN, Direction.UP, Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST, null};

    public static List<BakedQuad> getQuads(BakedModel modelIn, BlockState stateIn, ModelData modelData) {
        return getQuads(modelIn, stateIn, Vec3d.ZERO, everyFace, Random.create(), modelData);
    }

    public static List<BakedQuad> getQuads(BakedModel modelIn, BlockState stateIn, Direction[] sides, ModelData modelData) {
        return getQuads(modelIn, stateIn, Vec3d.ZERO, sides, Random.create(), modelData);
    }

    public static List<BakedQuad> getQuads(BakedModel modelIn, BlockState stateIn, Random rand, ModelData modelData) {
        return getQuads(modelIn, stateIn, Vec3d.ZERO, everyFace, rand, modelData);
    }

    public static List<BakedQuad> getQuads(BakedModel modelIn, BlockState stateIn, Vec3d offset, Random rand, ModelData modelData) {
        return getQuads(modelIn, stateIn, offset, everyFace, rand, modelData);
    }

    public static List<BakedQuad> getQuads(BakedModel modelIn, BlockState stateIn, Vec3d offset, ModelData modelData) {
        return getQuads(modelIn, stateIn, offset, everyFace, Random.create(), modelData);
    }

    public static List<BakedQuad> getQuads(BakedModel modelIn, BlockState stateIn, Vec3d offset, Direction[] sides, ModelData modelData) {
        return getQuads(modelIn, stateIn, offset, sides, Random.create(), modelData);
    }

    public static List<BakedQuad> getQuads(BakedModel modelIn, BlockState stateIn, Vec3d offset, Direction[] sides, Random rand, ModelData modelData) {
        ArrayList<BakedQuad> outQuads = new ArrayList<>();

        if (stateIn != null) {
            for (Direction dir : sides) {
                outQuads.addAll(modelIn.getQuads(stateIn, dir, rand, modelData, null));
            }
        }

        return offset.equals(Vec3d.ZERO) ? outQuads : moveQuads(outQuads, offset);
    }

    public static List<BakedQuad> moveQuads(List<BakedQuad> inQuads, Vec3d offset) {
        ArrayList<BakedQuad> outQuads = new ArrayList<>();

        for (BakedQuad inQuad : inQuads) {
            BakedQuad quadCopy = new BakedQuad(inQuad.getVertexData().clone(), inQuad.getColorIndex(), inQuad.getFace(), inQuad.getSprite(), inQuad.hasShade());
            int[] vertexData = quadCopy.getVertexData();
            for (int i = 0; i < vertexData.length; i += VertexFormats.POSITION_COLOR_TEXTURE_LIGHT_NORMAL.getVertexSizeInteger()) {
                int pos = 0;
                for (VertexFormatElement vfe : VertexFormats.POSITION_COLOR_TEXTURE_LIGHT_NORMAL.getElements()) {
                    if (vfe.getType() == VertexFormatElement.Type.POSITION) {
                        float x = Float.intBitsToFloat(vertexData[i + pos + 0]);
                        float y = Float.intBitsToFloat(vertexData[i + pos + 1]);
                        float z = Float.intBitsToFloat(vertexData[i + pos + 2]);
                        x += offset.x;
                        y += offset.y;
                        z += offset.z;
                        vertexData[i + pos + 0] = Float.floatToIntBits(x);
                        vertexData[i + pos + 1] = Float.floatToIntBits(y);
                        vertexData[i + pos + 2] = Float.floatToIntBits(z);
                        break;
                    }
                    pos += vfe.getByteLength() / 4;//Size is always in bytes but we are dealing with an array of int32s
                }
            }

            outQuads.add(quadCopy);
        }

        outQuads.trimToSize();
        return outQuads;
    }

    public static BakedModel getModelForState(BlockState state) {
        BakedModel model = null;

        try {
            model = getModel(state);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return model;
    }

    public static BakedModelManager getModelManager() {
        return MinecraftClient.getInstance().getBakedModelManager();
    }

    public static BakedModel getModel(BlockState state) {
        return MinecraftClient.getInstance().getBlockRenderManager().getModel(state);//This gives us earlier access
    }

    public static Identifier getModelTexture(BakedModel model, Function<Identifier, Sprite> bakedTextureGetter, BlockState state, Direction dir) {

        float[] uvs = getSpriteUVFromBlockState(state, dir);

        if (uvs != null) {
            List<Sprite> sprites = new ArrayList<>();

            float closest = Float.POSITIVE_INFINITY;
            Identifier closestTex = new Identifier("missingno");
            if (model != null) {
                Identifier tex = model.getParticleSprite(ModelData.EMPTY).getName();
                Sprite tas = bakedTextureGetter.apply(tex);
                float u = tas.getFrameU(8);
                float v = tas.getFrameV(8);
                sprites.add(tas);
                float du = u - uvs[0];
                float dv = v - uvs[1];
                float distSq = du * du + dv * dv;
                if (distSq < closest) {
                    closest = distSq;
                    closestTex = tex;
                }
            }

            return closestTex;
        }

        return null;
    }

    public static float[] getSpriteUVFromBlockState(BlockState state, Direction side) {
        BakedModel bakedModel = getModelManager().getBlockModels().getModel(state);
        List<BakedQuad> quads = new ArrayList<>();
        Random random = Random.create();
        quads.addAll(bakedModel.getQuads(state, side, random, ModelData.EMPTY, null));
        quads.addAll(bakedModel.getQuads(state, null, random, ModelData.EMPTY, null));

        Optional<BakedQuad> quad = quads.stream().filter(q -> q.getFace() == side).findFirst();

        if (quad.isPresent()) {

            float u = 0.0f;
            float v = 0.0f;

            int[] vertexData = quad.get().getVertexData();
            int numVertices = 0;
            for (int i = 0; i < vertexData.length; i += VertexFormats.POSITION_COLOR_TEXTURE_LIGHT_NORMAL.getVertexSizeInteger()) {
                int pos = 0;
                for (VertexFormatElement vfe : VertexFormats.POSITION_COLOR_TEXTURE_LIGHT_NORMAL.getElements()) {
                    if (vfe.getType() == VertexFormatElement.Type.UV) {
                        u += Float.intBitsToFloat(vertexData[i + pos + 0]);
                        v += Float.intBitsToFloat(vertexData[i + pos + 1]);
                    }
                    pos += vfe.getByteLength() / 4;//Size is always in bytes but we are dealing with an array of int32s
                }
                numVertices++;
            }

            return new float[]{u / numVertices, v / numVertices};
        }

        System.err.println("Warning: Could not get \"" + side + "\" side quads from blockstate: " + state);

        return null;
    }

}
