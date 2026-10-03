package dev.flarelog.vstuff.internal.utility;


import com.mojang.datafixers.util.Either;
import com.mojang.logging.LogUtils;
import dev.flarelog.vstuff.content.ropes.Rope;
import dev.flarelog.vstuff.content.ropes.RopeFactory;
import dev.flarelog.vstuff.content.ropes.util.RopeSegment;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraftforge.server.ServerLifecycleHooks;
import org.slf4j.Logger;
import org.valkyrienskies.core.api.events.PhysTickEvent;
import org.valkyrienskies.core.api.world.PhysLevel;
import org.valkyrienskies.core.internal.world.VsiPhysLevel;
import org.valkyrienskies.mod.common.VSGameUtilsKt;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

public class JointScheduler {
    private static final ConcurrentHashMap<String,ConcurrentHashMap<Rope, Integer>> dimensionsToRopes = new ConcurrentHashMap<>();

    public static final int RETRIES = 60;
    public static final Logger LOGGER = LogUtils.getLogger();

    public static void tick(PhysTickEvent event) {
        VsiPhysLevel level = (VsiPhysLevel) event.getWorld();
        ConcurrentHashMap<Rope, Integer> ropes = dimensionsToRopes.get(event.getWorld().getDimension());

        if (ropes == null) {
            return;
        }

        for (Rope rope : new HashSet<>(ropes.keySet())) {

            if (ropes.get(rope) > RETRIES) {
                LOGGER.error("Retries exceeded for rope {}, discarding", rope.getRopeId());
                ropes.remove(rope);
                RopeFactory.removeAndCleanupRope(rope, ServerLifecycleHooks.getCurrentServer().getLevel(VSGameUtilsKt.getResourceKey(level.getDimension())));
                continue;
            }

            AtomicBoolean failed = new AtomicBoolean(false);

            for (RopeSegment segment : rope.segments) {
                if (segment == null) {
                    throw new RuntimeException("null segment");
                }

                if (bodiesExist(segment, level)) {
                    segment.joint().ifRight((joint) -> {
                        int id = level.addJoint(joint);
                        if (id != -1) {
                            segment.joint(Either.left(id));
                            // TODO: remove the need for this and store joint ids per segment
                            rope.getJointIds().add(id);
                            return;
                        }
                        failed.set(true);
                    });
                }
            }

            if (!failed.get()) {
                ropes.remove(rope);
                continue;
            }

            ropes.put(rope, ropes.get(rope) + 1);
        }
    }

    public static void schedule(Rope rope, Level level) {
        dimensionsToRopes.computeIfAbsent(VSGameUtilsKt.getDimensionId(level), (s) -> new ConcurrentHashMap<>()).put(rope, 0);
    }

    private static boolean bodiesExist(RopeSegment segment, PhysLevel level) {
        return (segment.pos0().isWorld() || level.getBodyById(segment.pos0().id()) != null) && (segment.pos1().isWorld() || level.getBodyById(segment.pos1().id()) != null);
    }


}
