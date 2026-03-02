package de.tomalbrc.cameraobscura.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.logging.LogUtils;
import de.tomalbrc.cameraobscura.ModConfig;
import de.tomalbrc.cameraobscura.render.renderer.BufferedImageRenderer;
import de.tomalbrc.cameraobscura.util.RPHelper;
import eu.pb4.mapcanvas.api.core.CanvasImage;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;

public class CameraCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        LiteralArgumentBuilder<CommandSourceStack> camera_obscura = Commands.literal("camera-obscura");

        var node = camera_obscura
                .then(Commands.literal("save")
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
                .build();

        dispatcher.getRoot().addChild(node);
    }

    private static int createImageFromSource(CommandContext<CommandSourceStack> x, int width, int height) throws CommandSyntaxException {
        var source = EntityArgument.getEntity(x, "source");
        if (source instanceof LivingEntity livingEntity)
            CameraCommand.createImageAsync(x, livingEntity, width, height);
        return 0;
    }

    public static List<ItemStack> mapItems(CanvasImage image, ServerLevel level) {
        var xSections = Mth.ceil(image.getWidth() / 128d);
        var ySections = Mth.ceil(image.getHeight() / 128d);

        var xDelta = (xSections * 128 - image.getWidth()) / 2;
        var yDelta = (ySections * 128 - image.getHeight()) / 2;

        var items = new ArrayList<ItemStack>();

        for (int ys = 0; ys < ySections; ys++) {
            for (int xs = 0; xs < xSections; xs++) {
                var id = level.getFreeMapId();
                var state = MapItemSavedData.createFresh(0, 0, (byte) 0, false, false, ResourceKey.create(Registries.DIMENSION, Identifier.fromNamespaceAndPath("camera-obscura", "generated")));

                for (int xl = 0; xl < 128; xl++) {
                    for (int yl = 0; yl < 128; yl++) {
                        var x = xl + xs * 128 - xDelta;
                        var y = yl + ys * 128 - yDelta;

                        if (x >= 0 && y >= 0 && x < image.getWidth() && y < image.getHeight()) {
                            state.colors[xl + yl * 128] = image.getRaw(x, y);
                        }
                    }
                }

                level.setMapData(id, state);

                var stack = new ItemStack(Items.FILLED_MAP);
                stack.set(DataComponents.MAP_ID, id);
                items.add(stack);
            }
        }

        return items;
    }


    private static void createImageAsync(CommandContext<CommandSourceStack> context, LivingEntity entity, int width, int height) {
        CommandSourceStack source = context.getSource();

        if (ModConfig.getInstance().showSystemMessages)
            source.sendSuccess(() -> Component.literal("Taking photo..."), false);

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
