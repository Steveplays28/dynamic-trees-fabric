package io.github.steveplays28.dynamictreesfabric.entities.animation;

import io.github.steveplays28.dynamictreesfabric.entities.FallingTreeEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import net.minecraft.client.util.math.MatrixStack;

public class VoidAnimationHandler implements AnimationHandler {

	@Override
	public String getName() {
		return "void";
	}

	@Override
	public boolean shouldDie(FallingTreeEntity entity) {
		return true;
	}

	@Override
	public void renderTransform(FallingTreeEntity entity, float entityYaw, float partialTicks, MatrixStack matrixStack) {
	}

	@Override
	public void initMotion(FallingTreeEntity entity) {
		FallingTreeEntity.standardDropLogsPayload(entity);
		FallingTreeEntity.standardDropLeavesPayLoad(entity);
		entity.cleanupRootyDirt();
	}

	@Override
	public void handleMotion(FallingTreeEntity entity) {
	}

	@Override
	public void dropPayload(FallingTreeEntity entity) {
	} //Payload is dropped in initMotion

	@Override
	@OnlyIn(Dist.CLIENT)
	public boolean shouldRender(FallingTreeEntity entity) {
		return false;
	}

}
