package dev.loat.server;

import java.util.Map;

import io.modelcontextprotocol.spec.McpSchema.CallToolResult;

@FunctionalInterface
public interface ToolHandler {
    CallToolResult handle(Map<String, Object> args);
}
