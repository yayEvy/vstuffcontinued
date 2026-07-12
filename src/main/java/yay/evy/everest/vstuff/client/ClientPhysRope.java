package yay.evy.everest.vstuff.client;

import net.minecraft.resources.ResourceKey;
import org.joml.Vector3d;
import yay.evy.everest.vstuff.internal.styling.data.RopeStyle;
import yay.evy.everest.vstuff.internal.utility.records.RopeSegment;

import java.util.List;

public record ClientPhysRope(Integer id, Vector3d pos0, Vector3d pos1, List<RopeSegment> segments, ResourceKey<RopeStyle> styleKey) {
    // the fuck you
}
