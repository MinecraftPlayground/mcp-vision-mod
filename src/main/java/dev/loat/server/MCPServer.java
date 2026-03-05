package dev.loat.server;

import io.modelcontextprotocol.json.jackson3.JacksonMcpJsonMapper;
import io.modelcontextprotocol.server.McpAsyncServer;
import io.modelcontextprotocol.server.McpServer;
import io.modelcontextprotocol.server.McpServerFeatures.AsyncToolSpecification;
import io.modelcontextprotocol.server.transport.HttpServletStreamableServerTransportProvider;
import io.modelcontextprotocol.spec.McpSchema;
import io.modelcontextprotocol.spec.McpSchema.ServerCapabilities;
import io.modelcontextprotocol.spec.McpSchema.Tool;

import org.eclipse.jetty.ee10.servlet.ServletContextHandler;
import org.eclipse.jetty.ee10.servlet.ServletHolder;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import reactor.core.publisher.Mono;
import tools.jackson.databind.json.JsonMapper;

/**
 * MCP Async HTTP Server using the official MCP Java SDK 1.0.0.
 *
 * Transport: HttpServletStreamableServerTransportProvider (Streamable HTTP, from the mcp module)
 * Container: Embedded Jetty (minimal, no Spring required)
 *
 * Endpoints:
 *   /mcp             → MCP JSON-RPC (for LLM clients like Claude Desktop)
 *   /mcp/tool/<name>        → Convenience REST-Endpunkt pro Tool zum Testen
 */
public class MCPServer {

    private static final int PORT = 8080;

    private static Server jettyServer;
    private static McpAsyncServer mcpAsyncServer;
    private static ServletContextHandler context;
    private static final JsonMapper jsonMapper = JsonMapper.builder().build();
    private static final JacksonMcpJsonMapper mcpJsonMapper = new JacksonMcpJsonMapper(MCPServer.jsonMapper);

    public static void start() {

        HttpServletStreamableServerTransportProvider transportProvider =
            HttpServletStreamableServerTransportProvider.builder()
                .jsonMapper(MCPServer.mcpJsonMapper)
                .mcpEndpoint("/mcp")
                .build();

        MCPServer.mcpAsyncServer = McpServer.async(transportProvider)
            .serverInfo("mcp-bridge-server", "1.0.0")
            .capabilities(ServerCapabilities.builder()
                .tools(true)
                .build())
            .build();

        MCPServer.jettyServer = new Server();

        ServerConnector connector = new ServerConnector(MCPServer.jettyServer);
        connector.setPort(MCPServer.PORT);
        MCPServer.jettyServer.addConnector(connector);

        MCPServer.context = new ServletContextHandler(ServletContextHandler.NO_SESSIONS);
        MCPServer.context.setContextPath("/");

        // MCP SDK transport servlet (JSON-RPC endpoint)
        MCPServer.context.addServlet(new ServletHolder("mcp-transport", transportProvider), "/mcp/*");

        MCPServer.jettyServer.setHandler(MCPServer.context);
        try {
            MCPServer.jettyServer.start();
        } catch (Exception e) {
            e.printStackTrace();
        }

        System.out.printf("[MCP] Server started on http://localhost:%d%n", MCPServer.PORT);
        System.out.printf("[MCP] MCP JSON-RPC : http://localhost:%d/mcp%n", MCPServer.PORT);
    }

    /**
     * Registers a tool on the MCP server.
     *
     * @param name Name of the Tool
     * @param description Tool description for the LLM client
     * @param schema JSON schema of the expected arguments
     * @param handler Lambda implementing the tool logic
     */
    public static void addTool(String name, String description, McpSchema.JsonSchema schema, ToolHandler handler) {

        AsyncToolSpecification toolSpec = AsyncToolSpecification.builder()
            .tool(Tool.builder()
                .name(name)
                .description(description)
                .inputSchema(schema)
                .build())
            .callHandler((exchange, request) -> Mono.just(handler.handle(request.arguments())))
            .build();

        MCPServer.mcpAsyncServer.addTool(toolSpec);

        // Register convenience REST servlet for direct testing
        MCPServer.context.addServlet(
            new ServletHolder(name + "-tool", new ToolServlet(handler, MCPServer.jsonMapper)),
            "/mcp/tool/" + name
        );

        System.out.printf("[MCP] Tool '%s' registered -> http://localhost:%d/mcp/tool/%s%n", name, MCPServer.PORT, name);
    }

    public static void stop() {
        if (MCPServer.mcpAsyncServer != null) {
            MCPServer.mcpAsyncServer.close();
        }
        if (MCPServer.jettyServer != null) {
            try {
                MCPServer.jettyServer.stop();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
