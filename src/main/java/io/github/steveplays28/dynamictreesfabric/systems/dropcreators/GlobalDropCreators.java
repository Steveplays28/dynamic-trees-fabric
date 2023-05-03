package io.github.steveplays28.dynamictreesfabric.systems.dropcreators;

import io.github.steveplays28.dynamictreesfabric.systems.dropcreators.context.DropContext;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import java.util.List;
import java.util.Map;
import net.minecraft.util.Identifier;

/**
 * @author Harley O'Connor
 */
public final class GlobalDropCreators {

    private GlobalDropCreators() {}

    private static final Map<Identifier, DropCreatorConfiguration> ENTRIES = Maps.newHashMap();

    public static List<DropCreatorConfiguration> getAll() {
        return Lists.newLinkedList(ENTRIES.values());
    }

    public static <C extends DropContext> void appendAll(final DropCreator.Type<C> type, final C context) {
        getAll().forEach(configuration -> configuration.appendDrops(type, context));
    }

    public static DropCreatorConfiguration get(final Identifier registryName) {
        return ENTRIES.get(registryName);
    }

    public static void put(final Identifier registryName, final DropCreatorConfiguration configuration) {
        ENTRIES.put(registryName, configuration);
    }

}
