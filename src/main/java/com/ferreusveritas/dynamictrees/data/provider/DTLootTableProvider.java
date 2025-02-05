package com.ferreusveritas.dynamictrees.data.provider;

import com.ferreusveritas.dynamictrees.blocks.branches.BranchBlock;
import com.ferreusveritas.dynamictrees.blocks.leaves.LeavesProperties;
import com.ferreusveritas.dynamictrees.loot.DTLootParameterSets;
import com.ferreusveritas.dynamictrees.loot.condition.SeasonalSeedDropChance;
import com.ferreusveritas.dynamictrees.loot.condition.VoluntarySeedDropChance;
import com.ferreusveritas.dynamictrees.loot.entry.SeedItemLootEntry;
import com.ferreusveritas.dynamictrees.loot.function.MultiplyLogsCount;
import com.ferreusveritas.dynamictrees.loot.function.MultiplySticksCount;
import com.ferreusveritas.dynamictrees.systems.fruit.Fruit;
import com.ferreusveritas.dynamictrees.systems.pod.Pod;
import com.ferreusveritas.dynamictrees.trees.Species;
import com.ferreusveritas.dynamictrees.util.ResourceLocationUtils;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.minecraft.advancements.criterion.EnchantmentPredicate;
import net.minecraft.advancements.criterion.ItemPredicate;
import net.minecraft.advancements.criterion.MinMaxBounds;
import net.minecraft.advancements.criterion.StatePropertiesPredicate;
import net.minecraft.block.Block;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DirectoryCache;
import net.minecraft.data.IDataProvider;
import net.minecraft.data.LootTableProvider;
import net.minecraft.data.loot.BlockLootTables;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.loot.ConstantRange;
import net.minecraft.loot.ItemLootEntry;
import net.minecraft.loot.LootParameterSet;
import net.minecraft.loot.LootParameterSets;
import net.minecraft.loot.LootPool;
import net.minecraft.loot.LootTable;
import net.minecraft.loot.LootTableManager;
import net.minecraft.loot.RandomValueRange;
import net.minecraft.loot.conditions.BlockStateProperty;
import net.minecraft.loot.conditions.ILootCondition;
import net.minecraft.loot.conditions.MatchTool;
import net.minecraft.loot.conditions.SurvivesExplosion;
import net.minecraft.loot.conditions.TableBonus;
import net.minecraft.loot.functions.ExplosionDecay;
import net.minecraft.loot.functions.SetCount;
import net.minecraft.resources.ResourcePackType;
import net.minecraft.state.IntegerProperty;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.registries.ForgeRegistries;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Harley O'Connor
 */
public class DTLootTableProvider extends LootTableProvider {

    private static final ILootCondition.IBuilder HAS_SILK_TOUCH = MatchTool.toolMatches(ItemPredicate.Builder.item()
            .hasEnchantment(new EnchantmentPredicate(Enchantments.SILK_TOUCH, MinMaxBounds.IntBound.atLeast(1))));
    private static final ILootCondition.IBuilder HAS_NO_SILK_TOUCH = HAS_SILK_TOUCH.invert();
    private static final ILootCondition.IBuilder HAS_SHEARS =
            MatchTool.toolMatches(ItemPredicate.Builder.item().of(Items.SHEARS));
    private static final ILootCondition.IBuilder HAS_SHEARS_OR_SILK_TOUCH = HAS_SHEARS.or(HAS_SILK_TOUCH);
    private static final ILootCondition.IBuilder HAS_NO_SHEARS_OR_SILK_TOUCH = HAS_SHEARS_OR_SILK_TOUCH.invert();

    private static final Logger LOGGER = LogManager.getLogger();
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

    private final Map<ResourceLocation, LootTable.Builder> lootTables = new HashMap<>();
    private final DataGenerator generator;
    private final String modId;
    private final ExistingFileHelper existingFileHelper;

    public DTLootTableProvider(DataGenerator generator, String modId, ExistingFileHelper existingFileHelper) {
        super(generator);
        this.generator = generator;
        this.modId = modId;
        this.existingFileHelper = existingFileHelper;
    }

    @Override
    public void run(DirectoryCache cache) {
        addTables();
        writeTables(cache);
    }

    private void addTables() {
        Species.REGISTRY.dataGenerationStream(modId).forEach(this::addVoluntaryTable);

        ForgeRegistries.BLOCKS.getValues().stream()
                .filter(block -> block instanceof BranchBlock)
                .map(block -> (BranchBlock) block)
                .filter(block -> block.getRegistryName().getNamespace().equals(modId))
                .forEach(this::addBranchTable);

        LeavesProperties.REGISTRY.dataGenerationStream(modId).forEach(leavesProperties -> {
            addLeavesBlockTable(leavesProperties);
            addLeavesTable(leavesProperties);
        });

        Fruit.REGISTRY.dataGenerationStream(modId).forEach(this::addFruitBlockTable);
        Pod.REGISTRY.dataGenerationStream(modId).forEach(this::addPodBlockTable);
    }

    private void addVoluntaryTable(Species species) {
        if (species.shouldGenerateVoluntaryDrops()) {
            final ResourceLocation leavesTablePath = getFullDropsPath(species.getVoluntaryDropsPath());
            if (!existingFileHelper.exists(leavesTablePath, ResourcePackType.SERVER_DATA)) {
                lootTables.put(leavesTablePath, species.createVoluntaryDrops());
            }
        }
    }

    private void addBranchTable(BranchBlock branchBlock) {
        if (branchBlock.shouldGenerateBranchDrops()) {
            final ResourceLocation branchTablePath = getFullDropsPath(branchBlock.getLootTableName());
            if (!existingFileHelper.exists(branchTablePath, ResourcePackType.SERVER_DATA)) {
                lootTables.put(branchTablePath, branchBlock.createBranchDrops());
            }
        }
    }

    private void addLeavesBlockTable(LeavesProperties leavesProperties) {
        if (leavesProperties.shouldGenerateBlockDrops()) {
            final ResourceLocation leavesBlockTablePath = getFullDropsPath(leavesProperties.getBlockLootTableName());
            if (!existingFileHelper.exists(leavesBlockTablePath, ResourcePackType.SERVER_DATA)) {
                lootTables.put(leavesBlockTablePath, leavesProperties.createBlockDrops());
            }
        }
    }

    private void addLeavesTable(LeavesProperties leavesProperties) {
        if (leavesProperties.shouldGenerateDrops()) {
            final ResourceLocation leavesTablePath = getFullDropsPath(leavesProperties.getLootTableName());
            if (!existingFileHelper.exists(leavesTablePath, ResourcePackType.SERVER_DATA)) {
                lootTables.put(leavesTablePath, leavesProperties.createDrops());
            }
        }
    }

    private void addFruitBlockTable(Fruit fruit) {
        if (fruit.shouldGenerateBlockDrops()) {
            final ResourceLocation fruitBlockTablePath = getFullDropsPath(fruit.getBlockDropsPath());
            if (!existingFileHelper.exists(fruitBlockTablePath, ResourcePackType.SERVER_DATA)) {
                lootTables.put(fruitBlockTablePath, fruit.createBlockDrops());
            }
        }
    }

    private void addPodBlockTable(Pod pod) {
        if (pod.shouldGenerateBlockDrops()) {
            final ResourceLocation fruitBlockTablePath = getFullDropsPath(pod.getBlockDropsPath());
            if (!existingFileHelper.exists(fruitBlockTablePath, ResourcePackType.SERVER_DATA)) {
                lootTables.put(fruitBlockTablePath, pod.createBlockDrops());
            }
        }
    }

    private ResourceLocation getFullDropsPath(ResourceLocation path) {
        return ResourceLocationUtils.surround(path, "loot_tables/", ".json");
    }

    public static LootTable.Builder createLeavesBlockDrops(Block primitiveLeavesBlock, float[] seedChances) {
        return BlockLootTables.createSilkTouchOrShearsDispatchTable(
                primitiveLeavesBlock,
                SeedItemLootEntry.lootTableSeedItem()
                        .when(SurvivesExplosion.survivesExplosion())
                        .when(TableBonus.bonusLevelFlatChance(Enchantments.BLOCK_FORTUNE, seedChances))
                        .when(SeasonalSeedDropChance.seasonalSeedDropChance())
        ).withPool(
                LootPool.lootPool().setRolls(ConstantRange.exactly(1)).when(HAS_NO_SHEARS_OR_SILK_TOUCH)
                        .add(ItemLootEntry.lootTableItem(Items.STICK)
                                .apply(SetCount.setCount(
                                        RandomValueRange.between(1.0F, 2.0F)
                                ))
                                .apply(ExplosionDecay.explosionDecay())
                                .when(TableBonus.bonusLevelFlatChance(Enchantments.BLOCK_FORTUNE, 0.02F,
                                        0.022222223F, 0.025F, 0.033333335F, 0.1F)))
        ).setParamSet(LootParameterSets.BLOCK);
    }

    public static LootTable.Builder createPalmLeavesBlockDrops(Block primitiveLeavesBlock, float[] seedChances) {
        return BlockLootTables.createSilkTouchOrShearsDispatchTable(
                primitiveLeavesBlock,
                SeedItemLootEntry.lootTableSeedItem()
                        .when(SurvivesExplosion.survivesExplosion())
                        .when(TableBonus.bonusLevelFlatChance(Enchantments.BLOCK_FORTUNE, seedChances))
                        .when(SeasonalSeedDropChance.seasonalSeedDropChance())
        ).setParamSet(LootParameterSets.BLOCK);
    }

    public static LootTable.Builder createWartBlockDrops(Block primitiveWartBlock) {
        return LootTable.lootTable().withPool(
                LootPool.lootPool().setRolls(ConstantRange.exactly(1))
                        .add(ItemLootEntry.lootTableItem(primitiveWartBlock))
                        .when(SurvivesExplosion.survivesExplosion())
        );
    }

    public static LootTable.Builder createLeavesDrops(float[] seedChances, LootParameterSet parameterSet) {
        return LootTable.lootTable().withPool(
                LootPool.lootPool().setRolls(ConstantRange.exactly(1)).add(
                        SeedItemLootEntry.lootTableSeedItem()
                                .when(SurvivesExplosion.survivesExplosion())
                                .when(TableBonus.bonusLevelFlatChance(Enchantments.BLOCK_FORTUNE, seedChances))
                                .when(SeasonalSeedDropChance.seasonalSeedDropChance())
                )
        ).withPool(
                LootPool.lootPool().setRolls(ConstantRange.exactly(1)).add(
                        ItemLootEntry.lootTableItem(Items.STICK)
                                .apply(SetCount.setCount(
                                        RandomValueRange.between(1.0F, 2.0F)
                                ))
                                .apply(ExplosionDecay.explosionDecay())
                                .when(TableBonus.bonusLevelFlatChance(
                                        Enchantments.BLOCK_FORTUNE, 0.02F, 0.022222223F, 0.025F, 0.033333335F, 0.1F
                                ))
                )
        ).setParamSet(parameterSet);
    }

    public static LootTable.Builder createPalmLeavesDrops(float[] seedChances, LootParameterSet parameterSet) {
        return LootTable.lootTable().withPool(
                LootPool.lootPool().setRolls(ConstantRange.exactly(1)).add(
                        SeedItemLootEntry.lootTableSeedItem()
                                .when(SurvivesExplosion.survivesExplosion())
                                .when(TableBonus.bonusLevelFlatChance(Enchantments.BLOCK_FORTUNE, seedChances))
                                .when(SeasonalSeedDropChance.seasonalSeedDropChance())
                )
        ).setParamSet(parameterSet);
    }

    public static LootTable.Builder createWartDrops(Block primitiveWartBlock) {
        return LootTable.lootTable().withPool(
                LootPool.lootPool().setRolls(ConstantRange.exactly(1))
                        .add(ItemLootEntry.lootTableItem(primitiveWartBlock))
                        .when(SurvivesExplosion.survivesExplosion())
                        .when(TableBonus.bonusLevelFlatChance(
                                Enchantments.BLOCK_FORTUNE, 0.1F, 0.1333333F, 0.1666666F, 0.2F
                        ))
        );
    }

    public static LootTable.Builder createVoluntaryDrops(Item seedItem) {
        return LootTable.lootTable().withPool(
                LootPool.lootPool().setRolls(ConstantRange.exactly(1)).add(
                        ItemLootEntry.lootTableItem(seedItem)
                                .when(VoluntarySeedDropChance.voluntarySeedDropChance())
                )
        ).setParamSet(DTLootParameterSets.VOLUNTARY);
    }

    public static LootTable.Builder createBranchDrops(Block primitiveLogBlock, Item stickItem) {
        return LootTable.lootTable().withPool(
                LootPool.lootPool().setRolls(ConstantRange.exactly(1)).add(
                        ItemLootEntry.lootTableItem(primitiveLogBlock)
                                .apply(MultiplyLogsCount.multiplyLogsCount())
                                .apply(ExplosionDecay.explosionDecay())
                )
        ).withPool(
                LootPool.lootPool().setRolls(ConstantRange.exactly(1)).add(
                        ItemLootEntry.lootTableItem(stickItem)
                                .apply(MultiplySticksCount.multiplySticksCount())
                                .apply(ExplosionDecay.explosionDecay())
                )
        ).setParamSet(DTLootParameterSets.BRANCHES);
    }

    public static LootTable.Builder createFruitDrops(Block fruitBlock, Item fruitItem, IntegerProperty ageProperty, int matureAge) {
        return LootTable.lootTable().withPool(
                LootPool.lootPool().setRolls(ConstantRange.exactly(1)).add(
                        ItemLootEntry.lootTableItem(fruitItem)
                                .when(
                                        BlockStateProperty.hasBlockStateProperties(fruitBlock)
                                                .setProperties(
                                                        StatePropertiesPredicate.Builder.properties()
                                                                .hasProperty(ageProperty, matureAge)
                                                )
                                )
                )
        ).apply(ExplosionDecay.explosionDecay()).setParamSet(LootParameterSets.BLOCK);
    }

    public static LootTable.Builder createPodDrops(Block podBlock, Item podItem, IntegerProperty ageProperty, int matureAge) {
        return LootTable.lootTable().withPool(
                LootPool.lootPool().setRolls(ConstantRange.exactly(1)).add(
                        ItemLootEntry.lootTableItem(podItem)
                                .apply(
                                        SetCount.setCount(ConstantRange.exactly(3))
                                                .when(
                                                        BlockStateProperty.hasBlockStateProperties(podBlock)
                                                                .setProperties(
                                                                        StatePropertiesPredicate.Builder.properties()
                                                                                .hasProperty(ageProperty, matureAge)
                                                                )
                                                )
                                )
                                .apply(ExplosionDecay.explosionDecay())
                )
        ).setParamSet(LootParameterSets.BLOCK);
    }

    private void writeTables(DirectoryCache cache) {
        Path outputFolder = this.generator.getOutputFolder();
        lootTables.forEach((key, lootTable) -> {
            Path path = outputFolder.resolve("data/" + key.getNamespace() + "/" + key.getPath());
            try {
                IDataProvider.save(GSON, cache, LootTableManager.serialize(lootTable.build()), path);
            } catch (IOException e) {
                LOGGER.error("Couldn't write loot table {}", path, e);
            }
        });
    }

    @Override
    public String getName() {
        return modId;
    }
}
