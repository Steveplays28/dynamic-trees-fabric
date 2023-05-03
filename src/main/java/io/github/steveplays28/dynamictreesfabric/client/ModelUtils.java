package io.github.steveplays28.dynamictreesfabric.client;

import com.mojang.math.Vector3f;
import net.minecraftforge.client.RenderTypeGroup;
import net.minecraftforge.client.model.IModelBuilder;
import net.minecraftforge.client.model.geometry.IGeometryBakingContext;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.model.BakedQuad;
import net.minecraft.client.render.model.BakedQuadFactory;
import net.minecraft.client.render.model.ModelRotation;
import net.minecraft.client.render.model.json.ModelElement;
import net.minecraft.client.render.model.json.ModelElementFace;
import net.minecraft.client.render.model.json.ModelOverrideList;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Direction.Axis;

public class ModelUtils {

	public static float[] getUVs(Box box, Direction face) {
		switch (face) {
			default:
			case DOWN:
				return new float[]{(float) box.minX, 16f - (float) box.minZ, (float) box.maxX, 16f - (float) box.maxZ};
			case UP:
				return new float[]{(float) box.minX, (float) box.minZ, (float) box.maxX, (float) box.maxZ};
			case NORTH:
				return new float[]{16f - (float) box.maxX, (float) box.minY, 16f - (float) box.minX, (float) box.maxY};
			case SOUTH:
				return new float[]{(float) box.minX, (float) box.minY, (float) box.maxX, (float) box.maxY};
			case WEST:
				return new float[]{(float) box.minZ, (float) box.minY, (float) box.maxZ, (float) box.maxY};
			case EAST:
				return new float[]{16f - (float) box.maxZ, (float) box.minY, 16f - (float) box.minZ, (float) box.maxY};
		}
	}

	/**
	 * A Hack to determine the UV face angle for a block column on a certain axis
	 *
	 * @param axis
	 * @param face
	 * @return
	 */
	public static int getFaceAngle(Axis axis, Direction face) {
		if (axis == Axis.Y) { //UP / DOWN
			return 0;
		} else if (axis == Axis.Z) {//NORTH / SOUTH
			switch (face) {
				case UP:
					return 0;
				case WEST:
					return 270;
				case DOWN:
					return 180;
				case NORTH:
					return 270;
				default:
					return 90;
			}
		} else { //EAST/WEST
			return (face == Direction.NORTH) ? 270 : 90;
		}
	}

	public static float[] modUV(float[] uvs) {
		uvs[0] = (int) uvs[0] & 0xf;
		uvs[1] = (int) uvs[1] & 0xf;
		uvs[2] = (((int) uvs[2] - 1) & 0xf) + 1;
		uvs[3] = (((int) uvs[3] - 1) & 0xf) + 1;
		return uvs;
	}

	public static Vector3f[] AABBLimits(Box aabb) {
		return new Vector3f[]{
				new Vector3f((float) aabb.minX, (float) aabb.minY, (float) aabb.minZ),
				new Vector3f((float) aabb.maxX, (float) aabb.maxY, (float) aabb.maxZ),
		};
	}

	public static BakedQuad makeBakedQuad(ModelElement blockPart, ModelElementFace partFace, Sprite atlasSprite, Direction dir, ModelRotation modelRotation, Identifier modelResLoc) {
		return new BakedQuadFactory().bake(blockPart.from, blockPart.to, partFace, atlasSprite, dir, modelRotation, blockPart.rotation, true, modelResLoc);
	}

	public static IModelBuilder<?> getModelBuilder(IGeometryBakingContext context, Sprite particle) {
		Identifier renderTypeHint = context.getRenderTypeHint();
		RenderTypeGroup renderTypes = renderTypeHint != null ? context.getRenderType(renderTypeHint) : RenderTypeGroup.EMPTY;

		return IModelBuilder.of(context.useAmbientOcclusion(), context.useBlockLight(), context.isGui3d(),
				context.getTransforms(), ModelOverrideList.EMPTY, particle, renderTypes);
	}

	@SuppressWarnings("deprecation")
	public static Sprite getTexture(Identifier resLoc) {
		return getTexture(resLoc, SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE);
	}

	public static Sprite getTexture(Identifier resLoc, Identifier atlasResLoc) {
		return MinecraftClient.getInstance().getSpriteAtlas(atlasResLoc).apply(resLoc);
	}

}
