package dev.loat.mcp_vision;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import dev.loat.mcp_vision.json.CachedIdentifierDeserializer;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.resources.Identifier;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

public class ModConfig {
    private static final Path CONFIG_FILE_PATH = FabricLoader.getInstance().getConfigDir().resolve("mcp_vision_config.json");
    private static ModConfig instance;

    private static final Gson gson = new GsonBuilder()
            .registerTypeHierarchyAdapter(Identifier.class, new CachedIdentifierDeserializer())
            .setPrettyPrinting()
            .create();

    public int renderDistance = 128;

    public boolean showSystemMessages = false;

    public boolean renderEntities = false;
    public int renderEntitiesAmount = 20;

    public boolean fullbright = false;
    public int fov = 70;
    public int biomeBlend = 1;

    public int commandPermissionLevel = 2;
    public int mcpServerPort = 25566;

    public static ModConfig getInstance() {
        if (instance == null) {
            if (!load()) // only save if file wasn't just created
                save(); // save since newer versions may contain new options, also removes old options
        }
        return instance;
    }
    public static boolean load() {
        if (!CONFIG_FILE_PATH.toFile().exists()) {
            instance = new ModConfig();
            try {
                if (CONFIG_FILE_PATH.toFile().createNewFile()) {
                    try (FileOutputStream stream = new FileOutputStream(CONFIG_FILE_PATH.toFile())) {
                        stream.write(gson.toJson(instance).getBytes(StandardCharsets.UTF_8));
                    }
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            return true;
        }

        try {
            ModConfig.instance = gson.fromJson(new FileReader(ModConfig.CONFIG_FILE_PATH.toFile()), ModConfig.class);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }

        return false;
    }

    private static void save() {
        
        try (FileOutputStream stream = new FileOutputStream(CONFIG_FILE_PATH.toFile())) {
            stream.write(gson.toJson(instance).getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
