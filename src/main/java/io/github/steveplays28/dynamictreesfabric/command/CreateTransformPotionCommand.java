package io.github.steveplays28.dynamictreesfabric.command;

import io.github.steveplays28.dynamictreesfabric.init.DTRegistries;
import io.github.steveplays28.dynamictreesfabric.items.DendroPotion;
import io.github.steveplays28.dynamictreesfabric.trees.Species;
import io.github.steveplays28.dynamictreesfabric.util.CommandHelper;
import io.github.steveplays28.dynamictreesfabric.util.ItemUtils;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

/**
 * @author Harley O'Connor
 */
public final class CreateTransformPotionCommand extends SubCommand {

    @Override
    protected String getName() {
        return CommandConstants.CREATE_TRANSFORM_POTION;
    }

    @Override
    protected int getPermissionLevel() {
        return 2;
    }

    @Override
    public ArgumentBuilder<CommandSourceStack, ?> registerArgument() {
        return blockPosArgument().then(transformableSpeciesArgument().executes(context -> this.spawnTransformPotion(context.getSource(),
                blockPosArgument(context), speciesArgument(context))));
    }

    private int spawnTransformPotion(final CommandSourceStack source, final BlockPos pos, final Species species) throws CommandSyntaxException {
        if (!species.isTransformable()) {
            throw SPECIES_NOT_TRANSFORMABLE.create(species.getTextComponent());
        }

        final DendroPotion dendroPotion = DTRegistries.DENDRO_POTION.get();
        final ItemStack dendroPotionStack = new ItemStack(dendroPotion);

        dendroPotion.applyIndexTag(dendroPotionStack, DendroPotion.DendroPotionType.TRANSFORM.getIndex()); // Make it a transform potion.
        dendroPotion.setTargetSpecies(dendroPotionStack, species); // Tell it to set the target tree to the selected family.

        ItemUtils.spawnItemStack(source.getLevel(), pos, dendroPotionStack, true); // Spawn potion in the world.
        sendSuccessAndLog(source, Component.translatable("commands.dynamictrees.success.create_transform_potion",
                species.getTextComponent(), CommandHelper.posComponent(pos, ChatFormatting.AQUA)));

        return 1;
    }

}
