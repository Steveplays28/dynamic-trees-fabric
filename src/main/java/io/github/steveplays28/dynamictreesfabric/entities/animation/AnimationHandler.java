package io.github.steveplays28.dynamictreesfabric.entities.animation;

import io.github.steveplays28.dynamictreesfabric.entities.FallingTreeEntity;

import net.minecraft.client.util.math.MatrixStack;
import net.fabricmc.api.Environment;
import net.fabricmc.api.EnvType;

public interface AnimationHandler {
	String getName();

	void initMotion(FallingTreeEntity entity);

	void handleMotion(FallingTreeEntity entity);

	void dropPayload(FallingTreeEntity entity);

	boolean shouldDie(FallingTreeEntity entity);



@Environment(EnvType.CLIENT)
	void renderTransform(FallingTreeEntity entity, float entityYaw, float partialTicks, MatrixStack matrixStack);

@Environment(EnvType.CLIENT)
	boolean shouldRender(FallingTreeEntity entity);

}
