package yay.evy.everest.vstuff.events;

import net.minecraft.server.level.ServerLevel;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.fml.common.Mod;
import yay.evy.everest.vstuff.content.constraint.ConstraintPersistence;
import yay.evy.everest.vstuff.content.constraint.ConstraintTracker;

@Mod.EventBusSubscriber(modid = "vstuff", bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ServerTickHandler {

    private static int tickCounter = 0;
    private static final int TICKS_PER_SECOND = 20; // 20 ticks = 1 second

    @SubscribeEvent
    public static void onServerStarted(ServerStartedEvent event) {
        for (ServerLevel level : event.getServer().getAllLevels()) {
            System.out.println("VStuff: Restoring constraints for dimension: " + level.dimension().location());
            ConstraintPersistence.get(level).restoreConstraintsInstance(level);
        }
    }

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;

        tickCounter++;
        if (tickCounter < TICKS_PER_SECOND) return;
        tickCounter = 0;

        for (ServerLevel level : event.getServer().getAllLevels()) {
            if (level.players().isEmpty()) continue;

            ConstraintTracker.getActiveConstraints().forEach((id, rope) -> {
                if (rope.constraintType == ConstraintTracker.RopeConstraintData.ConstraintType.ROPE_PULLEY) {
                    if (rope.sourceBlockPos != null && level.getBlockState(rope.sourceBlockPos).isAir()) {
                        System.out.println("Removing orphaned rope " + id + " at " + rope.sourceBlockPos);
                        ConstraintTracker.removeConstraintWithPersistence(level, id);
                    }
                }
            });

            ConstraintTracker.validateAndCleanupConstraints(level);
        }
    }
}
