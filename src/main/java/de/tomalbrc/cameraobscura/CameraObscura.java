package de.tomalbrc.cameraobscura;

import de.tomalbrc.cameraobscura.color.BlockColors;
import de.tomalbrc.cameraobscura.command.CameraCommand;
import de.tomalbrc.cameraobscura.util.BuiltinEntityModels;
import de.tomalbrc.cameraobscura.util.RPHelper;
import eu.pb4.polymer.resourcepack.api.PolymerResourcePackUtils;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;

public class CameraObscura implements ModInitializer {

    @Override
    public void onInitialize() {
        BuiltinEntityModels.initModels();
        BlockColors.init();

        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> CameraCommand.register(dispatcher));

        PolymerResourcePackUtils.RESOURCE_PACK_AFTER_INITIAL_CREATION_EVENT.register(resourcePackBuilder -> RPHelper.resourcePackBuilder = resourcePackBuilder);
    }
}
