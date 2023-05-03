package io.github.steveplays28.dynamictreesfabric.util;

import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.Vec3i;

/**
 * @author Harley O'Connor
 */
public final class CommandHelper {

    public static Text posComponent(final Vec3i pos) {
        return Text.translatable("chat.coordinates", pos.getX(), pos.getY(), pos.getZ());
    }

    public static Text posComponent(final Vec3i pos, final Formatting colour) {
        return posComponent(pos).copy().styled(style -> style.withColor(colour));
    }

    public static Text colour(final Object text, final Formatting colour) {
        return text instanceof Text ? ((Text) text).copy().styled(style -> style.withColor(colour)) :
                Text.literal(String.valueOf(text)).styled(style -> style.withColor(colour));
    }

}
