package io.github.steveplays28.dynamictreesfabric.event.handlers;

import io.github.steveplays28.dynamictreesfabric.client.TooltipHandler;
import io.github.steveplays28.dynamictreesfabric.compat.seasons.SeasonHelper;
import io.github.steveplays28.dynamictreesfabric.event.FutureBreak;
import io.github.steveplays28.dynamictreesfabric.init.DTClient;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.LogicalSide;

public class CommonEventHandler {

	@SubscribeEvent
	public void onWorldTick(TickEvent.LevelTickEvent event) {
		if (event.side == LogicalSide.SERVER) {
			FutureBreak.process(event.level);
		}

		if (event.type == TickEvent.Type.LEVEL && event.phase == TickEvent.Phase.START) {
			SeasonHelper.updateTick(event.level, event.level.getDayTime());
		}
	}

	@SubscribeEvent
	public void onWorldLoad(LevelEvent.Load event) {
		if (event.getLevel().isClientSide()) {
			DTClient.discoverWoodColors();
		}
	}

	@SubscribeEvent
	import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
	public void onItemTooltipAdded(ItemTooltipEvent event) {
		TooltipHandler.setupTooltips(event);
	}

}
