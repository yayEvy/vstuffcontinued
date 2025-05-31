package yay.evy.everest.vstuff.ropes;

import net.minecraft.server.level.ServerLevel;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.valkyrienskies.mod.common.VSGameUtilsKt;

@Mod.EventBusSubscriber(modid = "vstuff")
public class ServerEventHandler {

    @SubscribeEvent
    public static void onServerStarted(ServerStartedEvent event) {
        System.out.println("VStuff: Server started, scheduling constraint restoration...");

        event.getServer().execute(() -> {
            try {
                Thread.sleep(5000); // 5 seconds

                for (ServerLevel level : event.getServer().getAllLevels()) {
                    System.out.println("VStuff: Restoring constraints for dimension: " + level.dimension().location());

                    try {

                        if (VSGameUtilsKt.getShipObjectWorld(level) != null) {
                            ConstraintPersistence persistence = ConstraintPersistence.get(level);
                            persistence.restoreConstraints(level);
                        } else {

                            System.err.println("VStuff: VS not ready for dimension " + level.dimension().location());
                        }
                    } catch (Exception e) {
                        System.err.println("VStuff: Error restoring constraints for " + level.dimension().location() + ": " + e.getMessage());
                        e.printStackTrace();
                    }
                }
            } catch (InterruptedException e) {
                System.err.println("VStuff: Constraint restoration interrupted");
                Thread.currentThread().interrupt();
            }
        });
    }
}
