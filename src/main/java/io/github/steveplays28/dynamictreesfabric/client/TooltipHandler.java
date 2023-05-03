package io.github.steveplays28.dynamictreesfabric.client;

import io.github.steveplays28.dynamictreesfabric.compat.seasons.SeasonHelper;
import io.github.steveplays28.dynamictreesfabric.items.Seed;
import io.github.steveplays28.dynamictreesfabric.trees.Species;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;

import java.util.List;

public class TooltipHandler {

    public static void setupTooltips(ItemTooltipEvent event) {
        ItemStack stack = event.getItemStack();
        Item item = stack.getItem();

        if (!(item instanceof Seed)) {
            return;
        }

        Seed seed = (Seed) item;

        PlayerEntity player = event.getEntity();
        if (player == null || player.world == null || SeasonHelper.getSeasonValue(player.world, BlockPos.ORIGIN) == null) {
            return;
        }

        Species species = seed.getSpecies();
        if (species == null || !species.isValid()) {
            return;
        }

        int flags = seed.getSpecies().getSeasonalTooltipFlags(player.world);
        applySeasonalTooltips(event.getToolTip(), flags);
    }

    public static void applySeasonalTooltips(List<Text> tipList, int flags) {
        if (flags != 0) {
            tipList.add(Text.translatable("desc.sereneseasons.fertile_seasons").append(":"));

            if ((flags & 15) == 15) {
                tipList.add(Text.literal(" ").append(Text.translatable("desc.sereneseasons.year_round").formatted(Formatting.LIGHT_PURPLE)));
            } else {
                if ((flags & 1) != 0) {
                    tipList.add(Text.literal(" ").append(Text.translatable("desc.sereneseasons.spring").formatted(Formatting.GREEN)));
                }
                if ((flags & 2) != 0) {
                    tipList.add(Text.literal(" ").append(Text.translatable("desc.sereneseasons.summer").formatted(Formatting.YELLOW)));
                }
                if ((flags & 4) != 0) {
                    tipList.add(Text.literal(" ").append(Text.translatable("desc.sereneseasons.autumn").formatted(Formatting.GOLD)));
                }
                if ((flags & 8) != 0) {
                    tipList.add(Text.literal(" ").append(Text.translatable("desc.sereneseasons.winter").formatted(Formatting.AQUA)));
                }
            }
        }
    }

}
