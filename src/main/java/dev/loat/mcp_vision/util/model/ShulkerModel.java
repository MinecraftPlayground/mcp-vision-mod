package dev.loat.mcp_vision.util.model;

import net.minecraft.util.Mth;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.ShulkerBoxBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.joml.Vector3f;

import dev.loat.mcp_vision.render.model.resource.RPElement;
import dev.loat.mcp_vision.render.model.resource.RPModel;
import dev.loat.mcp_vision.util.RPHelper;

import java.util.Map;
import java.util.Optional;

public class ShulkerModel {
    public static RPModel.View get(BlockState blockState, Optional<DyeColor> dyeColor) {
        RPModel rpModel = RPHelper.loadModel(ChestModel.class.getResourceAsStream("/builtin/shulker.json"));

        dyeColor.ifPresentOrElse(
                color -> rpModel.textures.put("0", "minecraft:entity/shulker/shulker_" + color.getName()),
                () -> rpModel.textures.put("0", "minecraft:entity/shulker/shulker"));

        for (RPElement element : rpModel.elements) {
            for (Map.Entry<String, RPElement.TextureInfo> entry : element.faces.entrySet()) {
                entry.getValue().uv.mul(4.f);
            }
        }

        var rot = blockState.getValue(ShulkerBoxBlock.FACING).getRotation();
        return new RPModel.View(rpModel, rot.getEulerAnglesZXY(new Vector3f()).mul(-Mth.RAD_TO_DEG));
    }
}
