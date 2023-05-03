package io.github.steveplays28.dynamictreesfabric.entities.render;

import io.github.steveplays28.dynamictreesfabric.entities.LingeringEffectorEntity;
import net.minecraft.client.render.Frustum;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.texture.MissingSprite;
import net.minecraft.util.Identifier;

/**
 * @author Harley O'Connor
 */
public class LingeringEffectorRenderer extends EntityRenderer<LingeringEffectorEntity> {

    public LingeringEffectorRenderer(EntityRendererFactory.Context renderManager) {
        super(renderManager);
    }

    @Override
    public boolean shouldRender(LingeringEffectorEntity livingEntityIn, Frustum camera, double camX, double camY, double camZ) {
        return false;
    }

    @Override
    public Identifier getTextureLocation(LingeringEffectorEntity entity) {
        return MissingSprite.getMissingSpriteId();
    }

//    public static class Factory implements IRenderFactory<LingeringEffectorEntity> {
//
//        @Override
//        public EntityRenderer<LingeringEffectorEntity> createRenderFor(EntityRenderDispatcher manager) {
//            return new LingeringEffectorRenderer(manager);
//        }
//
//    }

}
