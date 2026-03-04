package dev.loat.mcp_vision;

import dev.loat.mcp_vision.color.BlockColors;
import dev.loat.mcp_vision.command.CommandManager;
import dev.loat.mcp_vision.util.BuiltinEntityModels;
import dev.loat.mcp_vision.util.RPHelper;
import eu.pb4.polymer.resourcepack.api.PolymerResourcePackUtils;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;

public class MCPVision implements ModInitializer {

    @Override
    public void onInitialize() {
        BuiltinEntityModels.initModels();
        BlockColors.init();

        CommandManager.register();

        PolymerResourcePackUtils.RESOURCE_PACK_AFTER_INITIAL_CREATION_EVENT.register(resourcePackBuilder -> RPHelper.resourcePackBuilder = resourcePackBuilder);

        ServerLifecycleEvents.SERVER_STARTED.register(MCPVisionServer::start);
        ServerLifecycleEvents.SERVER_STOPPING.register(server -> MCPVisionServer.stop());
    }
}
