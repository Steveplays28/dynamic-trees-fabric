package io.github.steveplays28.dynamictreesfabric.event.handlers;

import io.github.steveplays28.dynamictreesfabric.blocks.PottedSaplingBlock;
import io.github.steveplays28.dynamictreesfabric.models.bakedmodels.BakedModelBlockBonsaiPot;
import io.github.steveplays28.dynamictreesfabric.models.bakedmodels.BranchBlockBakedModel;
import io.github.steveplays28.dynamictreesfabric.models.loaders.BranchBlockModelLoader;
import io.github.steveplays28.dynamictreesfabric.models.loaders.RootBlockModelLoader;
import io.github.steveplays28.dynamictreesfabric.models.loaders.ThickBranchBlockModelLoader;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.util.ModelIdentifier;
import net.minecraft.util.Identifier;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ModelEvent.BakingCompleted;
import net.minecraftforge.client.event.ModelEvent.RegisterGeometryLoaders;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * @author Harley O'Connor
 */
@Mod.EventBusSubscriber(modid = io.github.steveplays28.dynamictreesfabric.DynamicTreesFabric.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class BakedModelEventHandler {

    public static final Identifier BRANCH = io.github.steveplays28.dynamictreesfabric.DynamicTreesFabric.resLoc("branch");
    public static final Identifier ROOT = io.github.steveplays28.dynamictreesfabric.DynamicTreesFabric.resLoc("root");
    public static final Identifier THICK_BRANCH = io.github.steveplays28.dynamictreesfabric.DynamicTreesFabric.resLoc("thick_branch");

    @SubscribeEvent
    public static void onModelRegistryEvent(RegisterGeometryLoaders event) {
        // Register model loaders for baked models.
        event.register("branch", new BranchBlockModelLoader());
        event.register("root", new RootBlockModelLoader());
        event.register("thick_branch", new ThickBranchBlockModelLoader());
    }

    @SubscribeEvent
    public static void onModelBake(BakingCompleted event) {
        // Setup branch baked models (bakes cores and sleeves).
        BranchBlockBakedModel.INSTANCES.forEach(BranchBlockBakedModel::setupModels);
        BranchBlockBakedModel.INSTANCES.clear();

        // Put bonsai pot baked model into its model location.
        BakedModel flowerPotModel = event.getModelManager().getModel(new ModelIdentifier(PottedSaplingBlock.REG_NAME, ""));
        event.getModels().put(new ModelIdentifier(PottedSaplingBlock.REG_NAME, ""),
                new BakedModelBlockBonsaiPot(flowerPotModel));

        ////Highly experimental code
//        SpriteMap spriteAtlases = event.getModelManager().atlases;
//        assert spriteAtlases != null;
//        Map<ResourceLocation, AtlasTexture> atlasTextures = spriteAtlases.atlasTextures;
//        atlasTextures.put(ThickRingTextureManager.LOCATION_THICKRINGS_TEXTURE, ThickRingTextureManager.textureAtlas);
//
//        Map<ResourceLocation, IUnbakedModel> topUnbakedModels = event.getModelLoader().topUnbakedModels;
//        List<ResourceLocation> modelsToRebake = new LinkedList<>();
//        for (Map.Entry<ResourceLocation, IUnbakedModel> entry : topUnbakedModels.entrySet()){
//            for (RenderMaterial material : entry.getValue().getTextures(event.getModelLoader()::getUnbakedModel, Sets.newLinkedHashSet())){
//                if (material.getAtlasLocation().equals(ThickRingTextureManager.LOCATION_THICKRINGS_TEXTURE)){
//                    modelsToRebake.add(entry.getKey());
//                }
//            }
//        }
//        for (ResourceLocation resourceLocation : modelsToRebake){
//            event.getModelRegistry().put(resourceLocation, event.getModelLoader().bake(resourceLocation, ModelRotation.X0_Y0));
//        }
    }

}
