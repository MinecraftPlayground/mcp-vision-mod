package dev.loat.mcp_vision.util.model;

import net.minecraft.world.phys.Vec3;

import java.util.Map;

import dev.loat.mcp_vision.render.model.resource.RPElement;
import dev.loat.mcp_vision.render.model.resource.RPModel;
import dev.loat.mcp_vision.util.RPHelper;

public class DecoratedPotModel {
    public static RPModel.View get() {
        RPModel rpModel = RPHelper.loadModel(ChestModel.class.getResourceAsStream("/builtin/decorated_pot.json"));
        for (RPElement element : rpModel.elements) {
            for (Map.Entry<String, RPElement.TextureInfo> entry : element.faces.entrySet()) {
                if (element.name == null || !(element.name.equals("left") || element.name.equals("right") || element.name.equals("back") || element.name.equals("front")))
                    entry.getValue().uv.mul(2.f);
            }
        }

        return new RPModel.View(rpModel, Vec3.ZERO.toVector3f());
    }
}
