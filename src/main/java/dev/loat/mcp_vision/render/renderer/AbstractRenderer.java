package dev.loat.mcp_vision.render.renderer;

import dev.loat.mcp_vision.render.Raytracer;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

public abstract class AbstractRenderer<T> implements Renderer<T> {
    protected final int width;
    protected final int height;
    protected final double FOV_PITCH_RAD;
    protected final double FOV_YAW_RAD;
    protected final Vec3 position;
    protected final float yaw;
    protected final float pitch;
    protected final Raytracer raytracer;

    public AbstractRenderer(ServerLevel level, Vec3 position, float yaw, float pitch, int width, int height, float fov, int renderDistance) {
        this.position = position;
        this.yaw = yaw;
        this.pitch = pitch;
        this.width = width;
        this.height = height;
        double fovRad = Mth.clamp(fov, 30, 110) * Mth.DEG_TO_RAD;
        double fovPitchRad = fovRad / 2.0;
        double aspectRatio = (double) width / (double) height;
        this.FOV_YAW_RAD = Math.atan(Math.tan(fovPitchRad) * aspectRatio);
        this.FOV_PITCH_RAD = fovPitchRad;
        this.raytracer = new Raytracer(level, position, yaw, pitch, renderDistance);
        this.raytracer.preloadChunks(BlockPos.containing(position));
    }

    public static Vec3 yawPitchRotation(Vec3 base, double angleYaw, double anglePitch) {
        double oldX = base.x();
        double oldY = base.y();
        double oldZ = base.z();

        double sinOne = Math.sin(angleYaw);
        double sinTwo = Math.sin(anglePitch);
        double cosOne = Math.cos(angleYaw);
        double cosTwo = Math.cos(anglePitch);

        double newX = oldX * cosOne * cosTwo - oldY * cosOne * sinTwo - oldZ * sinOne;
        double newY = oldX * sinTwo + oldY * cosTwo;
        double newZ = oldX * sinOne * cosTwo - oldY * sinOne * sinTwo + oldZ * cosOne;

        return new Vec3(newX, newY, newZ);
    }

    public static Vec3 doubleYawPitchRotation(Vec3 base, double firstYaw, double firstPitch, double secondYaw, double secondPitch) {
        return yawPitchRotation(yawPitchRotation(base, firstYaw, firstPitch), secondYaw, secondPitch);
    }

    protected Vec3 rayAt(float yaw, float pitch, int x, int y) {
        double yawRad = (yaw + 90) * Mth.DEG_TO_RAD;
        double pitchRad = -pitch * Mth.DEG_TO_RAD;

        double u = ((double) x / (width - 1)) * 2.0 - 1.0;
        double v = ((double) y / (height - 1)) * 2.0 - 1.0;

        double tanYaw   = Math.tan(FOV_YAW_RAD);
        double tanPitch = Math.tan(FOV_PITCH_RAD);

        Vec3 localRay = new Vec3(1.0, -v * tanPitch, u * tanYaw).normalize();

        return yawPitchRotation(localRay, yawRad, pitchRad);
    }
}
