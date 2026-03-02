package de.tomalbrc.cameraobscura.render.renderer;

import de.tomalbrc.cameraobscura.render.Raytracer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

public abstract class AbstractRenderer<T> implements Renderer<T> {
    protected final int width;
    protected final int height;
    protected final double FOV_YAW_RAD;

    protected final LivingEntity entity;
    protected final Raytracer raytracer;

    public AbstractRenderer(LivingEntity entity, int width, int height, int renderDistance) {
        this.entity = entity;
        this.width = width;
        this.height = height;
        double aspectRatio = (double) width / (double) height;
        this.FOV_YAW_RAD = Math.atan(Math.tan(FOV_PITCH_RAD) * aspectRatio);
        this.raytracer = new Raytracer(this.entity, renderDistance);
        this.raytracer.preloadChunks(entity.getOnPos());
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

    public static Vec3 doubleYawPitchRotation(Vec3 base, double firstYaw, double firstPitch, double secondYaw,
                                              double secondPitch) {
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
