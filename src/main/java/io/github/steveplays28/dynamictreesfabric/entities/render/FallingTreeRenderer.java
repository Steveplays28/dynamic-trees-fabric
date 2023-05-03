package io.github.steveplays28.dynamictreesfabric.entities.render;

import com.mojang.blaze3d.systems.RenderSystem;
import io.github.steveplays28.dynamictreesfabric.entities.FallingTreeEntity;
import io.github.steveplays28.dynamictreesfabric.models.FallingTreeEntityModel;
import io.github.steveplays28.dynamictreesfabric.models.FallingTreeEntityModelTrackerCache;

import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public class FallingTreeRenderer extends EntityRenderer<FallingTreeEntity> {

	public FallingTreeRenderer(EntityRendererFactory.Context renderManager) {
		super(renderManager);
	}

	@Override
	public Identifier getTextureLocation(FallingTreeEntity entity) {
		return SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE;
	}

	@Override
	public void render(FallingTreeEntity entity, float entityYaw, float partialTicks, MatrixStack matrixStack, VertexConsumerProvider buffer, int packedLight) {
		super.render(entity, entityYaw, partialTicks, matrixStack, buffer, packedLight);

		if (!entity.isClientBuilt() || !entity.shouldRender()) {
			return;
		}

		RenderSystem.setShaderTexture(0, this.getTextureLocation(entity));

		final FallingTreeEntityModel treeModel = FallingTreeEntityModelTrackerCache.getOrCreateModel(entity);

		matrixStack.push();

		final VertexConsumer vertexBuilder = buffer.getBuffer(RenderLayer.getEntityCutout(this.getTextureLocation(entity)));

//		if (entity.onFire) {
//			renderFire(matrixStack, vertexBuilder);
//		}

		entity.currentAnimationHandler.renderTransform(entity, entityYaw, partialTicks, matrixStack);

		treeModel.render(matrixStack, vertexBuilder, packedLight, OverlayTexture.DEFAULT_UV, 1, 1, 1, 1.0F);

		matrixStack.pop();
	}

//	private void renderFire(MatrixStack matrixStack, IVertexBuilder buffer) {
//		matrixStack.push();
//		matrixStack.translate(-0.5f, 0.0f, -0.5f);
//		BlockRendererDispatcher dispatcher = Minecraft.getInstance().getBlockRendererDispatcher();
//		BlockState fire = Blocks.FIRE.getDefaultState();
//		IBakedModel model = dispatcher.getModelForState(fire);
//
//		drawBakedQuads(QuadManipulator.getQuads(model, fire, EmptyModelData.INSTANCE), matrixStack,255, 0xFFFFFFFF);
//		matrixStack.pop();
//	}

//    public static class Factory implements IRenderFactory<FallingTreeEntity> {
//
//        @Override
//        public EntityRenderer<FallingTreeEntity> createRenderFor(EntityRendererProvider.Context manager) {
//            return new FallingTreeRenderer(manager);
//        }
//
//    }

}

