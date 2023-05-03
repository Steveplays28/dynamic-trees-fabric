package io.github.steveplays28.dynamictreesfabric.init;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import io.github.steveplays28.dynamictreesfabric.api.TreeHelper;
import io.github.steveplays28.dynamictreesfabric.api.client.ModelHelper;
import io.github.steveplays28.dynamictreesfabric.api.treedata.TreePart;
import io.github.steveplays28.dynamictreesfabric.blocks.PottedSaplingBlock;
import io.github.steveplays28.dynamictreesfabric.blocks.leaves.DynamicLeavesBlock;
import io.github.steveplays28.dynamictreesfabric.blocks.leaves.LeavesProperties;
import io.github.steveplays28.dynamictreesfabric.blocks.rootyblocks.RootyBlock;
import io.github.steveplays28.dynamictreesfabric.blocks.rootyblocks.SoilHelper;
import io.github.steveplays28.dynamictreesfabric.client.BlockColorMultipliers;
import io.github.steveplays28.dynamictreesfabric.client.TextureUtils;
import io.github.steveplays28.dynamictreesfabric.entities.render.FallingTreeRenderer;
import io.github.steveplays28.dynamictreesfabric.entities.render.LingeringEffectorRenderer;
import io.github.steveplays28.dynamictreesfabric.trees.Family;
import io.github.steveplays28.dynamictreesfabric.trees.Species;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.model.data.ModelData;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.apache.logging.log4j.LogManager;

import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.color.block.BlockColors;
import net.minecraft.client.color.world.FoliageColors;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.BakedQuad;
import net.minecraft.client.texture.Sprite;
import net.minecraft.entity.Entity;
import net.minecraft.particle.BlockStateParticleEffect;
import net.minecraft.particle.DefaultParticleType;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.screen.PlayerScreenHandler;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;

@Mod.EventBusSubscriber(modid = io.github.steveplays28.dynamictreesfabric.DynamicTreesFabric.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public class DTClient {

	//TODO: thick ring stitching
	public static void clientStart() {
//		FMLJavaModLoadingContext.get().getModEventBus().addListener(EventPriority.NORMAL, false, ColorHandlerEvent.Block.class, setupEvent -> {
//			IResourceManager manager = Minecraft.getInstance().getResourceManager();
//			if (manager instanceof IReloadableResourceManager){
//				ThickRingTextureManager.uploader = new ThickRingSpriteUploader(Minecraft.getInstance().textureManager);
//				((IReloadableResourceManager) manager).addReloadListener(ThickRingTextureManager.uploader);
//			}
//		});
	}

	public static void setup() {

		registerJsonColorMultipliers();


		registerColorHandlers();
//		MinecraftForge.EVENT_BUS.register(BlockBreakAnimationClientHandler.instance);

		LeavesProperties.postInitClient();
		cleanup();
	}

	import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
	public static void discoverWoodColors() {

		final Function<Identifier, Sprite> bakedTextureGetter = MinecraftClient.getInstance()
				.getSpriteAtlas(PlayerScreenHandler.BLOCK_ATLAS_TEXTURE);

		for (Family family : Species.REGISTRY.getAll().stream().map(Species::getFamily).distinct().collect(Collectors.toList())) {
			family.woodRingColor = 0xFFF1AE;
			family.woodBarkColor = 0xB3A979;
			if (family != Family.NULL_FAMILY) {
				family.getPrimitiveLog().ifPresent(branch -> {
					BlockState state = branch.getDefaultState();
					family.woodRingColor = getFaceColor(state, Direction.DOWN, bakedTextureGetter);
					family.woodBarkColor = getFaceColor(state, Direction.NORTH, bakedTextureGetter);
				});
			}
		}
	}

	import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
	private static int getFaceColor(BlockState state, Direction face, Function<Identifier, Sprite> textureGetter) {
		final BakedModel model = MinecraftClient.getInstance().getBlockRenderManager().getModel(state);
		List<BakedQuad> quads = model.getQuads(state, face, Random.create(), ModelData.EMPTY, null);
		if (quads.isEmpty()) // If the quad list is empty, means there is no face on that side, so we try with null.
		{
			quads = model.getQuads(state, null, Random.create(), ModelData.EMPTY, null);
		}
		if (quads.isEmpty()) { // If null still returns empty, there is nothing we can do so we just warn and exit.
			LogManager.getLogger().warn("Could not get color of " + face + " side for " + state.getBlock() + "! Branch needs to be handled manually!");
			return 0;
		}
		final Identifier resLoc = quads.get(0).getSprite().getName(); // Now we get the texture location of that selected face.
		if (!resLoc.toString().isEmpty()) {
			final TextureUtils.PixelBuffer pixelBuffer = new TextureUtils.PixelBuffer(textureGetter.apply(resLoc));
			final int u = pixelBuffer.w / 16;
			final TextureUtils.PixelBuffer center = new TextureUtils.PixelBuffer(u * 8, u * 8);
			pixelBuffer.blit(center, u * -8, u * -8);

			return center.averageColor();
		}
		return 0;
	}

	private static void cleanup() {
		BlockColorMultipliers.cleanUp();
	}

	private static boolean isValid(BlockView access, BlockPos pos) {
		return access != null && pos != null;
	}

	private static void registerColorHandlers() {
		final int white = 0xFFFFFFFF;
		final int magenta = 0x00FF00FF;//for errors.. because magenta sucks.

		// BLOCKS

		final BlockColors blockColors = MinecraftClient.getInstance().getBlockColors();

		// Register Rooty Colorizers
		for (RootyBlock roots : SoilHelper.getRootyBlocksList()) {
			blockColors.registerColorProvider((state, world, pos, tintIndex) -> roots.colorMultiplier(blockColors, state, world, pos, tintIndex), roots);
		}

		// Register Bonsai Pot Colorizer
		ModelHelper.regColorHandler(DTRegistries.POTTED_SAPLING.get(), (state, access, pos, tintIndex) -> isValid(access, pos) && (state.getBlock() instanceof PottedSaplingBlock)
				? DTRegistries.POTTED_SAPLING.get().getSpecies(access, pos).saplingColorMultiplier(state, access, pos, tintIndex) : white);

		// ITEMS

		// Register Potion Colorizer
		ModelHelper.regColorHandler(DTRegistries.DENDRO_POTION.get(), DTRegistries.DENDRO_POTION.get()::getColor);

		// Register Woodland Staff Colorizer
		ModelHelper.regColorHandler(DTRegistries.STAFF.get(), DTRegistries.STAFF.get()::getColor);

		// TREE PARTS

		// Register Sapling Colorizer
		for (Species species : Species.REGISTRY) {
			if (species.getSapling().isPresent()) {
				ModelHelper.regColorHandler(species.getSapling().get(), (state, access, pos, tintIndex) ->
						isValid(access, pos) ? species.saplingColorMultiplier(state, access, pos, tintIndex) : white);
			}
		}

		// Register Leaves Colorizers
		for (DynamicLeavesBlock leaves : LeavesProperties.REGISTRY.getAll().stream().filter(lp -> lp.getDynamicLeavesBlock().isPresent()).map(lp -> lp.getDynamicLeavesBlock().get()).collect(Collectors.toSet())) {
			ModelHelper.regColorHandler(leaves, (state, worldIn, pos, tintIndex) -> {
						final LeavesProperties properties = ((DynamicLeavesBlock) state.getBlock()).getProperties(state);
						return TreeHelper.isLeaves(state.getBlock()) ? properties.foliageColorMultiplier(state, worldIn, pos) : magenta;
					}
			);
		}

	}

	private static void registerJsonColorMultipliers() {
		// Register programmable custom block color providers for LeavesPropertiesJson
		BlockColorMultipliers.register("birch", (state, worldIn, pos, tintIndex) -> FoliageColors.getBirchColor());
		BlockColorMultipliers.register("spruce", (state, worldIn, pos, tintIndex) -> FoliageColors.getSpruceColor());
	}

	public static void registerClientEventHandlers() {
		//        MinecraftForge.EVENT_BUS.register(new ModelBakeEventListener());
		//        MinecraftForge.EVENT_BUS.register(TextureGenerationHandler.class);
	}

	@SubscribeEvent
	public static void registerEntityRenderers(EntityRenderersEvent.RegisterRenderers event) {
		event.registerEntityRenderer(DTRegistries.FALLING_TREE.get(), FallingTreeRenderer::new);
		event.registerEntityRenderer(DTRegistries.LINGERING_EFFECTOR.get(), LingeringEffectorRenderer::new);
	}

	private static int getFoliageColor(LeavesProperties leavesProperties, World world, BlockState blockState, BlockPos pos) {
		return leavesProperties.foliageColorMultiplier(blockState, world, pos);
	}

	///////////////////////////////////////////
	// PARTICLES
	///////////////////////////////////////////

	private static void addDustParticle(World world, double fx, double fy, double fz, double mx, double my, double mz, BlockState blockState, float r, float g, float b) {
		if (world.isClient) {
			Particle particle = MinecraftClient.getInstance().particleManager.addParticle(new BlockStateParticleEffect(ParticleTypes.BLOCK, blockState), fx, fy, fz, mx, my, mz);
			assert particle != null;
			particle.setColor(r, g, b);
		}
	}

	public static void spawnParticles(World world, DefaultParticleType particleType, BlockPos pos, int numParticles, Random random) {
		spawnParticles(world, particleType, pos.getX(), pos.getY(), pos.getZ(), numParticles, random);
	}

	public static void spawnParticles(WorldAccess world, DefaultParticleType particleType, int x, int y, int z, int numParticles, Random random) {
		for (int i1 = 0; i1 < numParticles; ++i1) {
			double mx = random.nextGaussian() * 0.02D;
			double my = random.nextGaussian() * 0.02D;
			double mz = random.nextGaussian() * 0.02D;
			DTClient.spawnParticle(world, particleType, x + random.nextFloat(), (double) y + (double) random.nextFloat(), (double) z + random.nextFloat(), mx, my, mz);
		}
	}

	/**
	 * Not strictly necessary. But adds a little more isolation to the server for particle effects
	 */
	public static void spawnParticle(WorldAccess world, DefaultParticleType particleType, double x, double y, double z, double mx, double my, double mz) {
		if (world.isClient()) {
			world.addParticle(particleType, x, y, z, mx, my, mz);
		}
	}

	public static void crushLeavesBlock(World world, BlockPos pos, BlockState blockState, Entity entity) {
		if (world.isClient) {
			Random random = world.random;
			TreePart treePart = TreeHelper.getTreePart(blockState);
			if (treePart instanceof DynamicLeavesBlock) {
				DynamicLeavesBlock leaves = (DynamicLeavesBlock) treePart;
				LeavesProperties leavesProperties = leaves.getProperties(blockState);
				int color = getFoliageColor(leavesProperties, world, blockState, pos);
				float r = (color >> 16 & 255) / 255.0F;
				float g = (color >> 8 & 255) / 255.0F;
				float b = (color & 255) / 255.0F;
				for (int dz = 0; dz < 8; dz++) {
					for (int dy = 0; dy < 8; dy++) {
						for (int dx = 0; dx < 8; dx++) {
							if (random.nextInt(8) == 0) {
								double fx = pos.getX() + dx / 8.0;
								double fy = pos.getY() + dy / 8.0;
								double fz = pos.getZ() + dz / 8.0;
								addDustParticle(world, fx, fy, fz, 0, random.nextFloat() * entity.getVelocity().y, 0, blockState, r, g, b);
							}
						}
					}
				}
			}
		}
	}

}
