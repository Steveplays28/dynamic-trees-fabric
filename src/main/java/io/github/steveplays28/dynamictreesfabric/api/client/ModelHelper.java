package io.github.steveplays28.dynamictreesfabric.api.client;

import net.minecraft.block.Block;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.color.block.BlockColorProvider;
import net.minecraft.client.color.item.ItemColorProvider;
import net.minecraft.item.Item;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(value = Dist.CLIENT)
public class ModelHelper {

    public static void regColorHandler(Block block, BlockColorProvider blockColor) {
        MinecraftClient.getInstance().getBlockColors().registerColorProvider(blockColor, block);
    }

    public static void regColorHandler(Item item, ItemColorProvider itemColor) {
        MinecraftClient.getInstance().getItemColors().register(itemColor, new Item[]{item});
    }

}
