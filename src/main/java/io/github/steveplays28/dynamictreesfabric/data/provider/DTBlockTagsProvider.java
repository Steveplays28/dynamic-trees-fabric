package io.github.steveplays28.dynamictreesfabric.data.provider;

import java.util.Optional;

import org.jetbrains.annotations.Nullable;

import io.github.steveplays28.dynamictreesfabric.blocks.leaves.LeavesProperties;
import io.github.steveplays28.dynamictreesfabric.data.DTBlockTags;
import io.github.steveplays28.dynamictreesfabric.init.DTRegistries;
import io.github.steveplays28.dynamictreesfabric.init.DTTrees;
import io.github.steveplays28.dynamictreesfabric.trees.Family;
import io.github.steveplays28.dynamictreesfabric.trees.Species;
import net.minecraftforge.common.data.ExistingFileHelper;

import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.server.tag.TagProvider;
import net.minecraft.data.server.tag.vanilla.VanillaBlockTagProvider;
import net.minecraft.item.ToolMaterial;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.registry.tag.TagKey;

/**
 * @author Harley O'Connor
 */
public class DTBlockTagsProvider extends VanillaBlockTagProvider {

	public DTBlockTagsProvider(DataGenerator dataGenerator, String modId, @Nullable ExistingFileHelper existingFileHelper) {
		super(dataGenerator, modId, existingFileHelper);
	}

	@Override
	protected void configure() {
		if (this.modId.equals(io.github.steveplays28.dynamictreesfabric.DynamicTreesFabric.MOD_ID)) {
			this.addDTOnlyTags();
		}
		this.addDTTags();
	}

	private void addDTOnlyTags() {
		this.getOrCreateTagBuilder(DTBlockTags.BRANCHES)
				.addTag(DTBlockTags.BRANCHES_THAT_BURN)
				.addTag(DTBlockTags.FUNGUS_BRANCHES);

		this.getOrCreateTagBuilder(DTBlockTags.FOLIAGE)
				.add(Blocks.GRASS)
				.add(Blocks.TALL_GRASS)
				.add(Blocks.FERN);

		this.getOrCreateTagBuilder(DTBlockTags.STRIPPED_BRANCHES)
				.addTag(DTBlockTags.STRIPPED_BRANCHES_THAT_BURN)
				.addTag(DTBlockTags.STRIPPED_FUNGUS_BRANCHES);

		this.getOrCreateTagBuilder(BlockTags.ENDERMAN_HOLDABLE)
				.addTag(DTBlockTags.FUNGUS_CAPS);

		this.getOrCreateTagBuilder(BlockTags.FLOWER_POTS)
				.add(DTRegistries.POTTED_SAPLING.get());

		Species.REGISTRY.get(DTTrees.WARPED).getSapling().ifPresent(sapling ->
				this.getOrCreateTagBuilder(BlockTags.HOGLIN_REPELLENTS).add(sapling));

		this.getOrCreateTagBuilder(BlockTags.LEAVES)
				.addTag(DTBlockTags.LEAVES);

		this.getOrCreateTagBuilder(BlockTags.LOGS)
				.addTag(DTBlockTags.BRANCHES);

		this.getOrCreateTagBuilder(BlockTags.LOGS_THAT_BURN)
				.addTag(DTBlockTags.BRANCHES_THAT_BURN)
				.addTag(DTBlockTags.STRIPPED_BRANCHES_THAT_BURN);

		this.getOrCreateTagBuilder(BlockTags.SAPLINGS)
				.addTag(DTBlockTags.SAPLINGS);

		this.getOrCreateTagBuilder(BlockTags.WART_BLOCKS)
				.addTag(DTBlockTags.WART_BLOCKS);
	}

	protected void addDTTags() {
		LeavesProperties.REGISTRY.dataGenerationStream(this.modId).forEach(leavesProperties -> {
			// Create dynamic leaves block tag.
			leavesProperties.getDynamicLeavesBlock().ifPresent(leaves ->
					leavesProperties.defaultLeavesTags().forEach(tag ->
							this.tag(tag).add(leaves))
			);
		});

		Family.REGISTRY.dataGenerationStream(this.modId).forEach(family -> {
			// Create branch tag and harvest tag if a branch exists.
			family.getBranch().ifPresent(branch -> {
				this.tierTag(family.getDefaultBranchHarvestTier()).ifPresent(tagBuilder -> tagBuilder.add(branch));
				family.defaultBranchTags().forEach(tag ->
						this.tag(tag).add(branch));
			});

			// Create stripped branch tag and harvest tag if the family has a stripped branch.
			family.getStrippedBranch().ifPresent(strippedBranch -> {
				this.tierTag(family.getDefaultStrippedBranchHarvestTier()).ifPresent(tagBuilder -> tagBuilder.add(strippedBranch));
				family.defaultStrippedBranchTags().forEach(tag ->
						this.tag(tag).add(strippedBranch));
			});
		});

		Species.REGISTRY.dataGenerationStream(this.modId).forEach(species -> {
			// Create dynamic sapling block tags.
			species.getSapling().ifPresent(sapling ->
					species.defaultSaplingTags().forEach(tag ->
							this.tag(tag).add(sapling)));
		});
	}

	protected Optional<TagProvider.ProvidedTagBuilder<Block>> tierTag(@Nullable ToolMaterial tier) {
		if (tier == null)
			return Optional.empty();

		TagKey<Block> tag = tier.getTag();

		return tag == null ? Optional.empty() : Optional.of(this.getOrCreateTagBuilder(tag));
	}

	@Override
	public String getName() {
		return modId + " DT Block Tags";
	}
}
