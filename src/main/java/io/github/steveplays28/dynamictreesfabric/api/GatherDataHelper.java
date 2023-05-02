package io.github.steveplays28.dynamictreesfabric.api;

import io.github.steveplays28.dynamictreesfabric.api.registry.Registry;
import io.github.steveplays28.dynamictreesfabric.data.provider.DTBlockStateProvider;
import io.github.steveplays28.dynamictreesfabric.data.provider.DTBlockTagsProvider;
import io.github.steveplays28.dynamictreesfabric.data.provider.DTItemModelProvider;
import io.github.steveplays28.dynamictreesfabric.data.provider.DTItemTagsProvider;
import net.minecraft.data.DataGenerator;
import net.minecraftforge.data.event.GatherDataEvent;

import java.util.Arrays;

/**
 * @author Harley O'Connor
 */
public final class GatherDataHelper {

    public static void gatherAllData(final String modId, final GatherDataEvent event, Registry<?>... registries) {
        gatherTagData(modId, event);
        gatherBlockStateAndModelData(modId, event, registries);
        gatherItemModelData(modId, event, registries);
    }

    public static void gatherTagData(final String modId, final GatherDataEvent event) {
        final DataGenerator generator = event.getGenerator();
        final boolean includeServer = event.includeServer();

        final DTBlockTagsProvider blockTagsProvider = new DTBlockTagsProvider(generator, modId, event.getExistingFileHelper());
        final DTItemTagsProvider itemTagsProvider = new DTItemTagsProvider(generator, modId, blockTagsProvider, event.getExistingFileHelper());

        generator.addProvider(includeServer, blockTagsProvider);
        generator.addProvider(includeServer, itemTagsProvider);
    }

    public static void gatherBlockStateAndModelData(final String modId, final GatherDataEvent event, Registry<?>... registries) {
        event.getGenerator().addProvider(event.includeClient(), new DTBlockStateProvider(event.getGenerator(), modId,
                event.getExistingFileHelper(), Arrays.asList(registries)));
    }

    public static void gatherItemModelData(final String modId, final GatherDataEvent event, Registry<?>... registries) {
        event.getGenerator().addProvider(event.includeClient(), new DTItemModelProvider(event.getGenerator(), modId,
                event.getExistingFileHelper(), Arrays.asList(registries)));
    }

}
