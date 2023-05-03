package io.github.steveplays28.dynamictreesfabric.models.geometry;

import io.github.steveplays28.dynamictreesfabric.models.bakedmodels.RootBlockBakedModel;
import io.github.steveplays28.dynamictreesfabric.models.loaders.RootBlockModelLoader;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.ModelBakeSettings;
import net.minecraft.client.render.model.ModelLoader;
import net.minecraft.client.render.model.json.ModelOverrideList;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.util.SpriteIdentifier;
import net.minecraft.util.Identifier;
import net.minecraftforge.client.model.geometry.IGeometryBakingContext;

import java.util.function.Function;

/**
 * Bakes {@link RootBlockBakedModel} from bark texture location given by {@link RootBlockModelLoader}.
 *
 * @author Harley O'Connor
 */
public class RootBlockModelGeometry extends BranchBlockModelGeometry {

    public RootBlockModelGeometry(final Identifier barkResLoc) {
        super(barkResLoc, null, null, false);
    }

    @Override
    public BakedModel bake(IGeometryBakingContext owner, ModelLoader bakery, Function<SpriteIdentifier, Sprite> spriteGetter, ModelBakeSettings modelTransform, ModelOverrideList overrides, Identifier modelLocation) {
        return new RootBlockBakedModel(modelLocation, this.barkResLoc);
    }

}
