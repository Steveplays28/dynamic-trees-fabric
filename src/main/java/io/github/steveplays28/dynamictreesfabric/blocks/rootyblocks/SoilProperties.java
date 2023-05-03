package io.github.steveplays28.dynamictreesfabric.blocks.rootyblocks;

import static io.github.steveplays28.dynamictreesfabric.util.ResourceLocationUtils.prefix;

import java.util.Optional;
import java.util.function.Supplier;

import javax.annotation.Nullable;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.steveplays28.dynamictreesfabric.api.data.Generator;
import io.github.steveplays28.dynamictreesfabric.api.data.SoilStateGenerator;
import io.github.steveplays28.dynamictreesfabric.api.registry.RegistryEntry;
import io.github.steveplays28.dynamictreesfabric.api.registry.RegistryHandler;
import io.github.steveplays28.dynamictreesfabric.api.registry.TypedRegistry;
import io.github.steveplays28.dynamictreesfabric.blocks.leaves.LeavesProperties;
import io.github.steveplays28.dynamictreesfabric.data.provider.DTBlockStateProvider;
import io.github.steveplays28.dynamictreesfabric.init.DTTrees;
import io.github.steveplays28.dynamictreesfabric.resources.Resources;
import io.github.steveplays28.dynamictreesfabric.trees.Resettable;
import io.github.steveplays28.dynamictreesfabric.util.MutableLazyValue;
import io.github.steveplays28.dynamictreesfabric.util.Optionals;

import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.MapColor;
import net.minecraft.block.Material;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.util.Identifier;

/**
 * @author Max Hyper
 */
public class SoilProperties extends RegistryEntry<SoilProperties> implements Resettable<SoilProperties> {

	public static final Codec<SoilProperties> CODEC = RecordCodecBuilder.create(instance -> instance
			.group(Identifier.CODEC.fieldOf(Resources.RESOURCE_LOCATION.toString()).forGetter(SoilProperties::getRegistryName))
			.apply(instance, SoilProperties::new));

	public static final SoilProperties NULL_SOIL_PROPERTIES = new SoilProperties() {
		@Override
		public Block getPrimitiveSoilBlock() {
			return Blocks.AIR;
		}

		@Override
		public Optional<RootyBlock> getBlock() {
			return Optional.empty();
		}

		@Override
		public Integer getSoilFlags() {
			return 0;
		}

		@Override
		public void generateBlock(AbstractBlock.Settings properties) {
		}
	}.setRegistryName(DTTrees.NULL).setBlockRegistryName(DTTrees.NULL);

	/**
	 * Central registry for all {@link LeavesProperties} objects.
	 */
	public static final TypedRegistry<SoilProperties> REGISTRY = new TypedRegistry<>(SoilProperties.class, NULL_SOIL_PROPERTIES, new TypedRegistry.EntryType<>(CODEC));
	protected final MutableLazyValue<Generator<DTBlockStateProvider, SoilProperties>> soilStateGenerator =
			MutableLazyValue.supplied(SoilStateGenerator::new);
	protected Block primitiveSoilBlock;
	protected Supplier<RootyBlock> block;
	protected Integer soilFlags = 0;
	protected boolean hasSubstitute;
	protected boolean worldGenOnly;
	private Identifier blockRegistryName;

	//used for null soil properties
	protected SoilProperties() {
	}

	//used for Dirt Helper registrations only
	protected SoilProperties(final Block primitiveBlock, Identifier name, Integer soilFlags, boolean generate) {
		this(primitiveBlock, name);
		this.soilFlags = soilFlags;
		if (generate) {
			generateBlock(AbstractBlock.Settings.copy(primitiveBlock));
		}
	}

	public SoilProperties(final Identifier registryName) {
		this(null, registryName);
	}

	///////////////////////////////////////////
	// PRIMITIVE SOIL
	///////////////////////////////////////////

	public SoilProperties(@Nullable final Block primitiveBlock, final Identifier registryName) {
		super(registryName);
		this.primitiveSoilBlock = primitiveBlock != null ? primitiveBlock : Blocks.AIR;
	}

	public Block getPrimitiveSoilBlock() {
		return primitiveSoilBlock;
	}

	public void setPrimitiveSoilBlock(final Block primitiveSoil) {
		if (this.primitiveSoilBlock == null || primitiveSoil != this.primitiveSoilBlock) {
			this.primitiveSoilBlock = primitiveSoil;
		}
		SoilHelper.addSoilPropertiesToMap(this);
	}

	public Optional<Block> getPrimitiveSoilBlockOptional() {
		return Optionals.ofBlock(primitiveSoilBlock);
	}

	/**
	 * Allows to veto a soil block based on the BlockState.
	 */
	public boolean isValidState(BlockState primitiveSoilState) {
		return true;
	}

	/**
	 * primitiveSoilState should always be this soil's primitive block, but if used on, verify anyways.
	 *
	 * @return the BlockState of the rooty soil.
	 */
	public BlockState getSoilState(BlockState primitiveSoilState, int fertility, boolean requireTileEntity) {
		return block.get().getDefaultState().with(RootyBlock.FERTILITY, fertility).with(RootyBlock.IS_VARIANT, requireTileEntity);
	}

	/**
	 * @return the BlockState of the primitive soil that is set when it is no longer supporting a tree.
	 */
	public BlockState getPrimitiveSoilState(BlockState currentSoilState) {
		return primitiveSoilBlock.getDefaultState();
	}

	public boolean isWorldGenOnly() {
		return worldGenOnly;
	}

	///////////////////////////////////////////
	// ROOTY BLOCK
	///////////////////////////////////////////

	public void setWorldGenOnly(boolean worldGenOnly) {
		this.worldGenOnly = worldGenOnly;
	}

	protected String getBlockRegistryNamePrefix() {
		return "rooty_";
	}

	public Identifier getBlockRegistryName() {
		return this.blockRegistryName;
	}

	public SoilProperties setBlockRegistryName(Identifier blockRegistryName) {
		this.blockRegistryName = blockRegistryName;
		return this;
	}

	private void setBlockRegistryNameIfNull() {
		if (this.blockRegistryName == null) {
			this.blockRegistryName = prefix(this.getRegistryName(), this.getBlockRegistryNamePrefix());
		}
	}

	public Optional<RootyBlock> getBlock() {
		return Optionals.ofBlock(block.get());
	}

	public void setBlock(RootyBlock rootyBlock) {
		this.block = () -> rootyBlock;
	}

	public Optional<RootyBlock> getSoilBlock() {
		return Optional.ofNullable(this.block == Blocks.AIR ? null : this.block.get());
	}

	public void generateBlock(AbstractBlock.Settings blockProperties) {
		setBlockRegistryNameIfNull();
		this.block = RegistryHandler.addBlock(this.blockRegistryName, () -> this.createBlock(blockProperties));
	}

	protected RootyBlock createBlock(AbstractBlock.Settings blockProperties) {
		return new RootyBlock(this, blockProperties);
	}

	public boolean hasSubstitute() {
		return hasSubstitute;
	}

	///////////////////////////////////////////
	// MATERIAL
	///////////////////////////////////////////

	public void setHasSubstitute(boolean hasSubstitute) {
		this.hasSubstitute = hasSubstitute;
	}

	public Material getDefaultMaterial() {
		return Material.SOIL;
	}

	///////////////////////////////////////////
	// SOIL FLAGS
	///////////////////////////////////////////

	public AbstractBlock.Settings getDefaultBlockProperties(final Material material, final MapColor materialColor) {
		return AbstractBlock.Settings.of(material, materialColor).strength(0.5F).sounds(BlockSoundGroup.GRAVEL);
	}

	public Integer getSoilFlags() {
		return soilFlags;
	}

	public SoilProperties setSoilFlags(Integer adjFlag) {
		this.soilFlags = adjFlag;
		return this;
	}

	///////////////////////////////////////////
	// DATA GEN
	///////////////////////////////////////////

	public SoilProperties addSoilFlags(Integer adjFlag) {
		this.soilFlags |= adjFlag;
		return this;
	}

	@Override
	public void generateStateData(DTBlockStateProvider provider) {
		// Generate soil state and model.
		this.soilStateGenerator.get().generate(provider, this);
	}

	public Identifier getRootsOverlayLocation() {
		return io.github.steveplays28.dynamictreesfabric.DynamicTreesFabric.resLoc("block/roots");
	}

	//////////////////////////////
	// JAVA OBJECT STUFF
	//////////////////////////////

	@Override
	public String toString() {
		return getRegistryName().toString();
	}
}
