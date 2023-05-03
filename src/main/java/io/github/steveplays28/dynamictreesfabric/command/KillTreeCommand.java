package io.github.steveplays28.dynamictreesfabric.command;

import io.github.steveplays28.dynamictreesfabric.api.TreeHelper;
import io.github.steveplays28.dynamictreesfabric.util.CommandHelper;
import com.mojang.brigadier.builder.ArgumentBuilder;
import java.util.Objects;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public final class KillTreeCommand extends SubCommand {

    @Override
    protected String getName() {
        return CommandConstants.KILL_TREE;
    }

    @Override
    protected int getPermissionLevel() {
        return 2;
    }

    @Override
    public ArgumentBuilder<ServerCommandSource, ?> registerArgument() {
        return blockPosArgument().executes(context -> executesSuccess(() -> this.killTree(context.getSource(), rootPosArgument(context))));
    }

    private void killTree(final ServerCommandSource source, final BlockPos rootPos) {
        final World world = source.getWorld();

        Objects.requireNonNull(TreeHelper.getRooty(world.getBlockState(rootPos))).destroyTree(world, rootPos);
        sendSuccessAndLog(source, Text.translatable("commands.dynamictrees.success.kill_tree",
                CommandHelper.posComponent(rootPos, Formatting.AQUA)));
    }

}
