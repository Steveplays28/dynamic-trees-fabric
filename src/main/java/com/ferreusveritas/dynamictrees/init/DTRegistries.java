package com.ferreusveritas.dynamictrees.init;

import com.ferreusveritas.dynamictrees.DynamicTrees;
import com.ferreusveritas.dynamictrees.api.TreeHelper;
import com.ferreusveritas.dynamictrees.api.TreeRegistry;
import com.ferreusveritas.dynamictrees.api.cell.CellKit;
import com.ferreusveritas.dynamictrees.api.registry.RegistryHandler;
import com.ferreusveritas.dynamictrees.api.worldgen.FeatureCanceller;
import com.ferreusveritas.dynamictrees.block.PottedSaplingBlock;
import com.ferreusveritas.dynamictrees.block.branch.BranchBlock;
import com.ferreusveritas.dynamictrees.block.branch.TrunkShellBlock;
import com.ferreusveritas.dynamictrees.block.entity.PottedSaplingBlockEntity;
import com.ferreusveritas.dynamictrees.block.entity.SpeciesBlockEntity;
import com.ferreusveritas.dynamictrees.block.rooty.RootyBlock;
import com.ferreusveritas.dynamictrees.block.rooty.SoilProperties;
import com.ferreusveritas.dynamictrees.cell.CellKits;
import com.ferreusveritas.dynamictrees.entity.FallingTreeEntity;
import com.ferreusveritas.dynamictrees.entity.LingeringEffectorEntity;
import com.ferreusveritas.dynamictrees.growthlogic.GrowthLogicKit;
import com.ferreusveritas.dynamictrees.growthlogic.GrowthLogicKits;
import com.ferreusveritas.dynamictrees.item.DendroPotion;
import com.ferreusveritas.dynamictrees.item.DirtBucket;
import com.ferreusveritas.dynamictrees.item.Staff;
import com.ferreusveritas.dynamictrees.loot.condition.DTLootConditions;
import com.ferreusveritas.dynamictrees.loot.entry.DTLootPoolEntries;
import com.ferreusveritas.dynamictrees.loot.function.DTLootFunctions;
import com.ferreusveritas.dynamictrees.systems.BranchConnectables;
import com.ferreusveritas.dynamictrees.systems.genfeature.GenFeature;
import com.ferreusveritas.dynamictrees.systems.genfeature.GenFeatures;
import com.ferreusveritas.dynamictrees.util.holderset.IncludesExcludesHolderSet;
import com.ferreusveritas.dynamictrees.util.holderset.NameRegexMatchHolderSet;
import com.ferreusveritas.dynamictrees.worldgen.CaveRootedTreeFeature;
import com.ferreusveritas.dynamictrees.worldgen.CaveRootedTreePlacement;
import com.ferreusveritas.dynamictrees.worldgen.DynamicTreeFeature;
import com.ferreusveritas.dynamictrees.worldgen.biomemodifiers.AddDynamicTreesBiomeModifier;
import com.ferreusveritas.dynamictrees.worldgen.biomemodifiers.RunFeatureCancellersBiomeModifier;
import com.ferreusveritas.dynamictrees.worldgen.feature.DTReplaceNyliumFungiBlockStateProvider;
import com.ferreusveritas.dynamictrees.worldgen.featurecancellation.FungusFeatureCanceller;
import com.ferreusveritas.dynamictrees.worldgen.featurecancellation.MushroomFeatureCanceller;
import com.ferreusveritas.dynamictrees.worldgen.featurecancellation.TreeFeatureCanceller;
import com.ferreusveritas.dynamictrees.worldgen.structure.DTCancelVanillaTreePoolElement;
import com.ferreusveritas.dynamictrees.worldgen.structure.TreePoolElement;
import com.google.common.base.Suppliers;
import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.HugeFungusConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.HugeMushroomFeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.RootSystemConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProviderType;
import net.minecraft.world.level.levelgen.placement.PlacementModifierType;
import net.minecraft.world.level.levelgen.structure.pools.StructurePoolElementType;
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

import java.util.LinkedList;
import java.util.Optional;
import java.util.function.Supplier;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class DTRegistries {
//    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES = DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, DynamicTrees.MOD_ID);
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, DynamicTrees.MOD_ID);
//    public static final DeferredRegister<PlacementModifierType<?>> PLACEMENT_MODIFIER_TYPES = DeferredRegister.create(Registries.PLACEMENT_MODIFIER_TYPE, DynamicTrees.MOD_ID);
//    public static final DeferredRegister<SoundEvent> SOUND_EVENTS = DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, DynamicTrees.MOD_ID);
    public static final DeferredRegister<Codec<? extends BiomeModifier>> BIOME_MODIFIER_SERIALIZERS = DeferredRegister.create(ForgeRegistries.Keys.BIOME_MODIFIER_SERIALIZERS, DynamicTrees.MOD_ID);
//    public static final DeferredRegister<HolderSetType> HOLDER_SET_TYPES = DeferredRegister.create(ForgeRegistries.Keys.HOLDER_SET_TYPES, DynamicTrees.MOD_ID);
//    public static final DeferredRegister<BlockStateProviderType<?>> BLOCK_STATE_PROVIDER_TYPES = DeferredRegister.create(Registries.BLOCK_STATE_PROVIDER_TYPE, DynamicTrees.MOD_ID);
    public static final DeferredRegister<StructurePoolElementType<?>> STRUCTURE_POOL_ELEMENT_TYPES = DeferredRegister.create(Registries.STRUCTURE_POOL_ELEMENT, DynamicTrees.MOD_ID);

    public static final LinkedList<Item> CREATIVE_TAB_ITEMS = new LinkedList<>();
    public static final RegistryObject<CreativeModeTab> DT_CREATIVE_TAB = CREATIVE_MODE_TABS.register(DynamicTrees.MOD_ID, () -> CreativeModeTab.builder()
            .icon(() -> TreeRegistry.findSpecies(DTTrees.OAK).getSeedStack(1))
            .title(Component.translatable("itemGroup.dynamictrees"))
            .displayItems((parameters, output) -> {
                for (final DendroPotion.DendroPotionType potion : DendroPotion.DendroPotionType.values()) {
                    if (potion.isActive()) {
                        output.accept(DendroPotion.applyIndexTag(new ItemStack(DTRegistries.DENDRO_POTION.get()), potion.getIndex()));
                    }
                }
                CREATIVE_TAB_ITEMS.forEach(e -> output.accept(e.getDefaultInstance()));
            }).build());

    ///////////////////////////////////////////
    // BLOCKS
    ///////////////////////////////////////////

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
//        ENTITY_TYPES.register(modBus);
        CREATIVE_MODE_TABS.register(modBus);
//        PLACEMENT_MODIFIER_TYPES.register(modBus);
//        SOUND_EVENTS.register(modBus);
        BIOME_MODIFIER_SERIALIZERS.register(modBus);
//        HOLDER_SET_TYPES.register(modBus);
//        BLOCK_STATE_PROVIDER_TYPES.register(modBus);
        STRUCTURE_POOL_ELEMENT_TYPES.register(modBus);
//        DTLootPoolEntries.LOOT_POOL_ENTRY_TYPES.register(modBus);
//        DTLootConditions.LOOT_CONDITION_TYPES.register(modBus);
//        DTLootFunctions.LOOT_FUNCTION_TYPES.register(modBus);

        setupBlocks();
        setupConnectables();
        setupItems();
    }

    private static void setupBlocks() {
        RegistryHandler.addBlock(PottedSaplingBlock.REG_NAME, POTTED_SAPLING);
        RegistryHandler.addBlock(DynamicTrees.location("trunk_shell"), TRUNK_SHELL);
    }

    private static void setupConnectables() {
        BranchConnectables.makeBlockConnectable(Blocks.BEE_NEST, (state, level, pos, side) -> {
            if (side == Direction.DOWN) {
                return 1;
            }
            return 0;
        });

        BranchConnectables.makeBlockConnectable(Blocks.SHROOMLIGHT, (state, level, pos, side) -> {
            if (side == Direction.DOWN) {
                BlockState branchState = level.getBlockState(pos.relative(Direction.UP));
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
        RegistryHandler.addItem(DynamicTrees.location("staff"), STAFF);
        RegistryHandler.addItem(DynamicTrees.location("dirt_bucket"), DIRT_BUCKET);
        RegistryHandler.addItem(DynamicTrees.location("dendro_potion"), DENDRO_POTION);
    }

    ///////////////////////////////////////////
    // ENTITIES
    ///////////////////////////////////////////

    public static final Supplier<EntityType<FallingTreeEntity>> FALLING_TREE = registerEntity("falling_tree", () -> EntityType.Builder.<FallingTreeEntity>of(FallingTreeEntity::new, MobCategory.MISC)
            .setShouldReceiveVelocityUpdates(true)
            .setTrackingRange(512)
            .setUpdateInterval(Integer.MAX_VALUE)
            .setCustomClientFactory((spawnEntity, level) -> new FallingTreeEntity(level)));
    public static final Supplier<EntityType<LingeringEffectorEntity>> LINGERING_EFFECTOR = registerEntity("lingering_effector", () -> EntityType.Builder.<LingeringEffectorEntity>of(LingeringEffectorEntity::new, MobCategory.MISC)
            .setCustomClientFactory((spawnEntity, level) ->
                    new LingeringEffectorEntity(level, BlockPos.containing(spawnEntity.getPosX(), spawnEntity.getPosY(), spawnEntity.getPosZ()), null)));

    private static <T extends Entity> Supplier<EntityType<T>> registerEntity(String name, Supplier<EntityType.Builder<T>> builderSupplier) {
        return ENTITY_TYPES.register(name, () -> builderSupplier.get().build(name));
    }

    ///////////////////////////////////////////
    // TILE ENTITIES
    ///////////////////////////////////////////

    public static BlockEntityType<SpeciesBlockEntity> SPECIES_BLOCK_ENTITY;
    public static BlockEntityType<PottedSaplingBlockEntity> POTTED_SAPLING_BLOCK_ENTITY;

    public static void setupTileEntities() {
        RootyBlock[] rootyBlocks = SoilProperties.REGISTRY.getAll().stream()
                .map(SoilProperties::getBlock)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .distinct()
                .toArray(RootyBlock[]::new);

        SPECIES_BLOCK_ENTITY = BlockEntityType.Builder.of(SpeciesBlockEntity::new, rootyBlocks).build(null);
        POTTED_SAPLING_BLOCK_ENTITY = BlockEntityType.Builder.of(PottedSaplingBlockEntity::new, POTTED_SAPLING.get()).build(null);
    }

    @SubscribeEvent
    public static void onTileEntitiesRegistry(final RegisterEvent tileEntityRegistryEvent) {
        tileEntityRegistryEvent.register(ForgeRegistries.Keys.BLOCK_ENTITY_TYPES, registerHelper -> {
            setupTileEntities();
            registerHelper.register(PottedSaplingBlock.REG_NAME, POTTED_SAPLING_BLOCK_ENTITY);
            registerHelper.register(DynamicTrees.location("tile_entity_species"), SPECIES_BLOCK_ENTITY);
        });
    }

    ///////////////////////////////////////////
    // WORLD GEN
    ///////////////////////////////////////////

    public static final RegistryObject<PlacementModifierType<CaveRootedTreePlacement>> CAVE_ROOTED_TREE_PLACEMENT_MODIFIER_TYPE = PLACEMENT_MODIFIER_TYPES.register("cave_rooted_tree",
            () -> () -> CaveRootedTreePlacement.CODEC);

    public static final DynamicTreeFeature DYNAMIC_TREE_FEATURE = Registry.register(
            BuiltInRegistries.FEATURE, new ResourceLocation(DynamicTrees.MOD_ID, "tree"), new DynamicTreeFeature());
    public static final CaveRootedTreeFeature CAVE_ROOTED_TREE_FEATURE = Registry.register(BuiltInRegistries.FEATURE, new ResourceLocation(DynamicTrees.MOD_ID, "cave_rooted_tree"), new CaveRootedTreeFeature());

    public static final RegistryObject<Codec<AddDynamicTreesBiomeModifier>> ADD_DYNAMIC_TREES_BIOME_MODIFIER = BIOME_MODIFIER_SERIALIZERS.register("add_dynamic_trees",
            () -> Codec.unit(AddDynamicTreesBiomeModifier::new));
    public static final RegistryObject<Codec<RunFeatureCancellersBiomeModifier>> RUN_FEATURE_CANCELLERS_BIOME_MODIFIER = BIOME_MODIFIER_SERIALIZERS.register("run_feature_cancellers",
            () -> Codec.unit(RunFeatureCancellersBiomeModifier::new));
    public static final RegistryObject<HolderSetType> INCLUDES_EXCLUDES_HOLDER_SET_TYPE = HOLDER_SET_TYPES.register("includes_excludes", () -> IncludesExcludesHolderSet::codec);
    public static final RegistryObject<HolderSetType> NAME_REGEX_MATCH_HOLDER_SET_TYPE = HOLDER_SET_TYPES.register("name_regex_match", () -> NameRegexMatchHolderSet::codec);
    public static final RegistryObject<HolderSetType> TAGS_REGEX_MATCH_HOLDER_SET_TYPE = HOLDER_SET_TYPES.register("tags_regex_match", () -> NameRegexMatchHolderSet::codec);

    public static final RegistryObject<BlockStateProviderType<DTReplaceNyliumFungiBlockStateProvider>> REPLACE_NYLIUM_FUNGI_BLOCK_STATE_PROVIDER_TYPE = BLOCK_STATE_PROVIDER_TYPES.register(
        "replace_nylium_fungi", () -> new BlockStateProviderType<>(DTReplaceNyliumFungiBlockStateProvider.CODEC));

    public static final RegistryObject<StructurePoolElementType<DTCancelVanillaTreePoolElement>> CANCEL_VANILLA_VILLAGE_TREE_STRUCTURE_POOL_ELEMENT_TYPE = STRUCTURE_POOL_ELEMENT_TYPES.register(
            "cancel_vanilla_village_tree_element", () -> () -> DTCancelVanillaTreePoolElement.CODEC);
    public static final RegistryObject<StructurePoolElementType<TreePoolElement>> TREE_STRUCTURE_POOL_ELEMENT_TYPE = STRUCTURE_POOL_ELEMENT_TYPES.register(
            "tree_pool_element", () -> () -> TreePoolElement.CODEC);

    public static final FeatureCanceller TREE_CANCELLER = new TreeFeatureCanceller<>(DynamicTrees.location("tree"), TreeConfiguration.class);

    public static final FeatureCanceller ROOTED_TREE_CANCELLER = new TreeFeatureCanceller<>(DynamicTrees.location("rooted_tree"), RootSystemConfiguration.class);

    public static final FeatureCanceller FUNGUS_CANCELLER = new FungusFeatureCanceller<>(DynamicTrees.location("fungus"), HugeFungusConfiguration.class);

    public static final FeatureCanceller MUSHROOM_CANCELLER = new MushroomFeatureCanceller<>(DynamicTrees.location("mushroom"), HugeMushroomFeatureConfiguration.class);

    @SubscribeEvent
    public static void onFeatureCancellerRegistry(final com.ferreusveritas.dynamictrees.api.registry.RegistryEvent<FeatureCanceller> event) {
        event.getRegistry().registerAll(TREE_CANCELLER, ROOTED_TREE_CANCELLER, FUNGUS_CANCELLER, MUSHROOM_CANCELLER);
    }

    ///////////////////////////////////////////
    // CUSTOM TREE LOGIC
    ///////////////////////////////////////////

    @SubscribeEvent
    public static void onCellKitRegistry(final com.ferreusveritas.dynamictrees.api.registry.RegistryEvent<CellKit> event) {
        CellKits.register(event.getRegistry());
    }

    @SubscribeEvent
    public static void onGrowthLogicKitRegistry(final com.ferreusveritas.dynamictrees.api.registry.RegistryEvent<GrowthLogicKit> event) {
        GrowthLogicKits.register(event.getRegistry());
    }

    @SubscribeEvent
    public static void onGenFeatureRegistry(final com.ferreusveritas.dynamictrees.api.registry.RegistryEvent<GenFeature> event) {
        GenFeatures.register(event.getRegistry());
    }

    ///////////////////////////////////////////
    // SOUNDS
    ///////////////////////////////////////////

    public static final RegistryObject<SoundEvent> FALLING_TREE_HIT_WATER = registerSoundEvent("falling_tree_hit_water");
    public static final RegistryObject<SoundEvent> FALLING_TREE_BIG_START = registerSoundEvent("falling_tree_big_start");
    public static final RegistryObject<SoundEvent> FALLING_TREE_BIG_END = registerSoundEvent("falling_tree_big_end");
    public static final RegistryObject<SoundEvent> FALLING_TREE_MEDIUM_START = registerSoundEvent("falling_tree_medium_start");
    public static final RegistryObject<SoundEvent> FALLING_TREE_MEDIUM_END = registerSoundEvent("falling_tree_medium_end");
    public static final RegistryObject<SoundEvent> FALLING_TREE_SMALL_HIT_WATER = registerSoundEvent("falling_tree_small_hit_water");
    public static final RegistryObject<SoundEvent> FALLING_TREE_SMALL_END = registerSoundEvent("falling_tree_small_end");
    public static final RegistryObject<SoundEvent> FALLING_TREE_SMALL_END_BARE = registerSoundEvent("falling_tree_small_end_bare");
    public static final RegistryObject<SoundEvent> FALLING_TREE_FUNGUS_START = registerSoundEvent("falling_tree_fungus_start");
    public static final RegistryObject<SoundEvent> FALLING_TREE_FUNGUS_END = registerSoundEvent("falling_tree_fungus_end");
    public static final RegistryObject<SoundEvent> FALLING_TREE_FUNGUS_SMALL_END = registerSoundEvent("falling_tree_fungus_small_end");

    private static RegistryObject<SoundEvent> registerSoundEvent(String name) {
        return SOUND_EVENTS.register(name, () -> SoundEvent.createVariableRangeEvent(DynamicTrees.location(name)));
    }
}
