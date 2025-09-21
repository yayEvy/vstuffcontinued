package yay.evy.everest.vstuff.events;

import net.minecraft.server.level.ServerLevel;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import yay.evy.everest.vstuff.content.constraint.ConstraintTracker;
import yay.evy.everest.vstuff.content.constraint.ConstraintPersistence;
import net.minecraftforge.event.server.ServerStartedEvent;

import java.util.HashMap;
import java.util.Map;

@Mod.EventBusSubscriber(modid = "vstuff")
public class ServerTickHandler {
    private static int globalTickCounter = 0;
    private static final int VALIDATION_INTERVAL = 20;
    private static final Map<String, Integer> dimensionTickCounters = new HashMap<>();



    @SubscribeEvent
    public static void onServerStarted(ServerStartedEvent event) {
        for (ServerLevel level : event.getServer().getAllLevels()) {
            System.out.println("VStuff: Restoring constraints for dimension: " + level.dimension().location());
            ConstraintPersistence.get(level).restoreConstraintsInstance(level);
        }
    }

    // this drove me insane so fuck you now you dont get rope validation :clueless:
/*
    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            globalTickCounter++;

            for (ServerLevel level : event.getServer().getAllLevels()) {
                String dimensionKey = level.dimension().location().toString();
                int dimensionTicks = dimensionTickCounters.getOrDefault(dimensionKey, 0) + 1;
                dimensionTickCounters.put(dimensionKey, dimensionTicks);
            }

            if (globalTickCounter >= VALIDATION_INTERVAL) {
                globalTickCounter = 0;
                for (ServerLevel level : event.getServer().getAllLevels()) {
                    if (!level.players().isEmpty()) {
                        ConstraintTracker.validateAndCleanupConstraints(level);
                    }
                }
            }
        }
    }


 */

}