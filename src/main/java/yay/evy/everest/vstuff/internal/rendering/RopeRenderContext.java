package yay.evy.everest.vstuff.internal.rendering;

import net.minecraft.world.level.Level;
import org.joml.Vector3d;

public record RopeRenderContext(
        Vector3d startRelative,
        Vector3d endRelative,
        Vector3d prevStartRelative,
        Vector3d prevEndRelative,
        double maxLength,
        double actualLength,
        float partialTick,
        Level level
) {}
