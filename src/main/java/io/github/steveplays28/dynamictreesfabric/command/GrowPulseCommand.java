package io.github.steveplays28.dynamictreesfabric.command;

import io.github.steveplays28.dynamictreesfabric.api.TreeHelper;
import io.github.steveplays28.dynamictreesfabric.util.CommandHelper;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import java.util.Collection;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.minecraft.command.CommandSource;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;

public final class GrowPulseCommand extends SubCommand {

    @Override
    protected String getName() {
        return CommandConstants.GROW_PULSE;
    }

    @Override
    protected int getPermissionLevel() {
        return 2;
    }

    private static final String NUMBER = "number";
    private static final Collection<String> NUMBER_SUGGESTIONS = Stream.of(1, 4, 8, 16, 32, 64).map(String::valueOf).collect(Collectors.toList());

    @Override
    public ArgumentBuilder<ServerCommandSource, ?> registerArgument() {
        return blockPosArgument().executes(context -> executesSuccess(() -> this.sendGrowPulse(context.getSource(), rootPosArgument(context), 1)))
                .then(CommandManager.argument(NUMBER, IntegerArgumentType.integer(1)).suggests(((context, builder) -> CommandSource.suggestMatching(NUMBER_SUGGESTIONS, builder)))
                        .executes(context -> executesSuccess(() -> this.sendGrowPulse(context.getSource(), rootPosArgument(context), intArgument(context, NUMBER)))));
    }

    private void sendGrowPulse(final ServerCommandSource source, final BlockPos rootPos, final int number) {
        for (int i = 0; i < number; i++) {
            TreeHelper.growPulse(source.getWorld(), rootPos);
        }

        sendSuccessAndLog(source, Text.translatable("commands.dynamictrees.success.grow_pulse",
                CommandHelper.colour(String.valueOf(number), Formatting.AQUA),
                CommandHelper.posComponent(rootPos, Formatting.AQUA)));
    }

}
