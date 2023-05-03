package io.github.steveplays28.dynamictreesfabric.data.provider;

import io.github.steveplays28.dynamictreesfabric.data.DTItemTags;
import io.github.steveplays28.dynamictreesfabric.trees.Family;
import io.github.steveplays28.dynamictreesfabric.trees.Species;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.server.tag.vanilla.VanillaBlockTagProvider;
import net.minecraft.data.server.tag.vanilla.VanillaItemTagProvider;
import net.minecraft.registry.tag.ItemTags;
import net.minecraftforge.common.data.ExistingFileHelper;

import javax.annotation.Nullable;

/**
 * @author Harley O'Connor
 */
public class DTItemTagsProvider extends VanillaItemTagProvider {

    public DTItemTagsProvider(DataGenerator dataGenerator, String modId, VanillaBlockTagProvider blockTagsProvider, @Nullable ExistingFileHelper existingFileHelper) {
        super(dataGenerator, blockTagsProvider, modId, existingFileHelper);
    }

    @Override
    protected void configure() {
        if (this.modId.equals(io.github.steveplays28.dynamictreesfabric.DynamicTreesFabric.MOD_ID)) {
            this.addDTOnlyTags();
        }
        this.addDTTags();
    }

    private void addDTOnlyTags() {
        this.getOrCreateTagBuilder(DTItemTags.BRANCHES)
                .addTag(DTItemTags.BRANCHES_THAT_BURN)
                .addTag(DTItemTags.FUNGUS_BRANCHES);

        this.getOrCreateTagBuilder(DTItemTags.SEEDS)
                .addTag(DTItemTags.FUNGUS_CAPS);

        this.getOrCreateTagBuilder(ItemTags.SAPLINGS)
                .addTag(DTItemTags.SEEDS);
    }

    protected void addDTTags() {
        Family.REGISTRY.dataGenerationStream(this.modId).forEach(family -> {
            family.getBranchItem().ifPresent(item ->
                    family.defaultBranchItemTags().forEach(tag -> this.tag(tag).add(item))
            );
        });

        Species.REGISTRY.dataGenerationStream(this.modId).forEach(species -> {
            // Some species return the common seed, so only return if the species has its own seed.
            if (!species.hasSeed()) {
                return;
            }
            // Create seed item tag.
            species.getSeed().ifPresent(seed ->
                    species.defaultSeedTags().forEach(tag ->
                            this.tag(tag).add(seed)));
        });
    }

    @Override
    public String getName() {
        return modId + " DT Block Tags";
    }

}
