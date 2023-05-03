package io.github.steveplays28.dynamictreesfabric.blocks.leaves;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.jetbrains.annotations.Nullable;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.steveplays28.dynamictreesfabric.api.cells.CellKit;
import io.github.steveplays28.dynamictreesfabric.api.data.Generator;
import io.github.steveplays28.dynamictreesfabric.api.data.LeavesStateGenerator;
import io.github.steveplays28.dynamictreesfabric.api.registry.RegistryEntry;
import io.github.steveplays28.dynamictreesfabric.api.registry.RegistryHandler;
import io.github.steveplays28.dynamictreesfabric.api.registry.TypedRegistry;
import io.github.steveplays28.dynamictreesfabric.blocks.branches.BranchBlock;
import io.github.steveplays28.dynamictreesfabric.cells.CellKits;
import io.github.steveplays28.dynamictreesfabric.client.BlockColorMultipliers;
import io.github.steveplays28.dynamictreesfabric.data.DTBlockTags;
import io.github.steveplays28.dynamictreesfabric.data.provider.DTBlockStateProvider;
import io.github.steveplays28.dynamictreesfabric.init.DTTrees;
import io.github.steveplays28.dynamictreesfabric.resources.Resources;
import io.github.steveplays28.dynamictreesfabric.trees.Family;
import io.github.steveplays28.dynamictreesfabric.trees.Resettable;
import io.github.steveplays28.dynamictreesfabric.util.BlockStates;
import io.github.steveplays28.dynamictreesfabric.util.ColorUtil;
import io.github.steveplays28.dynamictreesfabric.util.MutableLazyValue;
import io.github.steveplays28.dynamictreesfabric.util.Optionals;
import io.github.steveplays28.dynamictreesfabric.util.ResourceLocationUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager;

import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.MapColor;
import net.minecraft.block.Material;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.color.block.BlockColorProvider;
import net.minecraft.entity.EntityType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.BlockRenderView;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;

/**
 * This class provides a means of holding individual properties for leaves.  This is necessary since leaves can contain
 * sub blocks that may behave differently.  Each leaves properties object must have a reference to a tree family.
 *
 * @author ferreusveritas
 */
public class LeavesProperties extends RegistryEntry<LeavesProperties> implements Resettable<LeavesProperties> {

	public static final Codec<LeavesProperties> CODEC = RecordCodecBuilder.create(instance -> instance
			.group(Identifier.CODEC.fieldOf(Resources.RESOURCE_LOCATION.toString()).forGetter(LeavesProperties::getRegistryName))
			.apply(instance, LeavesProperties::new));

	public static final LeavesProperties NULL_PROPERTIES = new LeavesProperties() {
		@Override
		public LeavesProperties setFamily(Family family) {
			return this;
		}

		@Override
		public Family getFamily() {
			return Family.NULL_FAMILY;
		}

		@Override
		public BlockState getPrimitiveLeaves() {
			return Blocks.AIR.getDefaultState();
		}

		@Override
		public ItemStack getPrimitiveLeavesItemStack() {
			return ItemStack.EMPTY;
		}

		@Override
		public LeavesProperties setDynamicLeavesState(BlockState state) {
			return this;
		}

		@Override
		public BlockState getDynamicLeavesState() {
			return Blocks.AIR.getDefaultState();
		}

		@Override
		public BlockState getDynamicLeavesState(int hydro) {
			return Blocks.AIR.getDefaultState();
		}

		@Override
		public CellKit getCellKit() {
			return CellKit.NULL_CELL_KIT;
		}

		@Override
		public int getFlammability() {
			return 0;
		}

		@Override
		public int getFireSpreadSpeed() {
			return 0;
		}

		@Override
		public int getSmotherLeavesMax() {
			return 0;
		}

		@Override
		public int getLightRequirement() {
			return 15;
		}

		@Override
		public boolean updateTick(World worldIn, BlockPos pos, BlockState state, Random rand) {
			return false;
		}
	}.setRegistryName(DTTrees.NULL).setBlockRegistryName(DTTrees.NULL);

	/**
	 * Central registry for all {@link LeavesProperties} objects.
	 */
	public static final TypedRegistry<LeavesProperties> REGISTRY = new TypedRegistry<>(LeavesProperties.class, NULL_PROPERTIES, new TypedRegistry.EntryType<>(CODEC));

	protected static final int maxHydro = 7;
	protected final MutableLazyValue<Generator<DTBlockStateProvider, LeavesProperties>> stateGenerator =
			MutableLazyValue.supplied(LeavesStateGenerator::new);
	/**
	 * The primitive (vanilla) leaves are used for many purposes including rendering, drops, and some other basic
	 * behavior.
	 */
	protected BlockState primitiveLeaves;
	/**
	 * The {@link CellKit}, which is for leaves automata.
	 */
	protected CellKit cellKit;
	protected Family family;
	protected BlockState[] dynamicLeavesBlockHydroStates = new BlockState[maxHydro + 1];
	protected int flammability = 60;// Mimic vanilla leaves
	protected int fireSpreadSpeed = 30;// Mimic vanilla leaves
	protected int smotherLeavesMax = 4;
	protected int lightRequirement = 13;
	protected AgeingConfiguration ageingConfiguration = AgeingConfiguration.ALWAYS;
	protected boolean connectAnyRadius = false;
	protected boolean requiresShears = true;
	protected Integer colorNumber;
	protected String colorString;
	/**
	 * The registry name for the leaves block. This allows for built-in compatibility where the dynamic leaves may
	 * otherwise share the same name as their regular leaves block.
	 */
	private Identifier blockRegistryName;

	///////////////////////////////////////////
	// PRIMITIVE LEAVES BLOCK
	///////////////////////////////////////////
	private boolean canGrowOnGround = false;
	import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
	private BlockColorProvider colorMultiplier;

	private LeavesProperties() {
	}

	public LeavesProperties(final Identifier registryName) {
		this(null, registryName);
	}

	///////////////////////////////////////////
	// DYNAMIC LEAVES BLOCK
	///////////////////////////////////////////

	public LeavesProperties(@Nullable final BlockState primitiveLeaves, final Identifier registryName) {
		this(primitiveLeaves, CellKits.DECIDUOUS, registryName);
	}

	public LeavesProperties(@Nullable final BlockState primitiveLeaves, final CellKit cellKit, final Identifier registryName) {
		this.family = Family.NULL_FAMILY;
		this.primitiveLeaves = primitiveLeaves != null ? primitiveLeaves : BlockStates.AIR;
		this.cellKit = cellKit;
		this.setRegistryName(registryName);
		this.blockRegistryName = ResourceLocationUtils.suffix(registryName, this.getBlockRegistryNameSuffix());
	}

	import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
	public static void postInitClient() {
		REGISTRY.getAll().forEach(LeavesProperties::processColor);
	}

	/**
	 * Gets the primitive (vanilla) leaves for these {@link LeavesProperties}.
	 *
	 * @return The {@link BlockState} for the primitive leaves.
	 */
	public BlockState getPrimitiveLeaves() {
		return primitiveLeaves;
	}

	public void setPrimitiveLeaves(final Block primitiveLeaves) {
		if (this.primitiveLeaves == null || primitiveLeaves != this.primitiveLeaves.getBlock()) {
			this.primitiveLeaves = primitiveLeaves.getDefaultState();
		}
	}

	public Optional<Block> getPrimitiveLeavesBlock() {
		return Optionals.ofBlock(this.primitiveLeaves.getBlock());
	}

	/**
	 * Gets {@link ItemStack} of the primitive (vanilla) leaves (for things like when it's sheared).
	 *
	 * @return The {@link ItemStack} object.
	 */
	public ItemStack getPrimitiveLeavesItemStack() {
		return new ItemStack(Item.BLOCK_ITEMS.get(getPrimitiveLeaves().getBlock()));
	}

	/**
	 * Gets the {@link #blockRegistryName} for this {@link LeavesProperties} object.
	 *
	 * @return The {@link #blockRegistryName} for this {@link LeavesProperties} object.
	 */
	public Identifier getBlockRegistryName() {
		return this.blockRegistryName;
	}

	/**
	 * Sets the {@link #blockRegistryName} for this {@link LeavesProperties} object to the specified {@code
	 * blockRegistryName}.
	 *
	 * @param blockRegistryName The new {@link Identifier} object to set.
	 * @return This {@link LeavesProperties} object for chaining.
	 */
	public LeavesProperties setBlockRegistryName(Identifier blockRegistryName) {
		this.blockRegistryName = blockRegistryName;
		return this;
	}

	/**
	 * Returns a default suffix for {@link #blockRegistryName}. Note that this will be overridden if the {@link
	 * #blockRegistryName} is changed in the Json.
	 *
	 * @return A default suffix for {@link #blockRegistryName}.
	 */
	protected String getBlockRegistryNameSuffix() {
		return "_leaves";
	}

	///////////////////////////////////////////
	// PROPERTIES
	///////////////////////////////////////////

	public Optional<DynamicLeavesBlock> getDynamicLeavesBlock() {
		Block block = this.getDynamicLeavesState().getBlock();
		return Optional.ofNullable(block instanceof DynamicLeavesBlock ? (DynamicLeavesBlock) block : null);
	}

	protected DynamicLeavesBlock createDynamicLeaves(final AbstractBlock.Settings properties) {
		return new DynamicLeavesBlock(this, properties);
	}

	public void generateDynamicLeaves(final AbstractBlock.Settings properties) {
		RegistryHandler.addBlock(this.blockRegistryName, () -> this.createDynamicLeaves(properties));
	}

	public BlockState getDynamicLeavesState() {
		return getDynamicLeavesState(getCellKit().getDefaultHydration());
	}

	public LeavesProperties setDynamicLeavesState(BlockState state) {
		//Cache all the blockStates to speed up worldgen
		dynamicLeavesBlockHydroStates[0] = Blocks.AIR.getDefaultState();
		for (int i = 1; i <= maxHydro; i++) {
			dynamicLeavesBlockHydroStates[i] = state.with(DynamicLeavesBlock.DISTANCE, i);
		}
		return this;
	}

	public BlockState getDynamicLeavesState(int hydro) {
		return Optional.ofNullable(dynamicLeavesBlockHydroStates[MathHelper.clamp(hydro, 0, maxHydro)])
				.orElse(Blocks.AIR.getDefaultState());
	}

	/**
	 * Used by the {@link DynamicLeavesBlock} to find out if it can pull hydro from a branch.
	 *
	 * @return The {@link Family} for these {@link LeavesProperties}.
	 */
	public Family getFamily() {
		return family;
	}

	/**
	 * Sets the type of tree these leaves connect to.
	 *
	 * @param family The {@link Family} object to set.
	 * @return This {@link LeavesProperties} object.
	 */
	public LeavesProperties setFamily(Family family) {
		this.family = family;
		if (family.isFireProof()) {
			flammability = 0;
			fireSpreadSpeed = 0;
		}
		return this;
	}

	public int getFlammability() {
		return flammability;
	}

	public void setFlammability(int flammability) {
		this.flammability = flammability;
	}

	public int getFireSpreadSpeed() {
		return fireSpreadSpeed;
	}

	public void setFireSpreadSpeed(int fireSpreadSpeed) {
		this.fireSpreadSpeed = fireSpreadSpeed;
	}

	/**
	 * Gets the smother leaves max - the maximum amount of leaves in a stack before the bottom-most leaf block dies. Can
	 * beset to zero to disable smothering. [default = 4]
	 *
	 * @return the smother leaves max.
	 */
	public int getSmotherLeavesMax() {
		return this.smotherLeavesMax;
	}

	public void setSmotherLeavesMax(int smotherLeavesMax) {
		this.smotherLeavesMax = smotherLeavesMax;
	}

	/**
	 * Gets the minimum amount of light necessary for a leaves block to be created. [default = 13]
	 *
	 * @return the minimum light requirement.
	 */
	public int getLightRequirement() {
		return this.lightRequirement;
	}

	public void setLightRequirement(int lightRequirement) {
		this.lightRequirement = lightRequirement;
	}

	/**
	 * If the leaves block should tick and age. May return {@code false} to allow for dead leaves.
	 *
	 * @param worldGen {@code true} if called during world gen
	 * @return {@code true} if the leaves block should tick and age
	 */
	public boolean shouldAge(boolean worldGen, BlockState state) {
		return this.ageingConfiguration.shouldAge(worldGen);
	}

	/**
	 * @deprecated use {@link #setAgeingConfiguration(AgeingConfiguration)}
	 */
	@Deprecated
	public void setDoesAge(String doesAge) {
		try {
			this.ageingConfiguration = AgeingConfiguration.valueOf(doesAge.toUpperCase());
		} catch (IllegalArgumentException e) {
			throw new IllegalArgumentException("Invalid ageing configuration \"" + doesAge + "\".");
		}
	}

	public void setAgeingConfiguration(AgeingConfiguration doesAge) {
		this.ageingConfiguration = doesAge;
	}

	public void setCanGrowOnGround(boolean canGrowOnGround) {
		this.canGrowOnGround = canGrowOnGround;
	}

	public boolean canGrowOnGround() {
		return canGrowOnGround;
	}

	/**
	 * Gets the {@link CellKit}, which is for leaves automata.
	 *
	 * @return The {@link CellKit} object.
	 */
	public CellKit getCellKit() {
		return cellKit;
	}

	public void setCellKit(CellKit cellKit) {
		this.cellKit = cellKit;
	}

	///////////////////////////////////////////
	// INTERACTION
	///////////////////////////////////////////

	public boolean isConnectAnyRadius() {
		return connectAnyRadius;
	}

	public void setConnectAnyRadius(boolean connectAnyRadius) {
		this.connectAnyRadius = connectAnyRadius;
	}

	public Material getDefaultMaterial() {
		return Material.LEAVES;
	}

	public AbstractBlock.Settings getDefaultBlockProperties(final Material material, final MapColor materialColor) {
		return AbstractBlock.Settings.of(material, materialColor)
				.strength(0.2F)
				//.harvestTool(ToolTypes.SHEARS)
				.ticksRandomly()
				.sounds(BlockSoundGroup.GRASS)
				.nonOpaque()
				.allowsSpawning((s, r, p, e) -> e == EntityType.OCELOT || e == EntityType.PARROT)
				.suffocates((s, r, p) -> false)
				.blockVision((s, r, p) -> false);
	}

	public boolean updateTick(World worldIn, BlockPos pos, BlockState state, Random rand) {
		return shouldAge(false, state);
	}

	public int getRadiusForConnection(BlockState state, BlockView blockAccess, BlockPos pos, BranchBlock from, Direction side, int fromRadius) {
		final int twigRadius = from.getFamily().getPrimaryThickness();
		return (fromRadius == twigRadius || this.connectAnyRadius) && from.getFamily().isCompatibleDynamicLeaves(from.getFamily().getCommonSpecies(), blockAccess.getBlockState(pos), blockAccess, pos) ? twigRadius : 0;
	}

	public boolean doRequireShears() {
		return requiresShears;
	}

	///////////////////////////////////////////
	// LEAVES COLORS
	///////////////////////////////////////////

	public void setRequiresShears(boolean requiresShears) {
		this.requiresShears = requiresShears;
	}

	public List<TagKey<Block>> defaultLeavesTags() {
		return Collections.singletonList(DTBlockTags.LEAVES);
	}

	@Override
	public void generateStateData(DTBlockStateProvider provider) {
		// Generate leaves block state and model.
		this.stateGenerator.get().generate(provider, this);
	}

	public void setColorNumber(Integer colorNumber) {
		this.colorNumber = colorNumber;
	}

	public void setColorString(String colorString) {
		this.colorString = colorString;
	}

	import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
	public int treeFallColorMultiplier(BlockState state, BlockRenderView world, BlockPos pos) {
		return this.foliageColorMultiplier(state, world, pos);
	}

	import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
	public int foliageColorMultiplier(BlockState state, BlockRenderView world, BlockPos pos) {
		if (colorMultiplier == null) {
			return 0x00FF00FF; //purple if broken
		}
		return colorMultiplier.getColor(state, world, pos, -1);
	}

	import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
	private void processColor() {
		int color = -1;
		if (this.colorNumber != null) {
			color = this.colorNumber;
		} else if (this.colorString != null) {
			String code = this.colorString;
			if (code.startsWith("@")) {
				code = code.substring(1);
				if ("biome".equals(code)) { // Built in code since we need access to super.
					this.colorMultiplier = (state, world, pos, t) -> ((WorldAccess) world).getBiome(pos).value().getFoliageColor();
					return;
				}

				BlockColorProvider blockColor = BlockColorMultipliers.find(code);
				if (blockColor != null) {
					colorMultiplier = blockColor;
					return;
				} else {
					LogManager.getLogger().error("ColorMultiplier resource '{}' could not be found.", code);
				}
			} else {
				color = ColorUtil.decodeARGB32(code);
			}
		}
		int c = color;
		this.colorMultiplier = (s, w, p, t) -> c == -1 ? MinecraftClient.getInstance().getBlockColors().getColor(getPrimitiveLeaves(), w, p, 0) : c;
	}

	@Override
	public String toReloadDataString() {
		return this.getString(Pair.of("primitiveLeaves", this.primitiveLeaves), Pair.of("cellKit", this.cellKit),
				Pair.of("smotherLeavesMax", this.smotherLeavesMax), Pair.of("lightRequirement", this.lightRequirement),
				Pair.of("fireSpreadSpeed", this.fireSpreadSpeed), Pair.of("flammability", this.flammability),
				Pair.of("connectAnyRadius", this.connectAnyRadius));
	}

	///////////////////////////////////////////
	// MISC
	///////////////////////////////////////////

	public enum AgeingConfiguration {
		ALWAYS(true, true),
		WORLDGEN_ONLY(true, false),
		GROWTH_ONLY(false, true),
		NEVER(false, false);

		private final boolean ageDuringWorldGen, ageDuringGrowth;

		AgeingConfiguration(boolean ageDuringWorldGen, boolean ageDuringGrowth) {
			this.ageDuringWorldGen = ageDuringWorldGen;
			this.ageDuringGrowth = ageDuringGrowth;
		}

		public boolean shouldAge(boolean worldGen) {
			if (worldGen) {
				return ageDuringWorldGen;
			} else {
				return ageDuringGrowth;
			}
		}
	}

}
