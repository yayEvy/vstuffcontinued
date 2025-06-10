package yay.evy.everest.vstuff.magnetism;

import net.minecraft.server.level.ServerLevel;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber
public class MagnetismTickHandler {
    private static int tickCounter = 0;
    private static final int SPATIAL_CHECK_INTERVAL = 10;

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            tickCounter++;

            if (tickCounter >= SPATIAL_CHECK_INTERVAL) {
                tickCounter = 0;

                MagnetRegistry registry = MagnetRegistry.getInstance();

                event.getServer().getAllLevels().forEach(level -> {
                    if (level instanceof ServerLevel) {
                        ServerLevel serverLevel = (ServerLevel) level;

                        registry.performSpatialCheck(serverLevel);

                        registry.magnetPairs.forEach((shipId, pairs) -> {
                            if (!pairs.isEmpty()) {
                             //   System.out.println("[TICK] Ship " + shipId + " has " + pairs.size() + " pairs after spatial check");
                            }
                        });
                    }
                });
            }
        }
    }
}
