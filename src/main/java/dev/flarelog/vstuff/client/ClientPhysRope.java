package dev.flarelog.vstuff.client;

import net.minecraft.resources.ResourceKey;
import org.joml.Vector3d;
import dev.flarelog.vstuff.content.ropes.style.RopeStyle;
import dev.flarelog.vstuff.content.ropes.util.RopeSegment;

import java.util.List;

public record ClientPhysRope(Integer id, Vector3d pos0, Vector3d pos1, List<RopeSegment> segments, ResourceKey<RopeStyle> styleKey) {
    // the fuck you
}
