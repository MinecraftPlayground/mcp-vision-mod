package dev.loat.mcp_vision.render.model.resource;

import java.util.List;
import java.util.Map;

import dev.loat.mcp_vision.render.model.resource.state.MultipartDefinition;
import dev.loat.mcp_vision.render.model.resource.state.Variant;

public class RPBlockState {
    public Map<String, Variant> variants;
    public List<MultipartDefinition> multipart;
}
