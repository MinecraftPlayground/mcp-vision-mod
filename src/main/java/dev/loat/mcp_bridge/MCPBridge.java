package dev.loat.mcp_bridge;

import java.util.List;
import java.util.Map;

import dev.loat.server.MCPServer;
import io.modelcontextprotocol.spec.McpSchema;
import io.modelcontextprotocol.spec.McpSchema.CallToolResult;
import net.fabricmc.api.ModInitializer;

public class MCPBridge implements ModInitializer {

    @Override
    public void onInitialize() {
        MCPServer.start();

        MCPServer.addTool(
            "add",                          // Tool name
            "Adds two numbers.",            // Description
            new McpSchema.JsonSchema(       // Input schema
                "object",
                Map.of(
                    "a", Map.of("type", "number", "description", "First number"),
                    "b", Map.of("type", "number", "description", "Second number")
                ),
                List.of("a", "b"),          // Required fields
                null, null, null
            ),
            (args) -> {                     // Handler lambda
                double a = ((Number) args.get("a")).doubleValue();
                double b = ((Number) args.get("b")).doubleValue();
                return CallToolResult.builder()
                    .content(List.of(new McpSchema.TextContent(String.valueOf(a + b))))
                    .build();
            }
        );
    }
}
