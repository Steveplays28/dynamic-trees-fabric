package io.github.steveplays28.dynamictreesfabric.event.handlers;

import io.github.steveplays28.dynamictreesfabric.client.thickrings.ThickRingTextureManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.resource.ReloadableResourceManagerImpl;
import net.minecraft.util.Identifier;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Mod.EventBusSubscriber(modid = io.github.steveplays28.dynamictreesfabric.DynamicTreesFabric.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class TextureStitchEventHandler {

    @SubscribeEvent
    public static void onTextureStitchEventPre(TextureStitchEvent.Pre event) {
        Identifier eventAtlasLocation = event.getAtlas().location();
        if (eventAtlasLocation.equals(SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE)) {
            ReloadableResourceManagerImpl manager = (ReloadableResourceManagerImpl) MinecraftClient.getInstance().getResourceManager();

            List<Identifier> ringLocationsToGenerate = new LinkedList<>();
            for (Map.Entry<Identifier, Identifier> reslocs : ThickRingTextureManager.getThickRingEntrySet()) {
                Identifier thickLogResLoc = reslocs.getValue();

                boolean textureNotFound =
                        manager.getResource(new Identifier(thickLogResLoc.getNamespace(),String.format(Locale.ROOT, "textures/%s%s", thickLogResLoc.getPath(), ".png"))).isEmpty();

                if (textureNotFound) {
                    ringLocationsToGenerate.add(thickLogResLoc);
                }
            }

//            ThickRingTextureManager.textureAtlas = new ThickRingAtlasTexture();
//            ThickRingTextureManager.thickRingData = ThickRingTextureManager.textureAtlas.stitch(manager, ringLocationsToGenerate.stream(), EmptyProfiler.INSTANCE, 0);

        }
    }

    @SubscribeEvent
    public static void onTextureStitchEventPost(final TextureStitchEvent.Post event) {
//        if (event.getMap().getTextureLocation().equals(ThickRingAtlasTexture.LOCATION_THICKRINGS_TEXTURE)) {
//            TextureManager textureManager = Minecraft.getInstance().textureManager;
//            ThickRingAtlasTexture atlastexture = ThickRingTextureManager.textureAtlas;
//            AtlasTexture.SheetData atlastexture$sheetdata = ThickRingTextureManager.thickRingData;
//
//            textureManager.mapTextureObjects.remove(ThickRingAtlasTexture.LOCATION_THICKRINGS_TEXTURE);
//
//            atlastexture.upload(atlastexture$sheetdata);
//            textureManager.loadTexture(atlastexture.getTextureLocation(), atlastexture);
//            textureManager.bindTexture(atlastexture.getTextureLocation());
//            atlastexture.setBlurMipmap(atlastexture$sheetdata);
//        }

    }

}
