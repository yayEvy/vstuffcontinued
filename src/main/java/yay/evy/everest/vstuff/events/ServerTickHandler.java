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
    private static final int VALIDATION_INTERVAL = 20; // Every 60 seconds (20 ticks/sec * 60)

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            tickCounter++;

            for (ServerLevel level : event.getServer().getAllLevels()) {
                if (level.players().size() > 0 || level.getChunkSource().getLoadedChunksCount() > 0) {
                    MagnetismManager.onServerTick(level);
                }
            }

            // Validate constraints every 60 seconds
            if (tickCounter >= VALIDATION_INTERVAL) {
                tickCounter = 0;

                // Validate constraints in all dimensions
                for (ServerLevel level : event.getServer().getAllLevels()) {
                    ConstraintTracker.validateAndCleanupConstraints(level);
                }
            }
        }
    }
}
