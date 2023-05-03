package io.github.steveplays28.dynamictreesfabric.client.thickrings;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.Nullable;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.platform.PngInfo;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.datafixers.util.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.minecraft.client.resource.metadata.AnimationResourceMetadata;
import net.minecraft.client.texture.MissingSprite;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.client.texture.TextureStitcher;
import net.minecraft.client.texture.TextureStitcherCannotFitException;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import net.minecraft.util.crash.CrashException;
import net.minecraft.util.crash.CrashReport;
import net.minecraft.util.crash.CrashReportSection;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.profiler.Profiler;

public class ThickRingAtlasTexture extends SpriteAtlasTexture {

	private static final Logger LOGGER = LogManager.getLogger();
	private static final int spriteSizeMultiplier = 3;
	private static boolean uploaded = false;
	private final int maximumTextureSize;

	// @Override
	// public TextureAtlasSprite getSprite(ResourceLocation resloc) {
	//     TextureAtlasSprite sprite = super.getSprite(resloc);
	//     if (sprite instanceof ThickRingTextureAtlasSprite){
	//         ((ThickRingTextureAtlasSprite) sprite).loadAtlasTexture();
	//     }
	//     return sprite;
	// }


	public ThickRingAtlasTexture() {
		super(ThickRingTextureManager.LOCATION_THICKRINGS_TEXTURE);
		maximumTextureSize = RenderSystem.maxSupportedTextureSize();
	}

	@Override
	public void reload(Preparations sheetData) {
		if (!uploaded) {
			super.reload(sheetData);
			uploaded = true;
		}
	}

	public SpriteAtlasTexture.Preparations prepareToStitch(ResourceManager resourceManagerIn, Stream<Identifier> resourceLocationsIn, Profiler profilerIn, int maxMipmapLevelIn) {
		profilerIn.push("preparing");
		Set<Identifier> set = resourceLocationsIn.peek((resloc) -> {
			if (resloc == null) {
				throw new IllegalArgumentException("Location cannot be null!");
			}
		}).collect(Collectors.toSet());
		int i = this.maximumTextureSize;
		TextureStitcher stitcher = new TextureStitcher(i, i, maxMipmapLevelIn);
		int j = Integer.MAX_VALUE;
		int k = 1 << maxMipmapLevelIn;
		profilerIn.swap("extracting_frames");
		net.minecraftforge.client.ForgeHooksClient.onTextureStitchedPre(this, set);

		for (Sprite.Info spriteInfo : this.makeSprites(resourceManagerIn, set)) {
			int spriteWidth = spriteInfo.width() * spriteSizeMultiplier;
			int spriteHeight = spriteInfo.height() * spriteSizeMultiplier;
			j = Math.min(j, Math.min(spriteWidth, spriteHeight));
			int l = Math.min(Integer.lowestOneBit(spriteWidth), Integer.lowestOneBit(spriteHeight));
			if (l < k) {
				LOGGER.warn("Texture {} with size {}x{} limits mip level from {} to {}", spriteInfo.name(), spriteWidth, spriteHeight, MathHelper.floorLog2(k), MathHelper.floorLog2(l));
				k = l;
			}

			stitcher.add(spriteInfo);
		}

		int i1 = Math.min(j, k);
		int j1 = MathHelper.floorLog2(i1);
		int k1 = maxMipmapLevelIn;
		if (false) // FORGE: do not lower the mipmap level
		{
			if (j1 < maxMipmapLevelIn) {
				LOGGER.warn("{}: dropping miplevel from {} to {}, because of minimum power of two: {}", ThickRingTextureManager.LOCATION_THICKRINGS_TEXTURE, maxMipmapLevelIn, j1, i1);
				k1 = j1;
			} else {
				k1 = maxMipmapLevelIn;
			}
		}

		profilerIn.swap("register");
		stitcher.add(MissingSprite.info());
		profilerIn.swap("stitching");

		try {
			stitcher.stitch();
		} catch (TextureStitcherCannotFitException stitcherexception) {
			CrashReport crashreport = CrashReport.create(stitcherexception, "Stitching");
			CrashReportSection crashreportcategory = crashreport.addElement("Stitcher");
			crashreportcategory.add("Sprites", stitcherexception.getSprites().stream().map((p_229216_0_) -> {
				return String.format("%s[%dx%d]", p_229216_0_.name(), p_229216_0_.width(), p_229216_0_.height());
			}).collect(Collectors.joining(",")));
			crashreportcategory.add("Max Texture Size", i);
			throw new CrashException(crashreport);
		}

		profilerIn.swap("loading");
		List<Sprite> list = this.getStitchedSprites(resourceManagerIn, stitcher, k1);
		profilerIn.pop();
		return new SpriteAtlasTexture.Preparations(set, stitcher.getWidth(), stitcher.getHeight(), k1, list);
	}

	private Collection<Sprite.Info> makeSprites(ResourceManager resourceManagerIn, Set<Identifier> spriteLocationsIn) {
		List<CompletableFuture<?>> list = Lists.newArrayList();
		ConcurrentLinkedQueue<Sprite.Info> concurrentlinkedqueue = new ConcurrentLinkedQueue<>();

		for (Identifier thickSpriteLocation : spriteLocationsIn) {
			if (!MissingSprite.getMissingSpriteId().equals(thickSpriteLocation)) {
				list.add(CompletableFuture.runAsync(() -> {
					Identifier baseSpriteLocation = ThickRingTextureManager.getBaseRingFromThickRing(thickSpriteLocation);
					Identifier baseSpritePath = this.getSpritePath(baseSpriteLocation);

					resourceManagerIn.getResource(baseSpritePath).ifPresentOrElse(baseRingResource -> {
						try {
							PngInfo pngsizeinfo = new PngInfo(baseRingResource::toString, baseRingResource.getInputStream());
							AnimationResourceMetadata animationmetadatasection = baseRingResource.getMetadata().decode(AnimationResourceMetadata.READER)
									.orElse(AnimationResourceMetadata.EMPTY);

							Pair<Integer, Integer> pair = animationmetadatasection.getFrameSize(pngsizeinfo.width, pngsizeinfo.height);
							concurrentlinkedqueue.add(new Sprite.Info(baseSpriteLocation, pair.getFirst(), pair.getSecond(), animationmetadatasection));
						} catch (RuntimeException runtimeexception) {
							LOGGER.error("Unable to parse metadata from {} : {}", baseSpritePath, runtimeexception);
						} catch (IOException ioexception) {
							LOGGER.error("Using missing texture, unable to load {}", baseSpritePath, ioexception);
						}
					}, () -> LOGGER.error("Using missing texture, unable to load {}", baseSpritePath));
				}, Util.getMainWorkerExecutor()));
			}
		}

		CompletableFuture.allOf(list.toArray(new CompletableFuture[0])).join();
		return concurrentlinkedqueue;
	}

	private List<Sprite> getStitchedSprites(ResourceManager resourceManagerIn, TextureStitcher stitcherIn, int mipmapLevelIn) {
		ConcurrentLinkedQueue<Sprite> concurrentlinkedqueue = new ConcurrentLinkedQueue<>();
		List<CompletableFuture<?>> list = Lists.newArrayList();
		stitcherIn.getStitchedSprites((spriteInfo, width, height, x, y) -> {
			if (spriteInfo == MissingSprite.info()) {
				MissingSprite missingtexturesprite = MissingSprite.newInstance(this, mipmapLevelIn, width, height, x, y);
				concurrentlinkedqueue.add(missingtexturesprite);
			} else {
				list.add(CompletableFuture.runAsync(() -> {
					Sprite textureatlassprite = this.loadSprite(resourceManagerIn, spriteInfo, width, height, mipmapLevelIn, x, y);
					if (textureatlassprite != null) {
						concurrentlinkedqueue.add(textureatlassprite);
					}

				}, Util.getMainWorkerExecutor()));
			}

		});
		CompletableFuture.allOf(list.toArray(new CompletableFuture[0])).join();
		return Lists.newArrayList(concurrentlinkedqueue);
	}

	@Nullable
	private Sprite loadSprite(ResourceManager resourceManagerIn, Sprite.Info spriteInfoIn, int widthIn, int heightIn, int mipmapLevelIn, int originX, int originY) {
		Identifier baseSpritePath = this.getSpritePath(spriteInfoIn.name());

		Sprite.Info thickSpriteInfo = new Sprite.Info(
				ThickRingTextureManager.getThickRingFromBaseRing(spriteInfoIn.name()),
				spriteInfoIn.width() * spriteSizeMultiplier,
				spriteInfoIn.height() * spriteSizeMultiplier,
				AnimationResourceMetadata.EMPTY);

		Optional<Resource> resourceOpt = resourceManagerIn.getResource(baseSpritePath);
		if (resourceOpt.isPresent()) {
			try (InputStream inputStream = resourceOpt.get().getInputStream()) {
				NativeImage nativeimage = NativeImage.read(inputStream);
				Sprite thinRings = new Sprite(this, spriteInfoIn, mipmapLevelIn, widthIn, heightIn, originX, originY, nativeimage) {
				};
				return new ThickRingTextureAtlasSprite(this, thickSpriteInfo, mipmapLevelIn, widthIn, heightIn, originX, originY, thinRings, baseSpritePath);
			} catch (RuntimeException runtimeexception) {
				LOGGER.error("Unable to parse metadata from {}", baseSpritePath, runtimeexception);
			} catch (IOException ioexception) {
				LOGGER.error("Using missing texture, unable to load {}", baseSpritePath, ioexception);
			}
		}

		return null;
	}

	private Identifier getSpritePath(Identifier location) {
		return new Identifier(location.getNamespace(), String.format("textures/%s%s", location.getPath(), ".png"));
	}

}
