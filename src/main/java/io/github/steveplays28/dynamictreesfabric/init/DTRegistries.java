package io.github.steveplays28.dynamictreesfabric.init;

import io.github.steveplays28.dynamictreesfabric.api.TreeHelper;
import io.github.steveplays28.dynamictreesfabric.api.TreeRegistry;
import io.github.steveplays28.dynamictreesfabric.api.cells.CellKit;
import io.github.steveplays28.dynamictreesfabric.api.registry.RegistryEvent;
import io.github.steveplays28.dynamictreesfabric.api.registry.RegistryHandler;
import io.github.steveplays28.dynamictreesfabric.api.worldgen.FeatureCanceller;
import io.github.steveplays28.dynamictreesfabric.blocks.DynamicCocoaBlock;
import io.github.steveplays28.dynamictreesfabric.blocks.FruitBlock;
import io.github.steveplays28.dynamictreesfabric.blocks.PottedSaplingBlock;
import io.github.steveplays28.dynamictreesfabric.blocks.branches.BranchBlock;
import io.github.steveplays28.dynamictreesfabric.blocks.branches.TrunkShellBlock;
import io.github.steveplays28.dynamictreesfabric.blocks.rootyblocks.RootyBlock;
import io.github.steveplays28.dynamictreesfabric.blocks.rootyblocks.SoilProperties;
import io.github.steveplays28.dynamictreesfabric.cells.CellKits;
import io.github.steveplays28.dynamictreesfabric.entities.FallingTreeEntity;
import io.github.steveplays28.dynamictreesfabric.entities.LingeringEffectorEntity;
import io.github.steveplays28.dynamictreesfabric.growthlogic.GrowthLogicKit;
import io.github.steveplays28.dynamictreesfabric.growthlogic.GrowthLogicKits;
import io.github.steveplays28.dynamictreesfabric.items.DendroPotion;
import io.github.steveplays28.dynamictreesfabric.items.DirtBucket;
import io.github.steveplays28.dynamictreesfabric.items.Staff;
import io.github.steveplays28.dynamictreesfabric.systems.BranchConnectables;
import io.github.steveplays28.dynamictreesfabric.systems.dropcreators.DropCreator;
import io.github.steveplays28.dynamictreesfabric.systems.dropcreators.DropCreators;
import io.github.steveplays28.dynamictreesfabric.systems.genfeatures.GenFeature;
import io.github.steveplays28.dynamictreesfabric.systems.genfeatures.GenFeatures;
import io.github.steveplays28.dynamictreesfabric.tileentity.PottedSaplingTileEntity;
import io.github.steveplays28.dynamictreesfabric.tileentity.SpeciesTileEntity;
import io.github.steveplays28.dynamictreesfabric.trees.Species;
import io.github.steveplays28.dynamictreesfabric.util.holderset.IncludesExcludesHolderSet;
import io.github.steveplays28.dynamictreesfabric.util.holderset.NameRegexMatchHolderSet;
import io.github.steveplays28.dynamictreesfabric.worldgen.DynamicTreeFeature;
import io.github.steveplays28.dynamictreesfabric.worldgen.biomemodifiers.AddDynamicTreesBiomeModifier;
import io.github.steveplays28.dynamictreesfabric.worldgen.biomemodifiers.RunFeatureCancellersBiomeModifier;
import io.github.steveplays28.dynamictreesfabric.worldgen.cancellers.FungusFeatureCanceller;
import io.github.steveplays28.dynamictreesfabric.worldgen.cancellers.MushroomFeatureCanceller;
import io.github.steveplays28.dynamictreesfabric.worldgen.cancellers.TreeFeatureCanceller;
import com.google.common.base.Suppliers;
import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.HugeFungusConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.HugeMushroomFeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraftforge.common.world.BiomeModifier;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegisterEvent;
import net.minecraftforge.registries.RegistryObject;
import net.minecraftforge.registries.holdersets.HolderSetType;

import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class DTRegistries {

    /**
     * This is the creative tab that holds all DT items. Must be instantiated here so that it's not {@code null} when we
     * create blocks and items.
     */
    public static final CreativeModeTab ITEM_GROUP = new CreativeModeTab(io.github.steveplays28.dynamictreesfabric.DynamicTreesFabric.MOD_ID) {
        @Override
        public ItemStack makeIcon() {
            return TreeRegistry.findSpecies(DTTrees.OAK).getSeedStack(1);
        }
    };

    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES = DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, io.github.steveplays28.dynamictreesfabric.DynamicTreesFabric.MOD_ID);
    public static final DeferredRegister<ConfiguredFeature<?, ?>> CONFIGURED_FEATURES = DeferredRegister.create(Registry.CONFIGURED_FEATURE_REGISTRY, io.github.steveplays28.dynamictreesfabric.DynamicTreesFabric.MOD_ID);
    public static final DeferredRegister<PlacedFeature> PLACED_FEATURES = DeferredRegister.create(Registry.PLACED_FEATURE_REGISTRY, io.github.steveplays28.dynamictreesfabric.DynamicTreesFabric.MOD_ID);
    public static final DeferredRegister<Feature<?>> FEATURES = DeferredRegister.create(ForgeRegistries.FEATURES, io.github.steveplays28.dynamictreesfabric.DynamicTreesFabric.MOD_ID);
    public static final DeferredRegister<Codec<? extends BiomeModifier>> BIOME_MODIFIER_SERIALIZERS = DeferredRegister.create(ForgeRegistries.Keys.BIOME_MODIFIER_SERIALIZERS, io.github.steveplays28.dynamictreesfabric.DynamicTreesFabric.MOD_ID);
    public static final DeferredRegister<HolderSetType> HOLDER_SET_TYPES = DeferredRegister.create(ForgeRegistries.Keys.HOLDER_SET_TYPES, io.github.steveplays28.dynamictreesfabric.DynamicTreesFabric.MOD_ID);

    ///////////////////////////////////////////
    // BLOCKS
    ///////////////////////////////////////////

    /**
     * An apple fruit block.
     */
    public static final Supplier<FruitBlock> APPLE_FRUIT = Suppliers.memoize(() -> new FruitBlock().setDroppedItem(new ItemStack(Items.APPLE))
            .setCanBoneMeal(DTConfigs.CAN_BONE_MEAL_APPLE::get));

    /**
     * A modified cocoa fruit block (for dynamic trees).
     */
    public static final Supplier<DynamicCocoaBlock> COCOA_FRUIT = Suppliers.memoize(DynamicCocoaBlock::new);

    /**
     * A potted sapling block, which is a normal pot but for dynamic saplings.
     */
    public static final Supplier<PottedSaplingBlock> POTTED_SAPLING = Suppliers.memoize(PottedSaplingBlock::new);

    /**
     * A trunk shell block, which is the outer block for thick branches.
     */
    public static final Supplier<TrunkShellBlock> TRUNK_SHELL = Suppliers.memoize(TrunkShellBlock::new);

    public static void setup() {
        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
        ENTITY_TYPES.register(modBus);
        CONFIGURED_FEATURES.register(modBus);
        PLACED_FEATURES.register(modBus);
        FEATURES.register(modBus);
        BIOME_MODIFIER_SERIALIZERS.register(modBus);
        HOLDER_SET_TYPES.register(modBus);

        setupBlocks();
        setupConnectables();
        setupItems();
    }

    private static void setupBlocks() {
        RegistryHandler.addBlock(io.github.steveplays28.dynamictreesfabric.DynamicTreesFabric.resLoc("apple_fruit"), APPLE_FRUIT);
        RegistryHandler.addBlock(io.github.steveplays28.dynamictreesfabric.DynamicTreesFabric.resLoc("cocoa"), COCOA_FRUIT);
        RegistryHandler.addBlock(PottedSaplingBlock.REG_NAME, POTTED_SAPLING);
        RegistryHandler.addBlock(io.github.steveplays28.dynamictreesfabric.DynamicTreesFabric.resLoc("trunk_shell"), TRUNK_SHELL);
    }

    private static void setupConnectables() {
        BranchConnectables.makeBlockConnectable(Blocks.BEE_NEST, (state, world, pos, side) -> {
            if (side == Direction.DOWN) {
                return 1;
            }
            return 0;
        });

        BranchConnectables.makeBlockConnectable(Blocks.SHROOMLIGHT, (state, world, pos, side) -> {
            if (side == Direction.DOWN) {
                BlockState branchState = world.getBlockState(pos.relative(Direction.UP));
                BranchBlock branch = TreeHelper.getBranch(branchState);
                if (branch != null) {
                    return Mth.clamp(branch.getRadius(branchState) - 1, 1, 8);
                } else {
                    return 8;
                }
            }
            return 0;
        });
    }

    @SubscribeEvent
    public static void onBlocksRegistry(final RegisterEvent event) {
        event.register(ForgeRegistries.Keys.BLOCKS, registerHelper -> {
            final Species appleOak = Species.REGISTRY.get(io.github.steveplays28.dynamictreesfabric.DynamicTreesFabric.resLoc("apple_oak"));
            if (appleOak.isValid()) {
                APPLE_FRUIT.get().setSpecies(appleOak);
            }
        });
    }

    ///////////////////////////////////////////
    // ITEMS
    ///////////////////////////////////////////

    /**
     * A custom potion called the Dendro Potion, houses all tree potions.
     */
    public static final Supplier<DendroPotion> DENDRO_POTION = Suppliers.memoize(DendroPotion::new);

    /**
     * A bucket of dirt item, for crafting saplings into seeds and vice versa.
     */
    public static final Supplier<DirtBucket> DIRT_BUCKET = Suppliers.memoize(DirtBucket::new);

    /**
     * A staff, a creative tool for copying and pasting tree shapes.
     */
    public static final Supplier<Staff> STAFF = Suppliers.memoize(Staff::new);

    private static void setupItems() {
        RegistryHandler.addItem(io.github.steveplays28.dynamictreesfabric.DynamicTreesFabric.resLoc("staff"), STAFF);
        RegistryHandler.addItem(io.github.steveplays28.dynamictreesfabric.DynamicTreesFabric.resLoc("dirt_bucket"), DIRT_BUCKET);
        RegistryHandler.addItem(io.github.steveplays28.dynamictreesfabric.DynamicTreesFabric.resLoc("dendro_potion"), DENDRO_POTION);
    }

    ///////////////////////////////////////////
    // ENTITIES
    ///////////////////////////////////////////

    public static final Supplier<EntityType<FallingTreeEntity>> FALLING_TREE = registerEntity("falling_tree", () -> EntityType.Builder.<FallingTreeEntity>of(FallingTreeEntity::new, MobCategory.MISC)
            .setShouldReceiveVelocityUpdates(true)
            .setTrackingRange(512)
            .setUpdateInterval(Integer.MAX_VALUE)
            .setCustomClientFactory((spawnEntity, world) -> new FallingTreeEntity(world)));
    public static final Supplier<EntityType<LingeringEffectorEntity>> LINGERING_EFFECTOR = registerEntity("lingering_effector", () -> EntityType.Builder.<LingeringEffectorEntity>of(LingeringEffectorEntity::new, MobCategory.MISC)
            .setCustomClientFactory((spawnEntity, world) ->
                    new LingeringEffectorEntity(world, new BlockPos(spawnEntity.getPosX(), spawnEntity.getPosY(), spawnEntity.getPosZ()), null)));

    private static <T extends Entity> Supplier<EntityType<T>> registerEntity(String name, Supplier<EntityType.Builder<T>> builderSupplier) {
        return ENTITY_TYPES.register(name, () -> builderSupplier.get().build(name));
    }

    ///////////////////////////////////////////
    // TILE ENTITIES
    ///////////////////////////////////////////

    public static BlockEntityType<SpeciesTileEntity> speciesTE;
    public static BlockEntityType<PottedSaplingTileEntity> bonsaiTE;

    public static void setupTileEntities() {
        RootyBlock[] rootyBlocks = SoilProperties.REGISTRY.getAll().stream()
                .map(SoilProperties::getBlock)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .distinct()
                .toArray(RootyBlock[]::new);

        speciesTE = BlockEntityType.Builder.of(SpeciesTileEntity::new, rootyBlocks).build(null);
        bonsaiTE = BlockEntityType.Builder.of(PottedSaplingTileEntity::new, POTTED_SAPLING.get()).build(null);
    }

    @SubscribeEvent
    public static void onTileEntitiesRegistry(final RegisterEvent tileEntityRegistryEvent) {
        tileEntityRegistryEvent.register(ForgeRegistries.Keys.BLOCK_ENTITY_TYPES, registerHelper -> {
            setupTileEntities();
            registerHelper.register(PottedSaplingBlock.REG_NAME, bonsaiTE);
            registerHelper.register(io.github.steveplays28.dynamictreesfabric.DynamicTreesFabric.resLoc("tile_entity_species"), speciesTE);
        });
    }

    ///////////////////////////////////////////
    // WORLD GEN
    ///////////////////////////////////////////

    public static final RegistryObject<DynamicTreeFeature> DYNAMIC_TREE_FEATURE = FEATURES.register("dynamic_tree", DynamicTreeFeature::new);

    public static final RegistryObject<ConfiguredFeature<NoneFeatureConfiguration, ?>> DYNAMIC_TREE_CONFIGURED_FEATURE = CONFIGURED_FEATURES.register("dynamic_tree",
            () -> new ConfiguredFeature<>(DYNAMIC_TREE_FEATURE.get(), NoneFeatureConfiguration.INSTANCE));

    public static final RegistryObject<PlacedFeature> DYNAMIC_TREE_PLACED_FEATURE = PLACED_FEATURES.register("dynamic_tree_placed_feature",
            () -> new PlacedFeature(Holder.hackyErase(DYNAMIC_TREE_CONFIGURED_FEATURE.getHolder().get()), List.of()/*VegetationPlacements.treePlacement(PlacementUtils.countExtra(10, 0.1F, 1))*/));

    public static final RegistryObject<Codec<AddDynamicTreesBiomeModifier>> ADD_DYNAMIC_TREES_BIOME_MODIFIER = BIOME_MODIFIER_SERIALIZERS.register("add_dynamic_trees",
            () -> Codec.unit(AddDynamicTreesBiomeModifier::new));
    public static final RegistryObject<Codec<RunFeatureCancellersBiomeModifier>> RUN_FEATURE_CANCELLERS_BIOME_MODIFIER = BIOME_MODIFIER_SERIALIZERS.register("run_feature_cancellers",
            () -> Codec.unit(RunFeatureCancellersBiomeModifier::new));

    public static final RegistryObject<HolderSetType> INCLUDES_EXCLUDES_HOLDER_SET_TYPE = HOLDER_SET_TYPES.register("includes_excludes", () -> IncludesExcludesHolderSet::codec);
    public static final RegistryObject<HolderSetType> NAME_REGEX_MATCH_HOLDER_SET_TYPE = HOLDER_SET_TYPES.register("name_regex_match", () -> NameRegexMatchHolderSet::codec);
    public static final RegistryObject<HolderSetType> TAGS_REGEX_MATCH_HOLDER_SET_TYPE = HOLDER_SET_TYPES.register("tags_regex_match", () -> NameRegexMatchHolderSet::codec);

    public static final FeatureCanceller TREE_CANCELLER = new TreeFeatureCanceller<>(io.github.steveplays28.dynamictreesfabric.DynamicTreesFabric.resLoc("tree"), TreeConfiguration.class);

    public static final FeatureCanceller FUNGUS_CANCELLER = new FungusFeatureCanceller<>(io.github.steveplays28.dynamictreesfabric.DynamicTreesFabric.resLoc("fungus"), HugeFungusConfiguration.class);

    public static final FeatureCanceller MUSHROOM_CANCELLER = new MushroomFeatureCanceller<>(io.github.steveplays28.dynamictreesfabric.DynamicTreesFabric.resLoc("mushroom"), HugeMushroomFeatureConfiguration.class);

    @SubscribeEvent
    public static void onFeatureCancellerRegistry(final RegistryEvent<FeatureCanceller> event) {
        event.getRegistry().registerAll(TREE_CANCELLER, FUNGUS_CANCELLER, MUSHROOM_CANCELLER);
    }

    ///////////////////////////////////////////
    // CUSTOM TREE LOGIC
    ///////////////////////////////////////////

    @SubscribeEvent
    public static void onCellKitRegistry(final RegistryEvent<CellKit> event) {
        CellKits.register(event.getRegistry());
    }

    @SubscribeEvent
    public static void onGrowthLogicKitRegistry(final RegistryEvent<GrowthLogicKit> event) {
        GrowthLogicKits.register(event.getRegistry());
    }

    @SubscribeEvent
    public static void onGenFeatureRegistry(final RegistryEvent<GenFeature> event) {
        GenFeatures.register(event.getRegistry());
    }

    @SubscribeEvent
    public static void onDropCreatorRegistry(final RegistryEvent<DropCreator> event) {
        DropCreators.register(event.getRegistry());
    }

}
