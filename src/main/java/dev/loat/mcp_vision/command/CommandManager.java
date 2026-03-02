package dev.loat.mcp_vision.command;

import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.logging.LogUtils;

import dev.loat.mcp_vision.ModConfig;
import dev.loat.mcp_vision.render.Raytracer;
import dev.loat.mcp_vision.render.renderer.BufferedImageRenderer;
import dev.loat.mcp_vision.util.RPHelper;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.Vec3;

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
                .then(Commands.literal("save")
                    .then(Commands.argument("x", FloatArgumentType.floatArg())
                        .suggests((ctx, builder) -> {
                            if (ctx.getSource().getEntity() != null)
                                builder.suggest(String.valueOf((float) ctx.getSource().getEntity().getX()));
                            return builder.buildFuture();
                        })
                    .then(Commands.argument("y", FloatArgumentType.floatArg())
                        .suggests((ctx, builder) -> {
                            if (ctx.getSource().getEntity() != null)
                                builder.suggest(String.valueOf((float) ctx.getSource().getEntity().getEyeY()));
                            return builder.buildFuture();
                        })
                    .then(Commands.argument("z", FloatArgumentType.floatArg())
                        .suggests((ctx, builder) -> {
                            if (ctx.getSource().getEntity() != null)
                                builder.suggest(String.valueOf((float) ctx.getSource().getEntity().getZ()));
                            return builder.buildFuture();
                        })
                    .then(Commands.argument("yaw", FloatArgumentType.floatArg())
                        .suggests((ctx, builder) -> {
                            if (ctx.getSource().getEntity() != null)
                                builder.suggest(String.valueOf(ctx.getSource().getEntity().getYRot()));
                            return builder.buildFuture();
                        })
                    .then(Commands.argument("pitch", FloatArgumentType.floatArg())
                        .suggests((ctx, builder) -> {
                            if (ctx.getSource().getEntity() != null)
                                builder.suggest(String.valueOf(ctx.getSource().getEntity().getXRot()));
                            return builder.buildFuture();
                        })
                    .then(Commands.argument("width", IntegerArgumentType.integer(128))
                    .then(Commands.argument("height", IntegerArgumentType.integer(128))
                    .then(Commands.argument("fov", IntegerArgumentType.integer(30, 110))
                        .suggests((ctx, builder) -> {
                            builder.suggest(ModConfig.getInstance().fov);
                            return builder.buildFuture();
                        })
                    .executes(x -> createImageAsync(
                        x,
                        (ServerLevel) x.getSource().getLevel(),
                        new Vec3(
                            FloatArgumentType.getFloat(x, "x"),
                            FloatArgumentType.getFloat(x, "y"),
                            FloatArgumentType.getFloat(x, "z")
                        ),
                        FloatArgumentType.getFloat(x, "yaw"),
                        FloatArgumentType.getFloat(x, "pitch"),
                        IntegerArgumentType.getInteger(x, "width"),
                        IntegerArgumentType.getInteger(x, "height"),
                        IntegerArgumentType.getInteger(x, "fov")
                    ))))))))))
                )
                .then(Commands.literal("clear-cache")
                    .executes(x -> {
                        RPHelper.clearCache();
                        Raytracer.clearCache();
                        return 0;
                    })
                )
        ));
    }

    private static int createImageAsync(
        CommandContext<CommandSourceStack> context,
        ServerLevel level,
        Vec3 position,
        float yaw,
        float pitch,
        int width,
        int height,
        int fov
    ) {
        CommandSourceStack source = context.getSource();

        if (ModConfig.getInstance().showSystemMessages)
            source.sendSuccess(() -> Component.literal("Taking screenshot..."), false);

        var renderer = new BufferedImageRenderer(level, position, yaw, pitch, width, height, fov, ModConfig.getInstance().renderDistance);

        long startTime = System.nanoTime();

        CompletableFuture.supplyAsync(renderer::render)
            .thenAcceptAsync(mapImage -> finalizeImage(mapImage, startTime, source), source.getServer());
        return 0;
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
                long secs = durationInMillis / 1000;
                String time = secs > 0 ? String.format("%ds %dms", secs, millis) : String.format("%dms", millis);
                source.sendSuccess(() -> Component.literal("Done! ("+time+")"), false);
            }
        } catch (IOException e) {
            LogUtils.getLogger().error("Could not write image to " + file.getPath());
        }
    }
}
