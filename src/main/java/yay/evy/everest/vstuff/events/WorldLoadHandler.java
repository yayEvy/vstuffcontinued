package yay.evy.everest.vstuff.events;

import net.minecraft.server.level.ServerLevel;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import yay.evy.everest.vstuff.ropes.ConstraintPersistence;

@Mod.EventBusSubscriber(modid = "vstuff")
public class WorldLoadHandler {

    @SubscribeEvent
    public static void onWorldLoad(LevelEvent.Load event) {
        if (!event.getLevel().isClientSide() && event.getLevel() instanceof ServerLevel serverLevel) {
            System.out.println("World loaded: " + serverLevel.dimension().location());

            // Only restore constraints for the overworld (or whichever dimension you want)
            if (serverLevel.dimension() == net.minecraft.world.level.Level.OVERWORLD) {
                System.out.println("Scheduling constraint restoration for overworld...");

                serverLevel.getServer().execute(() -> {
                    serverLevel.getServer().tell(new net.minecraft.server.TickTask(200, () -> { // 10 seconds
                        try {
                            System.out.println("Attempting constraint restoration...");
                            ConstraintPersistence persistence = ConstraintPersistence.get(serverLevel);


                            // Attempt restoration
                            persistence.restoreConstraints(serverLevel);

                            System.out.println("Constraint restoration attempt completed");
                        } catch (Exception e) {
                            System.err.println("Error during constraint restoration: " + e.getMessage());
                            e.printStackTrace();
                        }
                    }));
                });
            }
        }
    }
}