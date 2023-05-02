package io.github.steveplays28.dynamictreesfabric.entities.animation;

import io.github.steveplays28.dynamictreesfabric.entities.FallingTreeEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public interface AnimationHandler {
    String getName();

    void initMotion(FallingTreeEntity entity);

    void handleMotion(FallingTreeEntity entity);

    void dropPayload(FallingTreeEntity entity);

    boolean shouldDie(FallingTreeEntity entity);

    @OnlyIn(Dist.CLIENT)
    void renderTransform(FallingTreeEntity entity, float entityYaw, float partialTicks, PoseStack matrixStack);

    @OnlyIn(Dist.CLIENT)
    boolean shouldRender(FallingTreeEntity entity);

}
