package io.github.steveplays28.dynamictreesfabric.trees;

import static io.github.steveplays28.dynamictreesfabric.util.ResourceLocationUtils.prefix;
import static io.github.steveplays28.dynamictreesfabric.util.ResourceLocationUtils.suffix;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Supplier;

import javax.annotation.Nullable;

import io.github.steveplays28.dynamictreesfabric.api.TreeHelper;
import io.github.steveplays28.dynamictreesfabric.api.data.BranchItemModelGenerator;
import io.github.steveplays28.dynamictreesfabric.api.data.BranchStateGenerator;
import io.github.steveplays28.dynamictreesfabric.api.data.Generator;
import io.github.steveplays28.dynamictreesfabric.api.data.StrippedBranchStateGenerator;
import io.github.steveplays28.dynamictreesfabric.api.data.SurfaceRootStateGenerator;
import io.github.steveplays28.dynamictreesfabric.api.registry.RegistryEntry;
import io.github.steveplays28.dynamictreesfabric.api.registry.RegistryHandler;
import io.github.steveplays28.dynamictreesfabric.api.registry.TypedRegistry;
import io.github.steveplays28.dynamictreesfabric.blocks.branches.BasicBranchBlock;
import io.github.steveplays28.dynamictreesfabric.blocks.branches.BranchBlock;
import io.github.steveplays28.dynamictreesfabric.blocks.branches.SurfaceRootBlock;
import io.github.steveplays28.dynamictreesfabric.blocks.branches.ThickBranchBlock;
import io.github.steveplays28.dynamictreesfabric.blocks.leaves.DynamicLeavesBlock;
import io.github.steveplays28.dynamictreesfabric.blocks.leaves.LeavesProperties;
import io.github.steveplays28.dynamictreesfabric.cells.MetadataCell;
import io.github.steveplays28.dynamictreesfabric.compat.waila.WailaOther;
import io.github.steveplays28.dynamictreesfabric.data.DTBlockTags;
import io.github.steveplays28.dynamictreesfabric.data.DTItemTags;
import io.github.steveplays28.dynamictreesfabric.data.provider.BranchLoaderBuilder;
import io.github.steveplays28.dynamictreesfabric.data.provider.DTBlockStateProvider;
import io.github.steveplays28.dynamictreesfabric.data.provider.DTItemModelProvider;
import io.github.steveplays28.dynamictreesfabric.entities.FallingTreeEntity;
import io.github.steveplays28.dynamictreesfabric.entities.animation.AnimationHandler;
import io.github.steveplays28.dynamictreesfabric.init.DTRegistries;
import io.github.steveplays28.dynamictreesfabric.init.DTTrees;
import io.github.steveplays28.dynamictreesfabric.util.BlockBounds;
import io.github.steveplays28.dynamictreesfabric.util.MutableLazyValue;
import io.github.steveplays28.dynamictreesfabric.util.Optionals;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.model.generators.BlockModelBuilder;
import net.minecraftforge.common.data.ExistingFileHelper;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager;

import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.HorizontalFacingBlock;
import net.minecraft.block.MapColor;
import net.minecraft.block.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.item.Items;
import net.minecraft.item.ToolMaterial;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.WorldView;

/**
 * This structure describes a Family whose member Species all have a common branch.
 * <p>
 * A {@link Family} is more or less just a definition of {@link BranchBlock} blocks. It also defines the cellular
 * automata function of the {@link BranchBlock}.  It defines the type of wood that the tree is made of and consequently
 * what kind of log you get when you cut it down.
 * <p>
 * A DynamicTree does not contain a reference to a Seed, Leaves, Sapling, or how it should grow(how fast, how tall,
 * etc). It does not control what drops it produces or what fruit it grows.  It does not control where it should grow.
 * All of these capabilities lie in the Species class for which a DynamicTree should always contain one default
 * species(the common species).
 *
 * @author ferreusveritas
 */
public class Family extends RegistryEntry<Family> implements Resettable<Family> {

	public static final TypedRegistry.EntryType<Family> TYPE = TypedRegistry.newType(Family::new);

	public final static Family NULL_FAMILY = new Family() {
		@Override
		public void setupCommonSpecies(Species species) {
		}

		@Override
		public Species getCommonSpecies() {
			return Species.NULL_SPECIES;
		}

		@Override
		public boolean onTreeActivated(World world, BlockPos hitPos, BlockState state, PlayerEntity player, Hand hand, ItemStack heldItem, BlockHitResult hit) {
			return false;
		}

		@Override
		public ItemStack getStick(int qty) {
			return ItemStack.EMPTY;
		}

		@Override
		public BranchBlock getValidBranchBlock(int index) {
			return null;
		}

		@Override
		public Species getSpeciesForLocation(WorldAccess world, BlockPos trunkPos) {
			return Species.NULL_SPECIES;
		}
	};

	/**
	 * Central registry for all {@link Family} objects.
	 */
	public static final TypedRegistry<Family> REGISTRY = new TypedRegistry<>(Family.class, NULL_FAMILY, TYPE);
	protected final MutableLazyValue<Generator<DTBlockStateProvider, Family>> branchStateGenerator =
			MutableLazyValue.supplied(BranchStateGenerator::new);
	protected final MutableLazyValue<Generator<DTBlockStateProvider, Family>> strippedBranchStateGenerator =
			MutableLazyValue.supplied(StrippedBranchStateGenerator::new);

	//Branches
	protected final MutableLazyValue<Generator<DTBlockStateProvider, Family>> surfaceRootStateGenerator =
			MutableLazyValue.supplied(SurfaceRootStateGenerator::new);
	protected final MutableLazyValue<Generator<DTItemModelProvider, Family>> branchItemModelGenerator =
			MutableLazyValue.supplied(BranchItemModelGenerator::new);
	/**
	 * A list of branches the tree accepts as its own. Used for the falling tree renderer
	 */
	private final List<BranchBlock> validBranches = new LinkedList<>();
	/**
	 * A list of child species, added to when tree family is set for species.
	 */
	private final Set<Species> species = new HashSet<>();
	/**
	 * Weather the branch can support cocoa pods on it's surface [default = false]
	 */
	public boolean canSupportCocoa = false;
	@OnlyIn(Dist.CLIENT)
	public int woodRingColor; // For rooty blocks
	@OnlyIn(Dist.CLIENT)
	public int woodBarkColor; // For rooty water
	protected Species commonSpecies;

	//Leaves
	protected LeavesProperties commonLeaves = LeavesProperties.NULL_PROPERTIES;
	/**
	 * Used to modify the getRadiusForCellKit call to create a special case
	 */
	protected boolean hasConiferVariants = false;
	protected boolean hasSurfaceRoot = false;

	//Misc
	protected boolean hasStrippedBranch = true;
	/**
	 * The dynamic branch used by this tree family
	 */
	private Supplier<BranchBlock> branch;
	/**
	 * The stripped variant of the branch used by this tree family
	 */
	private Supplier<BranchBlock> strippedBranch;
	/**
	 * The dynamic branch's block item
	 */
	private Supplier<Item> branchItem;
	/**
	 * The surface root used by this tree family
	 */
	private Supplier<SurfaceRootBlock> surfaceRoot;
	/**
	 * The primitive (vanilla) log to base the texture, drops, and other behavior from
	 */
	private Block primitiveLog = Blocks.AIR;
	/**
	 * The primitive stripped log to base the texture, drops, and other behavior from
	 */
	private Block primitiveStrippedLog = Blocks.AIR;
	/**
	 * The maximum radius of a {@link BranchBlock} belonging to this family. {@link Species#maxBranchRadius} will be
	 * clamped to this value.
	 */
	private int maxBranchRadius = BranchBlock.MAX_RADIUS;
	/**
	 * The stick that is returned when a whole log can't be dropped
	 */
	private Item stick = Items.STICK;
	private boolean isFireProof = false;
	private AbstractBlock.Settings properties;
	private int primaryThickness = 1;
	private int secondaryThickness = 2;

	///////////////////////////////////////////
	// SPECIES LOCATION OVERRIDES
	///////////////////////////////////////////
	private boolean branchIsLadder = true;
	private int maxSignalDepth = 32;

	///////////////////////////////////////////
	// INTERACTION
	///////////////////////////////////////////

	private Family() {
		this.setRegistryName(DTTrees.NULL);
	}

	/**
	 * Constructor suitable for derivative mods
	 *
	 * @param name The ResourceLocation of the tree e.g. "mymod:poplar"
	 */
	public Family(Identifier name) {
		this.setRegistryName(name);
		this.commonSpecies = Species.NULL_SPECIES;
	}

	public void setupBlocks() {
		this.setBranch(this.createBranch());
		this.setBranchItem(this.createBranchItem(this.getBranchRegName(""), this.branch));

		if (this.hasStrippedBranch()) {
			this.setStrippedBranch(this.createBranch(this.getBranchRegName("stripped_")));
		}

		if (this.hasSurfaceRoot()) {
			this.setSurfaceRoot(this.createSurfaceRoot());
		}
	}


	///////////////////////////////////////////
	// TREE PROPERTIES
	///////////////////////////////////////////

	public void setupCommonSpecies(final Species species) {
		// Set the common species and auto-generate seeds and saplings unless opted out.
		this.commonSpecies = species.setShouldGenerateSeedIfNull(true).setShouldGenerateSaplingIfNull(true)
				.generateSeed().generateSapling();
	}

	public Species getCommonSpecies() {
		return commonSpecies;
	}

	public void setCommonSpecies(final Species species) {
		this.commonSpecies = species;
	}

	public Family addSpecies(final Species species) {
		this.species.add(species);
		return this;
	}

	public Set<Species> getSpecies() {
		return this.species;
	}

	public Species getSpeciesForLocation(WorldAccess world, BlockPos trunkPos) {
		return this.getSpeciesForLocation(world, trunkPos, this.commonSpecies);
	}

	public Species getSpeciesForLocation(BlockView world, BlockPos trunkPos, Species defaultSpecies) {
		for (final Species species : this.species) {
			if (species.shouldOverrideCommon(world, trunkPos)) {
				return species;
			}
		}

		return defaultSpecies;
	}

	public boolean onTreeActivated(World world, BlockPos hitPos, BlockState state, PlayerEntity player, Hand hand, @Nullable ItemStack heldItem, BlockHitResult hit) {

		if (this.canSupportCocoa) {
			BlockPos pos = hit.getBlockPos();
			if (heldItem != null) {
				if (heldItem.getItem() == Items.COCOA_BEANS) {
					BranchBlock branch = TreeHelper.getBranch(state);
					if (branch != null && branch.getRadius(state) == 8) {
						if (hit.getSide() != Direction.UP && hit.getSide() != Direction.DOWN) {
							pos = pos.offset(hit.getSide());
						}
						if (world.isAir(pos)) {
							BlockState cocoaState = DTRegistries.COCOA_FRUIT.get().getPlacementState(new ItemPlacementContext(new ItemUsageContext(player, hand, hit)));
							assert cocoaState != null;
							Direction facing = cocoaState.get(HorizontalFacingBlock.FACING);
							world.setBlockState(pos, DTRegistries.COCOA_FRUIT.get().getDefaultState().with(HorizontalFacingBlock.FACING, facing), 2);
							if (!player.isCreative()) {
								heldItem.decrement(1);
							}
							return true;
						}
					}
				}
			}
		}

		BlockPos rootPos = TreeHelper.findRootNode(world, hitPos);

		if (canStripBranch(state, world, hitPos, player, heldItem)) {
			return stripBranch(state, world, hitPos, player, heldItem);
		}

		if (rootPos != BlockPos.ORIGIN) {
			return TreeHelper.getExactSpecies(world, hitPos).onTreeActivated(world, rootPos, hitPos, state, player, hand, heldItem, hit);
		}

		return false;
	}

	public boolean canStripBranch(BlockState state, World world, BlockPos pos, PlayerEntity player, ItemStack heldItem) {
		BranchBlock branchBlock = TreeHelper.getBranch(state);
		if (branchBlock == null) {
			return false;
		}
		return branchBlock.canBeStripped(state, world, pos, player, heldItem);
	}

	public boolean stripBranch(BlockState state, World world, BlockPos pos, PlayerEntity player, ItemStack heldItem) {
		if (this.hasStrippedBranch()) {
			this.getBranch().ifPresent(branch -> {
				branch.stripBranch(state, world, pos, player, heldItem);
				if (world.isClient) {
					world.playSound(player, pos, SoundEvents.ITEM_AXE_STRIP, SoundCategory.BLOCKS, 1.0F, 1.0F);
					WailaOther.invalidateWailaPosition();
				}
			});
			return this.getBranch().isPresent();
		} else {
			return false;
		}
	}

	public boolean isWood() {
		return true;
	}

	/**
	 * Creates the branch block. Can be overridden by sub-classes who want full control over registry and instantiation
	 * of the branch.
	 *
	 * @return A supplier for the {@link BranchBlock}.
	 */
	public Supplier<BranchBlock> createBranch() {
		return this.createBranch(this.getBranchRegName(""));
	}

	/**
	 * Gets a branch name with the given prefix and <tt>_branch</tt> as the suffix.
	 *
	 * @param prefix The prefix.
	 * @return The {@link Identifier} registry name for the branch.
	 */
	protected Identifier getBranchRegName(final String prefix) {
		return suffix(prefix(this.getRegistryName(), prefix), "_branch");
	}

	/**
	 * Instantiates and sets up the actual {@link BranchBlock} object. Can be overridden by sub-classes for custom
	 * branch blocks.
	 *
	 * @return The instantiated {@link BranchBlock}.
	 */
	protected BranchBlock createBranchBlock() {
		final BasicBranchBlock branch = this.isThick() ? new ThickBranchBlock(this.getProperties()) : new BasicBranchBlock(this.getProperties());
		if (this.isFireProof()) {
			branch.setFireSpreadSpeed(0).setFlammability(0);
		}
		return branch;
	}

	/**
	 * Creates branch block and adds it to the relevant {@link RegistryHandler}.
	 *
	 * @param registryName The {@link Identifier} registry name.
	 * @return A supplier for the {@link BranchBlock}.
	 */
	protected Supplier<BranchBlock> createBranch(final Identifier registryName) {
		return RegistryHandler.addBlock(registryName, this::createBranchBlock);
	}

	/**
	 * Creates and registers a {@link BlockItem} for the given branch with the given registry name.
	 *
	 * @param registryName The {@link Identifier} registry name for the item.
	 * @param branchSup    A supplier for the {@link BranchBlock} to create the {@link BlockItem} for.
	 * @return A supplier for the {@link BlockItem}.
	 */
	public Supplier<BlockItem> createBranchItem(final Identifier registryName, final Supplier<BranchBlock> branchSup) {
		return RegistryHandler.addItem(registryName, () -> new BlockItem(branchSup.get(), new Item.Settings()));
	}

	protected Supplier<BranchBlock> setupBranch(final Supplier<BranchBlock> branchBlockSup, final boolean canBeStripped) {
		return () -> {
			BranchBlock branchBlock = branchBlockSup.get();
			branchBlock.setFamily(this); // Link the branch to the tree.
			branchBlock.setCanBeStripped(canBeStripped);
			this.addValidBranches(branchBlock); // Add the branch as a valid branch.
			return branchBlock;
		};
	}

	public Optional<BranchBlock> getBranch() {
		return Optionals.ofBlock(this.branch);
	}

	protected Family setBranch(final Supplier<BranchBlock> branchSup) {
		this.branch = this.setupBranch(branchSup, this.hasStrippedBranch);
		return this;
	}

	/**
	 * Version of getBranch() used by jocodes to generate the tree.
	 * By default it acts just like getBranch() but it can be overriden
	 * by addons to customize the branch selected by the jocode
	 *
	 * @param world   The world the tree is generating in
	 * @param species The species of the tree generated
	 * @param pos     The position of the branch block
	 * @return branch block picked
	 */
	public Optional<BranchBlock> getBranchForPlacement(WorldAccess world, Species species, BlockPos pos) {
		return getBranch();
	}

	public Optional<BranchBlock> getStrippedBranch() {
		return Optionals.ofBlock(strippedBranch);
	}

	protected Family setStrippedBranch(final Supplier<BranchBlock> branch) {
		this.strippedBranch = this.setupBranch(branch, false);
		return this;
	}

	public Optional<Item> getBranchItem() {
		return Optionals.ofItem(branchItem.get());
	}

	@SuppressWarnings("unchecked")
	protected <T extends Item> Family setBranchItem(Supplier<T> branchItemSup) {
		this.branchItem = (Supplier<Item>) branchItemSup;
		return this;
	}

	public boolean isThick() {
		return this.maxBranchRadius > BranchBlock.MAX_RADIUS;
	}

	public int getMaxBranchRadius() {
		return this.maxBranchRadius;
	}

	public void setMaxBranchRadius(int maxBranchRadius) {
		this.maxBranchRadius = maxBranchRadius;
	}

	@OnlyIn(Dist.CLIENT)
	public int getRootColor(BlockState state, boolean getBark) {
		return getBark ? woodBarkColor : woodRingColor;
	}

	public void setHasConiferVariants(boolean hasConiferVariants) {
		this.hasConiferVariants = hasConiferVariants;
	}

	/**
	 * Used to set the type of stick that a tree drops when there's not enough wood volume for a log.
	 *
	 * @param item An itemstack of the stick
	 * @return {@link Family} for chaining calls
	 */
	public Family setStick(Item item) {
		stick = item;
		return this;
	}

	/**
	 * Get a quantity of whatever is considered a stick for this tree's type of wood.
	 *
	 * @param qty Number of sticks
	 * @return an {@link ItemStack} of sticky things
	 */
	public ItemStack getStick(int qty) {
		return this.stick == Items.AIR ? ItemStack.EMPTY : new ItemStack(this.stick, MathHelper.clamp(qty, 0, 64));
	}

	public void setCanSupportCocoa(boolean canSupportCocoa) {
		this.canSupportCocoa = canSupportCocoa;
	}

	/**
	 * Gets the primitive full block (vanilla)log that represents this tree's material. Chiefly used to determine the
	 * wood hardness for harvesting behavior.
	 *
	 * @return Block of the primitive log.
	 */
	public Optional<Block> getPrimitiveLog() {
		return Optionals.ofBlock(primitiveLog);
	}

	/**
	 * Used to set the type of log item that a tree drops when it's harvested. Use this function to explicitly set the
	 * itemstack instead of having it done automatically.
	 *
	 * @param primitiveLog A block object that is the log
	 * @param primitiveLog An itemStack of the log item
	 * @return {@link Family} for chaining calls
	 */
	public Family setPrimitiveLog(Block primitiveLog) {
		this.primitiveLog = primitiveLog;

		if (this.branch != null) {
			this.branch.get().setPrimitiveLogDrops(new ItemStack(primitiveLog));
		}

		return this;
	}

	public Optional<Block> getPrimitiveStrippedLog() {
		return Optionals.ofBlock(primitiveStrippedLog);
	}

	public Family setPrimitiveStrippedLog(Block primitiveStrippedLog) {
		this.primitiveStrippedLog = primitiveStrippedLog;

		if (this.strippedBranch != null) {
			this.strippedBranch.get().setPrimitiveLogDrops(new ItemStack(primitiveStrippedLog));
		}

		return this;
	}

	private List<ItemStack> getLogDropsForBranch(float volume, int branch) {
		BranchBlock branchBlock = getValidBranchBlock(branch);
		List<ItemStack> logs = new LinkedList<>();
		if (branchBlock != null) {
			branchBlock.getPrimitiveLogs(volume, logs);
		}
		return logs;
	}

	public boolean isFireProof() {
		return isFireProof;
	}

	public void setIsFireProof(boolean isFireProof) {
		this.isFireProof = isFireProof;
	}

	///////////////////////////////////////////
	//BRANCHES
	///////////////////////////////////////////

	public BlockSoundGroup getBranchSoundType(BlockState state, WorldView world, BlockPos pos, @Nullable Entity entity) {
		return this.getDefaultBranchSoundType();
	}

	/**
	 * {@code null} = can harvest with hand
	 */
	@Nullable
	public ToolMaterial getDefaultBranchHarvestTier() {
		return null;
	}

	/**
	 * {@code null} = can harvest with hand
	 */
	@Nullable
	public ToolMaterial getDefaultStrippedBranchHarvestTier() {
		return null;
	}

	public Material getDefaultBranchMaterial() {
		return Material.WOOD;
	}

	public BlockSoundGroup getDefaultBranchSoundType() {
		return BlockSoundGroup.WOOD;
	}

	public AbstractBlock.Settings getDefaultBranchProperties(final Material material, final MapColor materialColor) {
		return AbstractBlock.Settings.of(material, materialColor).sounds(this.getDefaultBranchSoundType()).dropsNothing().requiresTool();
	}

	/**
	 * Gets the {@link #properties} for this {@link Family} object.
	 *
	 * @return The {@link #properties} for this {@link Family} object.
	 */
	public AbstractBlock.Settings getProperties() {
		return this.properties == null ? this.getDefaultBranchProperties(this.getDefaultBranchMaterial(),
				this.getDefaultBranchMaterial().getColor()) : this.properties;
	}

	public Family setProperties(AbstractBlock.Settings properties) {
		this.properties = properties;
		return this;
	}

	public int getRadiusForCellKit(BlockView blockAccess, BlockPos pos, BlockState blockState, Direction dir, BranchBlock branch) {
		int radius = branch.getRadius(blockState);
		int meta = MetadataCell.NONE;
		if (hasConiferVariants && radius == getPrimaryThickness()) {
			if (blockAccess.getBlockState(pos.down()).getBlock() == branch) {
				meta = MetadataCell.CONIFERTOP;
			}
		}

		return MetadataCell.radiusAndMeta(radius, meta);
	}

	/**
	 * Thickness of a twig [default = 1]
	 */
	public int getPrimaryThickness() {
		return primaryThickness;
	}

	public void setPrimaryThickness(int primaryThickness) {
		this.primaryThickness = primaryThickness;
	}

	/**
	 * Thickness of the branch connected to a twig (radius == getPrimaryThickness) [default = 2]
	 */
	public int getSecondaryThickness() {
		return secondaryThickness;
	}

	public void setSecondaryThickness(int secondaryThickness) {
		this.secondaryThickness = secondaryThickness;
	}

	public boolean hasStrippedBranch() {
		return this.hasStrippedBranch;
	}

	public void setHasStrippedBranch(boolean hasStrippedBranch) {
		this.hasStrippedBranch = hasStrippedBranch;
	}

	public void addValidBranches(BranchBlock... branches) {
		this.validBranches.addAll(Arrays.asList(branches));
	}

	public int getBranchBlockIndex(BranchBlock block) {
		int index = this.validBranches.indexOf(block);
		if (index < 0) {
			LogManager.getLogger().warn("Block {} not valid branch for {}.", block, this);
			return 0;
		}
		return index;
	}

	@Nullable
	public BranchBlock getValidBranchBlock(int index) {
		if (index < validBranches.size())
			return this.validBranches.get(index);
		else {
			LogManager.getLogger().warn("Attempted to get branch block of index {} but {} only has {} valid branches.", index, this, validBranches.size());
			return this.validBranches.get(0);
		}
	}

	//Useful for addons
	public boolean isValidBranchBlock(BranchBlock block) {
		return this.validBranches.contains(block);
	}

	///////////////////////////////////////////
	// SURFACE ROOTS
	///////////////////////////////////////////

	public void setBranchIsLadder(boolean branchIsLadder) {
		this.branchIsLadder = branchIsLadder;
	}

	public boolean branchIsLadder() {
		return branchIsLadder;
	}

	public int getMaxSignalDepth() {
		return maxSignalDepth;
	}

	public void setMaxSignalDepth(int maxSignalDepth) {
		this.maxSignalDepth = maxSignalDepth;
	}

	public boolean hasSurfaceRoot() {
		return this.hasSurfaceRoot;
	}

	///////////////////////////////////////////
	// FALL ANIMATION HANDLING
	///////////////////////////////////////////

	public void setHasSurfaceRoot(boolean hasSurfaceRoot) {
		this.hasSurfaceRoot = hasSurfaceRoot;
	}

	///////////////////////////////////////////
	// LEAVES HANDLING
	///////////////////////////////////////////

	public Supplier<SurfaceRootBlock> createSurfaceRoot() {
		return RegistryHandler.addBlock(suffix(this.getRegistryName(), "_root"), () -> new SurfaceRootBlock(this));
	}

	public Optional<SurfaceRootBlock> getSurfaceRoot() {
		return Optionals.ofBlock(this.surfaceRoot);
	}

	protected Family setSurfaceRoot(Supplier<SurfaceRootBlock> surfaceRootSup) {
		this.surfaceRoot = surfaceRootSup;
		return this;
	}

	public AnimationHandler selectAnimationHandler(FallingTreeEntity fallingEntity) {
		return fallingEntity.defaultAnimationHandler();
	}

	/**
	 * When destroying leaves, an area is created from the branch endpoints to look for leaves blocks and destroy them.
	 * This area is then expanded by a certain size to make sure it covers all the leaves in the canopy.
	 *
	 * @return the expanded block bounds.
	 */
	public BlockBounds expandLeavesBlockBounds(BlockBounds bounds) {
		return bounds.expand(3);
	}

	public boolean isCompatibleDynamicLeaves(Species species, BlockState blockState, BlockView blockAccess, BlockPos pos) {
		final DynamicLeavesBlock leaves = TreeHelper.getLeaves(blockState);
		return (leaves != null) && (this == leaves.getFamily(blockState, blockAccess, pos)
				|| species.isValidLeafBlock(leaves));
	}

	public boolean isCompatibleGenericLeaves(final Species species, BlockState blockState, WorldAccess blockAccess, BlockPos pos) {
		return this.isCompatibleDynamicLeaves(species, blockState, blockAccess, pos);
	}

	public LeavesProperties getCommonLeaves() {
		return this.commonLeaves;
	}

	public void setCommonLeaves(LeavesProperties properties) {
		this.commonLeaves = properties;
		properties.setFamily(this);
	}

	public List<TagKey<Block>> defaultBranchTags() {
		return this.isFireProof ? Collections.singletonList(DTBlockTags.BRANCHES) :
				Collections.singletonList(DTBlockTags.BRANCHES_THAT_BURN);
	}

	public List<TagKey<Item>> defaultBranchItemTags() {
		return this.isFireProof ? Collections.singletonList(DTItemTags.BRANCHES) :
				Collections.singletonList(DTItemTags.BRANCHES_THAT_BURN);
	}

	public List<TagKey<Block>> defaultStrippedBranchTags() {
		return this.isFireProof ? Collections.singletonList(DTBlockTags.STRIPPED_BRANCHES) :
				Collections.singletonList(DTBlockTags.STRIPPED_BRANCHES_THAT_BURN);
	}

	/**
	 * @return a constructor for the relevant branch block model builder for the corresponding loader
	 */
	public BiFunction<BlockModelBuilder, ExistingFileHelper, BranchLoaderBuilder> getBranchLoaderConstructor() {
		return BranchLoaderBuilder::branch;
	}

	@Override
	public void generateStateData(DTBlockStateProvider provider) {
		// Generate branch block state and model.
		this.branchStateGenerator.get().generate(provider, this);
		this.strippedBranchStateGenerator.get().generate(provider, this);

		// Generate surface root block state and model.
		this.surfaceRootStateGenerator.get().generate(provider, this);
	}

	public Identifier getBranchItemParentLocation() {
		return io.github.steveplays28.dynamictreesfabric.DynamicTreesFabric.resLoc("item/branch");
	}

	public void addBranchTextures(BiConsumer<String, Identifier> textureConsumer, Identifier primitiveLogLocation) {
		textureConsumer.accept("bark", primitiveLogLocation);
		textureConsumer.accept("rings", suffix(primitiveLogLocation, "_top"));
	}

	@Override
	public void generateItemModelData(DTItemModelProvider provider) {
		// Generate branch item models.
		this.branchItemModelGenerator.get().generate(provider, this);
	}

	//////////////////////////////
	// JAVA OBJECT STUFF
	//////////////////////////////

	@Override
	public String toLoadDataString() {
		return this.getString(Pair.of("commonLeaves", this.commonLeaves), Pair.of("maxBranchRadius", this.maxBranchRadius),
				Pair.of("hasSurfaceRoot", this.hasSurfaceRoot), Pair.of("hasStrippedBranch", this.hasStrippedBranch));
	}

	@Override
	public String toReloadDataString() {
		return this.getString(Pair.of("commonLeaves", this.commonLeaves), Pair.of("maxBranchRadius", this.maxBranchRadius),
				Pair.of("commonSpecies", this.commonSpecies), Pair.of("primitiveLog", this.primitiveLog),
				Pair.of("primitiveStrippedLog", this.primitiveStrippedLog), Pair.of("stick", this.stick),
				Pair.of("hasConiferVariants", this.hasConiferVariants), Pair.of("canSupportCocoa", this.canSupportCocoa));
	}

}
