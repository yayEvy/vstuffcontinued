package dev.flarelog.vstuff.internal.utility;

import dev.flarelog.vstuff.content.ropes.util.RopeUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import org.joml.Vector3d;
import dev.flarelog.vstuff.content.ropes.util.RopePosData;
import dev.flarelog.vstuff.content.ropes.util.RopeSegment;

import java.util.Locale;

public class TagUtils {
    public static String sanitizeFileName(String name) {
        return name.toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9_]", "_");
    }
}
