package yay.evy.everest.vstuff.events;

import net.minecraft.server.level.ServerLevel;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import yay.evy.everest.vstuff.magnetism.MagnetismManager;

@Mod.EventBusSubscriber(modid = "vstuff")
public class WorldLoadHandler {

    @SubscribeEvent
    public static void onWorldLoad(LevelEvent.Load event) {
        if (!event.getLevel().isClientSide() && event.getLevel() instanceof ServerLevel serverLevel) {
            System.out.println("World loaded: " + serverLevel.dimension().location());
            serverLevel.getServer().execute(() -> {
                serverLevel.getServer().tell(new net.minecraft.server.TickTask(200, () -> { // 10 seconds
                    MagnetismManager.loadMagnetPositions(serverLevel);
                }));
            });
        }
    }

}
