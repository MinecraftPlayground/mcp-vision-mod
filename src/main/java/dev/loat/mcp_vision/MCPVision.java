package dev.loat.mcp_vision;

import dev.loat.mcp_vision.color.BlockColors;
import dev.loat.mcp_vision.command.MCPVisionCommand;
import dev.loat.mcp_vision.util.BuiltinEntityModels;
import dev.loat.mcp_vision.util.RPHelper;
import eu.pb4.polymer.resourcepack.api.PolymerResourcePackUtils;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;

public class MCPVision implements ModInitializer {

    @Override
    public void onInitialize() {
        BuiltinEntityModels.initModels();
        BlockColors.init();

        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> MCPVisionCommand.register(dispatcher));

        PolymerResourcePackUtils.RESOURCE_PACK_AFTER_INITIAL_CREATION_EVENT.register(resourcePackBuilder -> RPHelper.resourcePackBuilder = resourcePackBuilder);
    }
}
