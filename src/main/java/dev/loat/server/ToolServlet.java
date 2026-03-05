package dev.loat.server;

import java.io.IOException;
import java.util.Map;

import io.modelcontextprotocol.spec.McpSchema;
import io.modelcontextprotocol.spec.McpSchema.CallToolResult;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import tools.jackson.databind.json.JsonMapper;

class ToolServlet extends HttpServlet {

    private final ToolHandler handler;
    private final JsonMapper mapper;

    ToolServlet(ToolHandler handler, JsonMapper mapper) {
        this.handler = handler;
        this.mapper = mapper;
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            byte[] body = req.getInputStream().readAllBytes();
            @SuppressWarnings("unchecked")
            Map<String, Object> args = this.mapper.readValue(body, Map.class);

            CallToolResult result = this.handler.handle(args);

            String text = result.content().stream()
                .filter(c -> c instanceof McpSchema.TextContent)
                .map(c -> ((McpSchema.TextContent) c).text())
                .findFirst()
                .orElse("");

            resp.setContentType("application/json");
            resp.setStatus(HttpServletResponse.SC_OK);
            resp.getOutputStream().write(this.mapper.writeValueAsBytes(Map.of("result", text)));

        } catch (Exception e) {
            resp.setContentType("application/json");
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getOutputStream().write(this.mapper.writeValueAsBytes(Map.of("error", e.getMessage())));
        }
    }
}
