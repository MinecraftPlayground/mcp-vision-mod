package dev.loat.mcp_vision;

import dev.loat.mcp_vision.color.BlockColors;
import dev.loat.mcp_vision.command.MCPVisionCommand;
import dev.loat.mcp_vision.util.BuiltinEntityModels;
import dev.loat.mcp_vision.util.RPHelper;
import eu.pb4.polymer.resourcepack.api.PolymerResourcePackUtils;
import net.fabricmc.api.ModInitializer;

public class MCPVision implements ModInitializer {

    @Override
    public void onInitialize() {
        BuiltinEntityModels.initModels();
        BlockColors.init();

        MCPVisionCommand.register();

        PolymerResourcePackUtils.RESOURCE_PACK_AFTER_INITIAL_CREATION_EVENT.register(resourcePackBuilder -> RPHelper.resourcePackBuilder = resourcePackBuilder);
    }
}
