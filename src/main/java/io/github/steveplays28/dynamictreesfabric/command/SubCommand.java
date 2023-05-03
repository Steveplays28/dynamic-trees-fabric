package io.github.steveplays28.dynamictreesfabric.command;

import io.github.steveplays28.dynamictreesfabric.api.TreeHelper;
import io.github.steveplays28.dynamictreesfabric.api.TreeRegistry;
import io.github.steveplays28.dynamictreesfabric.trees.Species;
import io.github.steveplays28.dynamictreesfabric.util.CommandHelper;
import io.github.steveplays28.dynamictreesfabric.util.ThrowableRunnable;
import com.google.common.collect.Lists;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;
import net.minecraft.command.CommandSource;
import net.minecraft.command.argument.BlockPosArgumentType;
import net.minecraft.command.argument.IdentifierArgumentType;
import net.minecraft.command.argument.PosArgument;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;

/**
 * @author Harley O'Connor
 */
public abstract class SubCommand {

    protected static final DynamicCommandExceptionType NO_TREE_FOUND = new DynamicCommandExceptionType(pos -> Text.translatable("commands.dynamictrees.error.get_tree", Text.translatable("chat.coordinates", getVector3i(pos).getX(), getVector3i(pos).getY(), getVector3i(pos).getZ()).styled(style -> style.withColor(Formatting.DARK_RED))));
    protected static final DynamicCommandExceptionType SPECIES_UNKNOWN = new DynamicCommandExceptionType(resLocStr -> Text.translatable("commands.dynamictrees.error.unknown_species", darkRed(resLocStr)));
    protected static final DynamicCommandExceptionType SPECIES_NOT_TRANSFORMABLE = new DynamicCommandExceptionType(nonTransformableSpecies -> Text.translatable("commands.dynamictrees.error.not_transformable", darkRed(nonTransformableSpecies)));

    private static Vec3i getVector3i(final Object vecObj) {
        if (vecObj instanceof Vec3i) {
            return ((Vec3i) vecObj);
        }
        return Vec3i.ZERO;
    }

    /**
     * Returns the name of the command.
     *
     * @return - Name of command.
     */
    protected abstract String getName();

    /**
     * Returns the permission level required to use the command.
     *
     * @return Permission level required.
     */
    protected abstract int getPermissionLevel();

    public ArgumentBuilder<ServerCommandSource, ?> register() {
        final LiteralArgumentBuilder<ServerCommandSource> argumentBuilder = CommandManager.literal(this.getName())
                .requires(commandSource -> commandSource.hasPermissionLevel(this.getPermissionLevel()));

        this.registerArguments().forEach(argumentBuilder::then);
        return argumentBuilder;
    }

    protected List<ArgumentBuilder<ServerCommandSource, ?>> registerArguments() {
        return Lists.newArrayList(this.registerArgument());
    }

    /**
     * Registers the arguments for this implementation of {@link SubCommand}.
     *
     * @return The {@link ArgumentBuilder} created.
     */
    public abstract ArgumentBuilder<ServerCommandSource, ?> registerArgument();

    protected static int executesSuccess(final ThrowableRunnable<CommandSyntaxException> executeRunnable) throws CommandSyntaxException {
        executeRunnable.run();
        return 1;
    }

    protected static int executesSuccess(final CommandContext<ServerCommandSource> context, final Consumer<CommandContext<ServerCommandSource>> executeConsumer) {
        executeConsumer.accept(context);
        return 1;
    }

    protected static RequiredArgumentBuilder<ServerCommandSource, Boolean> booleanArgument(final String name) {
        return CommandManager.argument(name, BoolArgumentType.bool());
    }

    protected static boolean booleanArgument(final CommandContext<ServerCommandSource> context, final String name) {
        return BoolArgumentType.getBool(context, name);
    }

    protected static RequiredArgumentBuilder<ServerCommandSource, Integer> intArgument(final String name) {
        return CommandManager.argument(name, IntegerArgumentType.integer());
    }

    protected static int intArgument(final CommandContext<ServerCommandSource> context, final String name) {
        return IntegerArgumentType.getInteger(context, name);
    }

    protected static RequiredArgumentBuilder<ServerCommandSource, String> stringArgument(final String name) {
        return CommandManager.argument(name, StringArgumentType.string());
    }

    protected static RequiredArgumentBuilder<ServerCommandSource, String> stringArgument(final String name, final Collection<String> suggestions) {
        return CommandManager.argument(name, StringArgumentType.string()).suggests(((context, builder) -> CommandSource.suggestMatching(suggestions, builder)));
    }

    protected static String stringArgument(final CommandContext<ServerCommandSource> context, final String name) {
        return StringArgumentType.getString(context, name);
    }

    protected static RequiredArgumentBuilder<ServerCommandSource, PosArgument> blockPosArgument() {
        return CommandManager.argument(CommandConstants.LOCATION, BlockPosArgumentType.blockPos());
    }

    protected static BlockPos blockPosArgument(final CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        return BlockPosArgumentType.getLoadedBlockPos(context, CommandConstants.LOCATION);
    }

    protected static BlockPos rootPosArgument(final CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        final BlockPos pos = blockPosArgument(context);
        final BlockPos rootPos = TreeHelper.findRootNode(context.getSource().getWorld(), pos);

        if (rootPos == BlockPos.ORIGIN) {
            throw NO_TREE_FOUND.create(pos);
        }

        return rootPos;
    }

    protected static RequiredArgumentBuilder<ServerCommandSource, Identifier> speciesArgument() {
        return resourceLocationArgument(CommandConstants.SPECIES, Species.REGISTRY::getRegistryNames);
    }

    protected static RequiredArgumentBuilder<ServerCommandSource, Identifier> transformableSpeciesArgument() {
        return resourceLocationArgument(CommandConstants.SPECIES, TreeRegistry::getTransformableSpeciesLocations);
    }

    protected static Species speciesArgument(final CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        final Identifier registryName = IdentifierArgumentType.getIdentifier(context, CommandConstants.SPECIES);
        final Species species = TreeRegistry.findSpecies(registryName);

        if (!species.isValid()) {
            throw SPECIES_UNKNOWN.create(registryName.toString());
        }

        return species;
    }

    protected static RequiredArgumentBuilder<ServerCommandSource, Identifier> resourceLocationArgument(final String name, final Supplier<Collection<Identifier>> suggestionsSupplier) {
        return CommandManager.argument(name, IdentifierArgumentType.identifier())
                .suggests((context, builder) -> CommandSource.suggestIdentifiers(suggestionsSupplier.get(), builder));
    }

    protected static Text aqua(final Object object) {
        return CommandHelper.colour(object, Formatting.AQUA);
    }

    protected static Text darkRed(final Object object) {
        return CommandHelper.colour(object, Formatting.DARK_RED);
    }

    protected static void sendSuccess(final ServerCommandSource source, final Text component) {
        source.sendFeedback(component.copy().styled(style -> style.withColor(Formatting.GREEN)),
                false);
    }

    protected static void sendSuccessAndLog(final ServerCommandSource source, final Text component) {
        source.sendFeedback(component.copy().styled(style -> style.withColor(Formatting.GREEN)),
                true);
    }

    protected static void sendFailure(final ServerCommandSource source, final Text component) {
        source.sendError(component.copy().styled(style -> style.withColor(Formatting.RED)));
    }

}
