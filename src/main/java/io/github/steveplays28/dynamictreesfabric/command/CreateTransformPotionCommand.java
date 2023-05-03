package io.github.steveplays28.dynamictreesfabric.command;

import io.github.steveplays28.dynamictreesfabric.init.DTRegistries;
import io.github.steveplays28.dynamictreesfabric.items.DendroPotion;
import io.github.steveplays28.dynamictreesfabric.trees.Species;
import io.github.steveplays28.dynamictreesfabric.util.CommandHelper;
import io.github.steveplays28.dynamictreesfabric.util.ItemUtils;
import net.minecraft.item.ItemStack;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

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
    public ArgumentBuilder<ServerCommandSource, ?> registerArgument() {
        return blockPosArgument().then(transformableSpeciesArgument().executes(context -> this.spawnTransformPotion(context.getSource(),
                blockPosArgument(context), speciesArgument(context))));
    }

    private int spawnTransformPotion(final ServerCommandSource source, final BlockPos pos, final Species species) throws CommandSyntaxException {
        if (!species.isTransformable()) {
            throw SPECIES_NOT_TRANSFORMABLE.create(species.getTextComponent());
        }

        final DendroPotion dendroPotion = DTRegistries.DENDRO_POTION.get();
        final ItemStack dendroPotionStack = new ItemStack(dendroPotion);

        dendroPotion.applyIndexTag(dendroPotionStack, DendroPotion.DendroPotionType.TRANSFORM.getIndex()); // Make it a transform potion.
        dendroPotion.setTargetSpecies(dendroPotionStack, species); // Tell it to set the target tree to the selected family.

        ItemUtils.spawnItemStack(source.getWorld(), pos, dendroPotionStack, true); // Spawn potion in the world.
        sendSuccessAndLog(source, Text.translatable("commands.dynamictrees.success.create_transform_potion",
                species.getTextComponent(), CommandHelper.posComponent(pos, Formatting.AQUA)));

        return 1;
    }

}
