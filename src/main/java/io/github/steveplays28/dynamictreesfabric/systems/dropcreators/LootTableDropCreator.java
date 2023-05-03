package io.github.steveplays28.dynamictreesfabric.systems.dropcreators;

import io.github.steveplays28.dynamictreesfabric.api.configurations.ConfigurationProperty;
import io.github.steveplays28.dynamictreesfabric.data.DTLootParameterSets;
import io.github.steveplays28.dynamictreesfabric.data.DTLootParameters;
import io.github.steveplays28.dynamictreesfabric.systems.dropcreators.context.DropContext;
import io.github.steveplays28.dynamictreesfabric.systems.dropcreators.context.LogDropContext;
import net.minecraft.loot.LootTable;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.context.LootContextParameters;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;

/**
 * @author Harley O'Connor
 */
public final class LootTableDropCreator extends DropCreator {

    private static final ConfigurationProperty<Identifier> HARVEST_TABLE = ConfigurationProperty.property("harvest_table", Identifier.class);
    private static final ConfigurationProperty<Identifier> VOLUNTARY_TABLE = ConfigurationProperty.property("voluntary_table", Identifier.class);
    private static final ConfigurationProperty<Identifier> LEAVES_TABLE = ConfigurationProperty.property("leaves_table", Identifier.class);
    private static final ConfigurationProperty<Identifier> LOGS_TABLE = ConfigurationProperty.property("logs_table", Identifier.class);

    public LootTableDropCreator(Identifier registryName) {
        super(registryName);
    }

    @Override
    protected void registerProperties() {
        this.register(HARVEST_TABLE, VOLUNTARY_TABLE, LEAVES_TABLE, LOGS_TABLE);
    }

    @Override
    protected DropCreatorConfiguration createDefaultConfiguration() {
        return super.createDefaultConfiguration()
                .with(HARVEST_TABLE, LootTable.EMPTY.getLootTableId())
                .with(VOLUNTARY_TABLE, LootTable.EMPTY.getLootTableId())
                .with(LEAVES_TABLE, LootTable.EMPTY.getLootTableId())
                .with(LOGS_TABLE, LootTable.EMPTY.getLootTableId());
    }

    @Override
    public void appendHarvestDrops(DropCreatorConfiguration configuration, DropContext context) {
        context.drops().addAll(((ServerWorld) context.world()).getServer().getLootManager().getTable(configuration.get(HARVEST_TABLE))
                .generateLoot(new LootContext.Builder((ServerWorld) context.world())
                        .parameter(LootContextParameters.BLOCK_STATE, context.world().getBlockState(context.pos()))
                        .parameter(DTLootParameters.SPECIES, context.species())
                        .parameter(DTLootParameters.FERTILITY, context.fertility())
                        .parameter(DTLootParameters.FORTUNE, context.fortune())
                        .build(DTLootParameterSets.HARVEST)
                )
        );
    }

    @Override
    public void appendVoluntaryDrops(DropCreatorConfiguration configuration, DropContext context) {
        context.drops().addAll(((ServerWorld) context.world()).getServer().getLootManager().getTable(configuration.get(VOLUNTARY_TABLE))
                .generateLoot(new LootContext.Builder((ServerWorld) context.world())
                        .parameter(LootContextParameters.BLOCK_STATE, context.world().getBlockState(context.pos()))
                        .parameter(DTLootParameters.SPECIES, context.species())
                        .parameter(DTLootParameters.FERTILITY, context.fertility())
                        .build(DTLootParameterSets.VOLUNTARY)
                )
        );
    }

    @Override
    public void appendLeavesDrops(DropCreatorConfiguration configuration, DropContext context) {
        context.drops().addAll(((ServerWorld) context.world()).getServer().getLootManager().getTable(configuration.get(LEAVES_TABLE))
                .generateLoot(new LootContext.Builder((ServerWorld) context.world())
                        .parameter(LootContextParameters.BLOCK_STATE, context.world().getBlockState(context.pos()))
                        .parameter(LootContextParameters.TOOL, context.tool())
                        .parameter(DTLootParameters.SPECIES, context.species())
                        .parameter(DTLootParameters.FORTUNE, context.fortune())
                        .build(DTLootParameterSets.LEAVES)
                )
        );
    }

    @Override
    public void appendLogDrops(DropCreatorConfiguration configuration, LogDropContext context) {
        context.drops().addAll(((ServerWorld) context.world()).getServer().getLootManager().getTable(configuration.get(LOGS_TABLE))
                .generateLoot(new LootContext.Builder((ServerWorld) context.world())
                        .parameter(LootContextParameters.BLOCK_STATE, context.world().getBlockState(context.pos()))
                        .parameter(LootContextParameters.TOOL, context.tool())
                        .parameter(DTLootParameters.SPECIES, context.species())
                        .parameter(DTLootParameters.LOGS_AND_STICKS, context.species().getLogsAndSticks(context.volume()))
                        .build(DTLootParameterSets.LOGS)
                )
        );
    }

}
