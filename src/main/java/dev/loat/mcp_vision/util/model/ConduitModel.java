package dev.loat.mcp_vision.util.model;

import net.minecraft.world.phys.Vec3;

import java.util.Map;

import dev.loat.mcp_vision.render.model.resource.RPElement;
import dev.loat.mcp_vision.render.model.resource.RPModel;
import dev.loat.mcp_vision.util.RPHelper;

public class ConduitModel {
    public static RPModel.View get() {
        RPModel rpModel = RPHelper.loadModel(ChestModel.class.getResourceAsStream("/builtin/conduit.json"));
        for (RPElement element : rpModel.elements) {
            for (Map.Entry<String, RPElement.TextureInfo> entry : element.faces.entrySet()) {
                if (element.name == null || !element.name.equals("eye")) {
                    entry.getValue().uv.mul(2,1,2,1); // might not be correct
                }
            }
        }

        return new RPModel.View(rpModel, Vec3.ZERO.toVector3f());
    }
}
