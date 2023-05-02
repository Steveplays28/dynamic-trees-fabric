package io.github.steveplays28.dynamictreesfabric.event.handlers;

import io.github.steveplays28.dynamictreesfabric.command.DTCommand;
import io.github.steveplays28.dynamictreesfabric.compat.seasons.SeasonHelper;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class ServerEventHandler {

    @SubscribeEvent
    public void onServerStart(final ServerStartingEvent event) {
        SeasonHelper.getSeasonManager().flushMappings();
    }

    @SubscribeEvent
    public void registerCommands(final RegisterCommandsEvent event) {
        new DTCommand().registerDTCommand(event.getDispatcher());
    }

}
