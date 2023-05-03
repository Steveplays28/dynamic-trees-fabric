package io.github.steveplays28.dynamictreesfabric.data;

import java.util.function.Consumer;
import net.minecraft.loot.context.LootContextParameters;
import net.minecraft.loot.context.LootContextType;
import net.minecraft.loot.context.LootContextTypes;

/**
 * @author Harley O'Connor
 */
public final class DTLootParameterSets {

    public static final LootContextType HARVEST = register("harvest", builder ->
            builder.require(LootContextParameters.BLOCK_STATE)
                    .require(DTLootParameters.SPECIES)
                    .require(DTLootParameters.FERTILITY)
                    .require(DTLootParameters.FORTUNE)
    );

    public static final LootContextType VOLUNTARY = register("voluntary", builder ->
            builder.require(LootContextParameters.BLOCK_STATE)
                    .require(DTLootParameters.SPECIES)
                    .require(DTLootParameters.FERTILITY)
    );

    public static final LootContextType LEAVES = register("leaves", builder ->
            builder.require(LootContextParameters.BLOCK_STATE)
                    .require(LootContextParameters.TOOL)
                    .require(DTLootParameters.SPECIES)
                    .require(DTLootParameters.FORTUNE)
    );

    public static final LootContextType LOGS = register("logs", builder ->
            builder.require(LootContextParameters.BLOCK_STATE)
                    .require(LootContextParameters.TOOL)
                    .require(DTLootParameters.SPECIES)
                    .require(DTLootParameters.LOGS_AND_STICKS)
    );

    private static LootContextType register(String path, Consumer<LootContextType.Builder> builderConsumer) {
        final LootContextType.Builder builder = new LootContextType.Builder();
        builderConsumer.accept(builder);

        final LootContextType paramSet = builder.build();
        LootContextTypes.MAP.put(io.github.steveplays28.dynamictreesfabric.DynamicTreesFabric.resLoc(path), paramSet);

        return paramSet;
    }

}
