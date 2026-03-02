package dev.loat.mcp_vision.command;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.logging.LogUtils;

import dev.loat.mcp_vision.ModConfig;
import dev.loat.mcp_vision.render.renderer.BufferedImageRenderer;
import dev.loat.mcp_vision.util.RPHelper;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;

public class CommandManager {
    public static void register() {
        CommandRegistrationCallback.EVENT.register((
            dispatcher,
            registryAccess,
            environment
        ) -> dispatcher.register(
            Commands.literal("mcp-vision")
                .then(Commands.literal("save-entity")
                    .then(Commands.argument("source", EntityArgument.entity())
                        .executes(x -> createImageFromSource(x, 128, 128))
                        .then(Commands.argument("width", IntegerArgumentType.integer(128))
                            .then(Commands.argument("height", IntegerArgumentType.integer(128))
                            .executes(x -> createImageFromSource(x, IntegerArgumentType.getInteger(x, "width"), IntegerArgumentType.getInteger(x, "height")))
                        ))
                    )
                )
                .then(Commands.literal("clear-cache")
                    .executes(x -> {
                        RPHelper.clearCache();
                        //Raytracer.clearCache(); // not sure if enabling this is beneficial
                        return 0;
                    }))

        ));
    }

    private static int createImageFromSource(CommandContext<CommandSourceStack> x, int width, int height) throws CommandSyntaxException {
        var source = EntityArgument.getEntity(x, "source");
        if (source instanceof LivingEntity livingEntity)
            CommandManager.createImageAsync(x, livingEntity, width, height);
        return 0;
    }


    private static void createImageAsync(CommandContext<CommandSourceStack> context, LivingEntity entity, int width, int height) {
        CommandSourceStack source = context.getSource();

        if (ModConfig.getInstance().showSystemMessages)
            source.sendSuccess(() -> Component.literal("Taking screenshot..."), false);

        var renderer = new BufferedImageRenderer(entity, width, height, ModConfig.getInstance().renderDistance);

        long startTime = System.nanoTime();

        CompletableFuture.supplyAsync(renderer::render).thenAcceptAsync(mapImage -> finalizeImage(mapImage, startTime, source), source.getServer());
    }

    private static void finalizeImage(BufferedImage mapImage, long startTime, CommandSourceStack source) {
        var rendersDir = FabricLoader.getInstance().getGameDir().resolve("renders").toAbsolutePath();
        var f = rendersDir.toFile();
        if (!f.exists()) {
            if (!f.mkdir())
                return;
        }
        
        String date = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.ENGLISH).format(new Date());
        var file = rendersDir.resolve(date + ".png").toFile();

        try {
            ImageIO.write(mapImage, "PNG", file);

            if (ModConfig.getInstance().showSystemMessages) {
                long durationInMillis = (System.nanoTime() - startTime) / 1000000;
                long millis = durationInMillis % 1000;
                long secs = (durationInMillis / 1000);
                String time = secs > 0 ? String.format("%ds %dms", secs, millis) : String.format("%dms", millis);
                source.sendSuccess(() -> Component.literal("Done! ("+time+")"), false);
            }
        } catch (IOException e) {
            LogUtils.getLogger().error("Could not write image to " + file.getPath());
        }
    }
}
