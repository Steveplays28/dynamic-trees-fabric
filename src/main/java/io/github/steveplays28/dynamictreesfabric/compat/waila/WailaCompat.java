/*
package io.github.steveplays28.dynamictreesfabric.compat.waila;

import io.github.steveplays28.dynamictreesfabric.blocks.DynamicCocoaBlock;
import io.github.steveplays28.dynamictreesfabric.blocks.branches.BranchBlock;
import io.github.steveplays28.dynamictreesfabric.blocks.branches.TrunkShellBlock;
import io.github.steveplays28.dynamictreesfabric.blocks.rootyblocks.RootyBlock;
import io.github.steveplays28.dynamictreesfabric.blocks.rootyblocks.WaterSoilProperties;
import mcp.mobius.waila.api.IRegistrar;
import mcp.mobius.waila.api.IWailaPlugin;
import mcp.mobius.waila.api.TooltipPosition;
import mcp.mobius.waila.api.WailaPlugin;

@WailaPlugin
public class WailaCompat implements IWailaPlugin {

    @Override
    public void register(IRegistrar registrar) {
        WailaBranchHandler branchHandler = new WailaBranchHandler();
        WailaRootyHandler rootyHandler = new WailaRootyHandler();

        registrar.registerComponentProvider(branchHandler, TooltipPosition.BODY, BranchBlock.class);
        registrar.registerComponentProvider(branchHandler, TooltipPosition.BODY, TrunkShellBlock.class);
        registrar.registerComponentProvider(rootyHandler, TooltipPosition.BODY, RootyBlock.class);
        registrar.registerComponentProvider(new WailaCocoaHandler(), TooltipPosition.BODY, DynamicCocoaBlock.class);
        registrar.registerComponentProvider(new WailaRootyWaterHandler(), TooltipPosition.HEAD, WaterSoilProperties.RootyWaterBlock.class);
    }

}
*/
