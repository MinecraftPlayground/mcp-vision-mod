package dev.loat.mcp_vision.render.renderer;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.Vec3;

import java.awt.image.BufferedImage;
import java.util.stream.IntStream;

public class BufferedImageRenderer extends AbstractRenderer<BufferedImage> {
    public BufferedImageRenderer(ServerLevel level, Vec3 position, float yaw, float pitch, int width, int height, int fov, int renderDistance) {
        super(level, position, yaw, pitch, width, height, fov, renderDistance);
    }

    public BufferedImage render() {
        var imgFile = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        IntStream.range(0, width * height).parallel().forEach(i -> {
            int x = i % width;
            int y = i / width;
            imgFile.setRGB(x, y, raytracer.trace(position, rayAt(yaw, pitch, x, y)));
        });
        return imgFile;
    }
}
