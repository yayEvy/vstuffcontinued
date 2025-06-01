package yay.evy.everest.vstuff.events;

import net.minecraft.server.level.ServerLevel;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import yay.evy.everest.vstuff.magnetism.MagnetismManager;
import yay.evy.everest.vstuff.ropes.ConstraintTracker;

@Mod.EventBusSubscriber(modid = "vstuff")
public class ServerTickHandler {
    private static int tickCounter = 0;
    private static final int VALIDATION_INTERVAL = 100; // Every 5 seconds (20 ticks/sec * 5)

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            tickCounter++;

            // Always tick magnetism for all levels (magnets need to work even without players)
            for (ServerLevel level : event.getServer().getAllLevels()) {
                MagnetismManager.onServerTick(level);
            }

            // Validate constraints every 5 seconds, but only in dimensions with players
            if (tickCounter >= VALIDATION_INTERVAL) {
                tickCounter = 0;
                for (ServerLevel level : event.getServer().getAllLevels()) {
                    if (level.players().size() > 0) {
                        ConstraintTracker.validateAndCleanupConstraints(level);
                    }
                }
            }
        }
    }
}
