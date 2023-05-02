package io.github.steveplays28.dynamictreesfabric.data.provider;

import io.github.steveplays28.dynamictreesfabric.data.DTItemTags;
import io.github.steveplays28.dynamictreesfabric.trees.Family;
import io.github.steveplays28.dynamictreesfabric.trees.Species;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.tags.BlockTagsProvider;
import net.minecraft.data.tags.ItemTagsProvider;
import net.minecraft.tags.ItemTags;
import net.minecraftforge.common.data.ExistingFileHelper;

import javax.annotation.Nullable;

/**
 * @author Harley O'Connor
 */
public class DTItemTagsProvider extends ItemTagsProvider {

    public DTItemTagsProvider(DataGenerator dataGenerator, String modId, BlockTagsProvider blockTagsProvider, @Nullable ExistingFileHelper existingFileHelper) {
        super(dataGenerator, blockTagsProvider, modId, existingFileHelper);
    }

    @Override
    protected void addTags() {
        if (this.modId.equals(io.github.steveplays28.dynamictreesfabric.DynamicTreesFabric.MOD_ID)) {
            this.addDTOnlyTags();
        }
        this.addDTTags();
    }

    private void addDTOnlyTags() {
        this.tag(DTItemTags.BRANCHES)
                .addTag(DTItemTags.BRANCHES_THAT_BURN)
                .addTag(DTItemTags.FUNGUS_BRANCHES);

        this.tag(DTItemTags.SEEDS)
                .addTag(DTItemTags.FUNGUS_CAPS);

        this.tag(ItemTags.SAPLINGS)
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
