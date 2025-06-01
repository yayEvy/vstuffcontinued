package yay.evy.everest.vstuff.events;

import net.minecraft.server.level.ServerLevel;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import yay.evy.everest.vstuff.magnetism.MagnetismManager;
import yay.evy.everest.vstuff.ropes.ConstraintTracker;

import java.util.HashMap;
import java.util.Map;

@Mod.EventBusSubscriber(modid = "vstuff")
public class ServerTickHandler {
    private static int globalTickCounter = 0;
    private static final int VALIDATION_INTERVAL = 100; // Every 5 seconds (20 ticks/sec * 5)
    private static final Map<String, Integer> dimensionTickCounters = new HashMap<>();

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            globalTickCounter++;

            for (ServerLevel level : event.getServer().getAllLevels()) {
                String dimensionKey = level.dimension().location().toString();
                int dimensionTicks = dimensionTickCounters.getOrDefault(dimensionKey, 0) + 1;
                dimensionTickCounters.put(dimensionKey, dimensionTicks);

                MagnetismManager.onServerTick(level, dimensionTicks);

            }

            if (globalTickCounter >= VALIDATION_INTERVAL) {
                globalTickCounter = 0;
                for (ServerLevel level : event.getServer().getAllLevels()) {
                    if (level.players().size() > 0) {
                        ConstraintTracker.validateAndCleanupConstraints(level);
                    }
                }
            }
        }
    }
}
