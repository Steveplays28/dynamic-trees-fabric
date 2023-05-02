package io.github.steveplays28.dynamictreesfabric.command;

import io.github.steveplays28.dynamictreesfabric.api.TreeHelper;
import io.github.steveplays28.dynamictreesfabric.util.CommandHelper;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Objects;

import static io.github.steveplays28.dynamictreesfabric.command.CommandConstants.FERTILITY_SUGGESTIONS;
import static io.github.steveplays28.dynamictreesfabric.command.CommandConstants.RAW;

public final class FertilityCommand extends SubCommand {

    @Override
    protected String getName() {
        return FERTILITY;
    }

    @Override
    protected int getPermissionLevel() {
        return 0;
    }

    private static final String FERTILITY = CommandConstants.FERTILITY;

    @Override
    public ArgumentBuilder<CommandSourceStack, ?> registerArgument() {
        return blockPosArgument().executes(context -> executesSuccess(() -> this.getFertility(context.getSource(),
                        rootPosArgument(context), false)))
                .then(booleanArgument(RAW).executes(context -> executesSuccess(() -> this.getFertility(context.getSource(),
                        rootPosArgument(context), booleanArgument(context, RAW)))))
                .then(Commands.argument(FERTILITY, IntegerArgumentType.integer(0, 15)).suggests(FERTILITY_SUGGESTIONS)
                        .requires(commandSource -> commandSource.hasPermission(2)) // Setting fertility requires higher permission level.
                        .executes(context -> executesSuccess(() -> this.setFertility(context.getSource(), rootPosArgument(context),
                                intArgument(context, FERTILITY)))));
    }

    private void getFertility(final CommandSourceStack source, final BlockPos rootPos, final boolean raw) {
        final BlockState state = source.getLevel().getBlockState(rootPos);
        final int fertility = Objects.requireNonNull(TreeHelper.getRooty(state)).getFertility(state, source.getLevel(), rootPos);

        if (raw) {
            sendSuccess(source, Component.literal(String.valueOf(fertility)));
            return;
        }

        sendSuccess(source, Component.translatable("commands.dynamictrees.success.get_fertility",
                CommandHelper.posComponent(rootPos, ChatFormatting.AQUA),
                CommandHelper.colour(String.valueOf(fertility), ChatFormatting.AQUA)));
    }

    private void setFertility(final CommandSourceStack source, final BlockPos rootPos, final int fertility) {
        final BlockState state = source.getLevel().getBlockState(rootPos);
        Objects.requireNonNull(TreeHelper.getRooty(state)).setFertility(source.getLevel(), rootPos, fertility);

        sendSuccessAndLog(source, Component.translatable("commands.dynamictrees.success.set_fertility",
                CommandHelper.posComponent(rootPos, ChatFormatting.AQUA),
                CommandHelper.colour(String.valueOf(fertility), ChatFormatting.AQUA)));
    }

}
