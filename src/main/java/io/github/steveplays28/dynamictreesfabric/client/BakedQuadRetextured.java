package io.github.steveplays28.dynamictreesfabric.client;

import java.util.Arrays;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.render.model.BakedQuad;
import net.minecraft.client.render.model.BakedQuadFactory;
import net.minecraft.client.texture.Sprite;

public class BakedQuadRetextured extends BakedQuad {
    private final Sprite texture;

    public BakedQuadRetextured(BakedQuad quad, Sprite textureIn) {
        super(Arrays.copyOf(quad.getVertexData(), quad.getVertexData().length), quad.getColorIndex(), BakedQuadFactory.decodeDirection(quad.getVertexData()), quad.getSprite(), quad.hasShade());
        this.texture = textureIn;
        this.remapQuad();
    }

    private void remapQuad() {
        for (int i = 0; i < 4; ++i) {
            int j = VertexFormats.POSITION_COLOR_TEXTURE_LIGHT_NORMAL.getVertexSizeInteger() * i;
            int uvIndex = 4;
            this.vertexData[j + uvIndex] = Float.floatToRawIntBits(this.texture.getFrameU(getUnInterpolatedU(this.sprite, Float.intBitsToFloat(this.vertexData[j + uvIndex]))));
            this.vertexData[j + uvIndex + 1] = Float.floatToRawIntBits(this.texture.getFrameV(getUnInterpolatedV(this.sprite, Float.intBitsToFloat(this.vertexData[j + uvIndex + 1]))));
        }
    }

    @Override
    public Sprite getSprite() {
        return super.getSprite();
    }

    private static float getUnInterpolatedU(Sprite sprite, float u) {
        float f = sprite.getMaxU() - sprite.getMinU();
        return (u - sprite.getMinU()) / f * 16.0F;
    }

    private static float getUnInterpolatedV(Sprite sprite, float v) {
        float f = sprite.getMaxV() - sprite.getMinV();
        return (v - sprite.getMinV()) / f * 16.0F;
    }

}
