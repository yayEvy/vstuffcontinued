package yay.evy.everest.vstuff.events;

import net.minecraft.server.level.ServerLevel;
import net.minecraftforge.event.level.ChunkEvent;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import yay.evy.everest.vstuff.magnetism.MagnetRegistry;

@Mod.EventBusSubscriber
public class WorldEventHandler {

    @SubscribeEvent
    public static void onWorldLoad(LevelEvent.Load event) {
        if (event.getLevel() instanceof ServerLevel serverLevel) {
            System.out.println("[MAGNET] World loaded: " + serverLevel.dimension());

            // Use async loading to prevent tick lag
            MagnetRegistry.getInstance().loadPersistedMagnetsAsync(serverLevel);
        }
    }

    @SubscribeEvent
    public static void onChunkLoad(ChunkEvent.Load event) {
        if (event.getLevel() instanceof ServerLevel serverLevel) {
            int chunkX = event.getChunk().getPos().x;
            int chunkZ = event.getChunk().getPos().z;

            // Validate magnets in this chunk (async)
            MagnetRegistry.getInstance().validateMagnetsInChunk(serverLevel, chunkX, chunkZ);
        }
    }

    @SubscribeEvent
    public static void onWorldSave(LevelEvent.Save event) {
        if (event.getLevel() instanceof ServerLevel) {
            System.out.println("[MAGNET] World saving: " + ((ServerLevel) event.getLevel()).dimension());
            // The persistent data will automatically save when marked dirty
        }
    }
}
