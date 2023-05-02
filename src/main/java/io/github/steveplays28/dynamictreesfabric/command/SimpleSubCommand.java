package io.github.steveplays28.dynamictreesfabric.command;

import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;

/**
 * An extension of {@link SubCommand} for simple commands (in this case, a command is considered "simple" if it does not
 * take any arguments).
 *
 * @author Harley O'Connor
 */
public abstract class SimpleSubCommand extends SubCommand {

    @Override
    public ArgumentBuilder<CommandSourceStack, ?> register() {
        return Commands.literal(this.getName()).requires(commandSource -> commandSource.hasPermission(this.getPermissionLevel()))
                .executes(context -> executesSuccess(() -> this.execute(context)));
    }

    /**
     * This will be called when the command is executed. Should be implemented to perform the command's logic.
     *
     * @param context The {@link CommandContext<CommandSource>} for the executed command.
     */
    protected abstract void execute(final CommandContext<CommandSourceStack> context);

    /**
     * Default implementation returns {@code 0}, since commands which take no arguments are likely to be printing
     * non-sensitive data which needn't require permissions.
     *
     * @return A permission level of {@code 0}.
     */
    @Override
    protected int getPermissionLevel() {
        return 0;
    }

    @Override
    @SuppressWarnings("all") // This is never used so we just return null.
    public ArgumentBuilder<CommandSourceStack, ?> registerArgument() {
        return null;
    }

}
