package dev.loat.mcp_vision.render.renderer;

import dev.loat.mcp_vision.ModConfig;
import net.minecraft.util.Mth;

public interface Renderer<T> {
    float FOV_PITCH_RAD = Mth.DEG_TO_RAD * (Mth.clamp(ModConfig.getInstance().fov, 30,110)/2.f);

    T render();
}
