package io.github.steveplays28.dynamictreesfabric.command;

import io.github.steveplays28.dynamictreesfabric.api.registry.Registry;
import io.github.steveplays28.dynamictreesfabric.api.registry.RegistryEntry;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;

import static io.github.steveplays28.dynamictreesfabric.command.CommandConstants.RAW;

/**
 * @author Harley O'Connor
 */
public final class RegistrySubCommand<V extends RegistryEntry<V>> extends SubCommand {

    public final Registry<V> registry;

    public RegistrySubCommand(Registry<V> registry) {
        this.registry = registry;
    }

    @Override
    protected String getName() {
        return this.registry.getName().toLowerCase();
    }

    @Override
    protected int getPermissionLevel() {
        return 0;
    }

    @Override
    public ArgumentBuilder<ServerCommandSource, ?> registerArgument() {
        return CommandManager.literal("list")
                .executes(context -> executesSuccess(() -> this.listEntries(context.getSource(), false)))
                .then(booleanArgument(RAW)
                        .executes(context -> executesSuccess(() -> this.listEntries(context.getSource(), BoolArgumentType.getBool(context, RAW))))
                );
    }

    private void listEntries(final ServerCommandSource source, final boolean raw) {
        if (raw) {
            this.registry.getAll().forEach(entry -> source.sendFeedback(Text.literal(entry.getRegistryName().toString()), false));
            return;
        }

        this.registry.getAll().forEach(entry -> source.sendFeedback(Text.literal("- ")
                .append(entry.getTextComponent()).formatted(Formatting.GREEN), false));
    }

}
