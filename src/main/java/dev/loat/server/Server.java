package dev.loat.server;

import io.modelcontextprotocol.server.McpAsyncServer;
import io.modelcontextprotocol.server.McpServer;
import io.modelcontextprotocol.server.transport.HttpServletStreamableServerTransportProvider;

public class Server {
    public static void start() {
        HttpServletStreamableServerTransportProvider transportProvider = HttpServletStreamableServerTransportProvider.builder()
            .jsonMapper(jsonMapper)
            .mcpEndpoint("/mcp")
            .build();

        McpAsyncServer asyncServer = McpServer.sync(transportProvider);
    }
}
