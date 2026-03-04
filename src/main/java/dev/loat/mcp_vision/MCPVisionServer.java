package dev.loat.mcp_vision;

import com.mojang.logging.LogUtils;

import dev.loat.mcp_vision.render.renderer.BufferedImageRenderer;

import io.modelcontextprotocol.server.McpServer;
import io.modelcontextprotocol.server.McpServerFeatures;
import io.modelcontextprotocol.server.McpSyncServer;
import io.modelcontextprotocol.server.transport.HttpServletSseServerTransportProvider;
import io.modelcontextprotocol.spec.McpSchema;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.Vec3;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.util.Base64;
import java.util.List;
import java.util.Map;

/**
 * Embedded MCP server using the official MCP Java SDK.
 *
 * Uses HttpServletSseServerTransportProvider (no Spring required) hosted
 * inside a lightweight embedded Jetty server.
 *
 * Exposes a single tool: "take_screenshot"
 */
public class MCPVisionServer {

    private static final org.slf4j.Logger LOGGER = LogUtils.getLogger();

    // Real signature from source: new McpSchema.JsonSchema(type, properties, required, additionalProperties, defs, definitions)
    private static final McpSchema.JsonSchema INPUT_SCHEMA = new McpSchema.JsonSchema(
        "object",
        Map.of(
            "x",      Map.of("type", "number",  "description", "X coordinate of the camera position"),
            "y",      Map.of("type", "number",  "description", "Y coordinate of the camera position"),
            "z",      Map.of("type", "number",  "description", "Z coordinate of the camera position"),
            "yaw",    Map.of("type", "number",  "description", "Horizontal rotation in degrees"),
            "pitch",  Map.of("type", "number",  "description", "Vertical rotation in degrees"),
            "width",  Map.of("type", "integer", "description", "Image width in pixels"),
            "height", Map.of("type", "integer", "description", "Image height in pixels"),
            "fov",    Map.of("type", "integer", "description", "Field of view in degrees")
        ),
        List.of("x", "y", "z", "yaw", "pitch", "width", "height", "fov"),
        false,
        null,
        null
    );

    private static McpSyncServer mcpSyncServer;
    private static Server jettyServer;

    public static void start(MinecraftServer minecraftServer) {
        int port = ModConfig.getInstance().mcpServerPort;

        var transport = HttpServletSseServerTransportProvider.builder()
            .sseEndpoint("/sse")
            .messageEndpoint("/mcp/message")
            .build();

        // Real Tool constructor from Baeldung source: (name, description, "title", inputSchema, null, null, null)
        // OR 3-arg: (name, description, inputSchema) — depends on version.
        // For 1.0.0 we use 3-arg with JsonSchema:
        mcpSyncServer = McpServer.sync(transport)
            .serverInfo("mcp-vision", "0.1.0")
            .capabilities(McpSchema.ServerCapabilities.builder()
                .tools(true)
                .build())
            .build();

        mcpSyncServer.addTool(new McpServerFeatures.SyncToolSpecification(
            new McpSchema.Tool(
                "take_screenshot",
                "Take Screenshot",
                "Renders a screenshot of the Minecraft world from a given position and orientation. " +
                "Returns the image as a base64-encoded PNG.",
                INPUT_SCHEMA,
                null,
                null,
                null
            ),
            (exchange, req) -> renderScreenshot(minecraftServer, req.arguments())
        ));

        var context = new ServletContextHandler();
        context.setContextPath("/");
        context.addServlet(new ServletHolder(transport), "/*");

        jettyServer = new Server(port);
        jettyServer.setHandler(context);

        try {
            jettyServer.start();
            LOGGER.info("[MCP-Vision] MCP server started on http://localhost:{}/sse", port);
        } catch (Exception e) {
            LOGGER.error("[MCP-Vision] Failed to start embedded Jetty: {}", e.getMessage(), e);
        }
    }

    public static void stop() {
        if (mcpSyncServer != null) mcpSyncServer.close();
        if (jettyServer != null) {
            try {
                jettyServer.stop();
                LOGGER.info("[MCP-Vision] MCP server stopped.");
            } catch (Exception e) {
                LOGGER.error("[MCP-Vision] Failed to stop embedded Jetty: {}", e.getMessage(), e);
            }
        }
    }

    // Handler signature: (exchange, Map<String,Object>) — confirmed by Baeldung source
    private static McpSchema.CallToolResult renderScreenshot(MinecraftServer minecraftServer, Map<String, Object> args) {
        try {
            double x      = toDouble(args.get("x"));
            double y      = toDouble(args.get("y"));
            double z      = toDouble(args.get("z"));
            float  yaw    = (float) toDouble(args.get("yaw"));
            float  pitch  = (float) toDouble(args.get("pitch"));
            int    width  = toInt(args.get("width"));
            int    height = toInt(args.get("height"));
            int    fov    = toInt(args.get("fov"));

            ServerLevel level = minecraftServer.overworld();
            BufferedImageRenderer renderer = new BufferedImageRenderer(
                level, new Vec3(x, y, z), yaw, pitch, width, height, fov,
                ModConfig.getInstance().renderDistance
            );

            BufferedImage image = renderer.render();

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(image, "PNG", baos);
            String base64 = Base64.getEncoder().encodeToString(baos.toByteArray());

            LOGGER.info("[MCP-Vision] Screenshot rendered: {}x{} @ ({}, {}, {}) yaw={} pitch={} fov={}",
                width, height, x, y, z, yaw, pitch, fov);

            // ImageContent constructor from source: (annotations, meta, data, mimeType)
            McpSchema.ImageContent imageContent = new McpSchema.ImageContent(null, base64, "image/png");

            // CallToolResult is a record: (List<Content> content, Boolean isError)
            // ImageContent implements Content — cast needed for List.of() type inference
            return new McpSchema.CallToolResult(
                List.of(imageContent),
                false, null, null
            );

        } catch (Exception e) {
            LOGGER.error("[MCP-Vision] Screenshot failed: {}", e.getMessage(), e);
            return new McpSchema.CallToolResult(
                List.of(new McpSchema.TextContent("Error: " + e.getMessage())),
                false, null, null
            );
        }
    }

    private static double toDouble(Object v) {
        if (v instanceof Number n) return n.doubleValue();
        return Double.parseDouble(v.toString());
    }

    private static int toInt(Object v) {
        if (v instanceof Number n) return n.intValue();
        return Integer.parseInt(v.toString());
    }
}
