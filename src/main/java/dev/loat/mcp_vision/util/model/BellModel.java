package dev.loat.mcp_vision.util.model;

import org.joml.Vector3f;

import dev.loat.mcp_vision.render.model.resource.RPElement;
import dev.loat.mcp_vision.render.model.resource.RPModel;
import dev.loat.mcp_vision.util.RPHelper;

import java.util.Map;

public class BellModel {
    public static RPModel.View get() {
        RPModel rpModel = RPHelper.loadModel(BellModel.class.getResourceAsStream("/builtin/bell.json"));
        for (RPElement element : rpModel.elements) {
            for (Map.Entry<String, RPElement.TextureInfo> entry : element.faces.entrySet()) {
                entry.getValue().uv.mul(2);
            }
        }

        return new RPModel.View(rpModel, new Vector3f());
    }
}
