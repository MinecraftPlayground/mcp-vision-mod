package dev.loat.mcp_vision.util.model;

import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BedPart;
import org.joml.Vector3f;

import dev.loat.mcp_vision.render.model.resource.RPElement;
import dev.loat.mcp_vision.render.model.resource.RPModel;
import dev.loat.mcp_vision.util.RPHelper;

import java.util.Map;
import java.util.Optional;

public class BedModel {

    public static RPModel.View get(BlockState blockState, Optional<DyeColor> color) {
        RPModel rpModel = RPHelper.loadModel(ChestModel.class.getResourceAsStream(blockState.getValue(BedBlock.PART) == BedPart.HEAD ? "/builtin/bed_top.json" : "/builtin/bed_bottom.json"));

        color.ifPresentOrElse(
                dyeColor -> rpModel.textures.put("0", "minecraft:entity/bed/" + dyeColor.getName()),
                () -> rpModel.textures.put("0", "minecraft:entity/bed/red")
        );

        for (RPElement element : rpModel.elements) {
            for (Map.Entry<String, RPElement.TextureInfo> entry : element.faces.entrySet()) {
                entry.getValue().uv.mul(4.f);
            }
        }

        return new RPModel.View(rpModel, new Vector3f(0, (blockState.getValue(BedBlock.FACING).toYRot() + 180) % 360, 0));
    }
}
