package io.github.steveplays28.dynamictreesfabric.command;

import io.github.steveplays28.dynamictreesfabric.init.DTRegistries;
import io.github.steveplays28.dynamictreesfabric.items.Staff;
import io.github.steveplays28.dynamictreesfabric.trees.Species;
import io.github.steveplays28.dynamictreesfabric.util.CommandHelper;
import io.github.steveplays28.dynamictreesfabric.util.ItemUtils;
import io.github.steveplays28.dynamictreesfabric.worldgen.JoCode;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.minecraft.command.CommandSource;
import net.minecraft.item.ItemStack;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;

import static io.github.steveplays28.dynamictreesfabric.command.CommandConstants.DEFAULT_JO_CODE;
import static io.github.steveplays28.dynamictreesfabric.command.CommandConstants.JO_CODE;

public final class CreateStaffCommand extends SubCommand {

    @Override
    protected String getName() {
        return CommandConstants.CREATE_STAFF;
    }

    @Override
    protected int getPermissionLevel() {
        return 2;
    }

    private static final String COLOR = "color";
    private static final String READ_ONLY = "readOnly";
    private static final String MAX_USES = "maxUses";

    private static final int DEFAULT_COLOUR = 0x00FFFF;
    private static final boolean DEFAULT_READ_ONLY = false;
    private static final int DEFAULT_MAX_USES = 64;

    @Override
    public ArgumentBuilder<ServerCommandSource, ?> registerArgument() {
        return blockPosArgument()
                .then(speciesArgument().executes(context -> this.spawnStaff(context.getSource(), blockPosArgument(context), speciesArgument(context),
                                DEFAULT_JO_CODE, DEFAULT_COLOUR, DEFAULT_READ_ONLY, DEFAULT_MAX_USES))
                        .then(stringArgument(JO_CODE).suggests(((context, builder) -> CommandSource.suggestMatching(speciesArgument(context).getJoCodes()
                                        .stream().map(JoCode::toString).collect(Collectors.toList()), builder)))
                                .executes(context -> this.spawnStaff(context.getSource(), blockPosArgument(context), speciesArgument(context),
                                        stringArgument(context, JO_CODE), DEFAULT_COLOUR, DEFAULT_READ_ONLY, DEFAULT_MAX_USES))
                                .then(CommandManager.argument(COLOR, HexColorArgument.hex()).executes(context -> this.spawnStaff(context.getSource(), blockPosArgument(context),
                                                speciesArgument(context), stringArgument(context, JO_CODE), HexColorArgument.getHexCode(context, COLOR), DEFAULT_READ_ONLY,
                                                DEFAULT_MAX_USES))
                                        .then(CommandManager.argument(READ_ONLY, BoolArgumentType.bool()).executes(context -> this.spawnStaff(context.getSource(), blockPosArgument(context),
                                                        speciesArgument(context), stringArgument(context, JO_CODE), HexColorArgument.getHexCode(context, COLOR),
                                                        BoolArgumentType.getBool(context, READ_ONLY), DEFAULT_MAX_USES))
                                                .then(intArgument(MAX_USES).suggests(((context, builder) -> CommandSource.suggestMatching(Stream.of(1, 3, 32, 64, 128).map(String::valueOf).collect(Collectors.toList()), builder)))
                                                        .executes(context -> this.spawnStaff(context.getSource(), blockPosArgument(context), speciesArgument(context),
                                                                stringArgument(context, JO_CODE), HexColorArgument.getHexCode(context, COLOR), BoolArgumentType.getBool(context, READ_ONLY),
                                                                intArgument(context, MAX_USES))))))));
    }

    private int spawnStaff(final ServerCommandSource source, final BlockPos pos, final Species species, final String code, final int colour, final boolean readOnly, final int maxUses) {
        final Staff staff = DTRegistries.STAFF.get();

        final ItemStack wandStack = new ItemStack(staff, 1);

        staff.setSpecies(wandStack, species)
                .setCode(wandStack, code)
                .setColor(wandStack, colour)
                .setReadOnly(wandStack, readOnly)
                .setMaxUses(wandStack, maxUses)
                .setUses(wandStack, maxUses);

        ItemUtils.spawnItemStack(source.getWorld(), pos, wandStack, true);

        sendSuccessAndLog(source, Text.translatable("commands.dynamictrees.success.create_staff", species.getTextComponent(),
                new JoCode(code).getTextComponent(), aqua(String.format("#%08X", colour)), aqua(readOnly), aqua(maxUses), CommandHelper.posComponent(pos, Formatting.AQUA)));

        return 1;
    }

}
