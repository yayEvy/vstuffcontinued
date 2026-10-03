package dev.flarelog.vstuff.internal.utility;


import com.mojang.datafixers.util.Either;
import dev.flarelog.vstuff.content.ropes.Rope;
import dev.flarelog.vstuff.content.ropes.util.RopeSegment;
import org.valkyrienskies.core.api.events.PhysTickEvent;
import org.valkyrienskies.core.api.world.PhysLevel;
import org.valkyrienskies.core.internal.world.VsiPhysLevel;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

public class JointScheduler {
    private static final Set<Rope> ropes = ConcurrentHashMap.newKeySet();

    public static void tick(PhysTickEvent event) {
        VsiPhysLevel level = (VsiPhysLevel) event.getWorld();
        for (Rope rope : new HashSet<>(ropes)) {

            AtomicBoolean failed = new AtomicBoolean(false);

            for (RopeSegment segment : rope.segments) {
                if (segment == null)
                    throw new RuntimeException("null segment");
                if (bodiesExist(segment, level)) {
                    segment.joint().ifRight((joint) -> {
                        int id = level.addJoint(joint);
                        if (id != -1) {
                            segment.joint(Either.left(id));
                            return;
                        }
                        failed.set(true);
                    });
                }
                if (!failed.get()) {
                    ropes.remove(rope);
                }

            }
        }
    }

    private static boolean bodiesExist(RopeSegment segment, PhysLevel level) {
        return (segment.pos0().isWorld() || level.getBodyById(segment.pos0().id()) != null) && (segment.pos1().isWorld() || level.getBodyById(segment.pos1().id()) != null);
    }


}
