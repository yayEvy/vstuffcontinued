package yay.evy.everest.vstuff.ropes;

import net.minecraft.server.level.ServerLevel;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.valkyrienskies.mod.common.VSGameUtilsKt;

@Mod.EventBusSubscriber(modid = "vstuff")
public class ServerEventHandler {


    private static boolean restorationScheduled = false;
    private static int ticksUntilRestore = 500; // 15 seconds at 20 TPS
    private static net.minecraft.server.MinecraftServer serverInstance = null;

    @SubscribeEvent
    public static void onServerStarted(ServerStartedEvent event) {
        System.out.println("VStuff: Server started, resetting constraint system...");


        // Don't reset restoration state here - let each dimension handle it
        restorationScheduled = true;
        ticksUntilRestore = 500; // 25 seconds to ensure ships are loaded
        serverInstance = event.getServer();
    }

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }

        // Handle restoration scheduling
        if (restorationScheduled && serverInstance != null) {
            ticksUntilRestore--;
            if (ticksUntilRestore <= 0) {
                restorationScheduled = false;
                System.out.println("VStuff: Starting constraint restoration...");

                for (ServerLevel level : serverInstance.getAllLevels()) {
                    System.out.println("VStuff: Restoring constraints for dimension: " + level.dimension().location());
                    try {
                        if (VSGameUtilsKt.getShipObjectWorld(level) != null) {
                            ConstraintPersistence persistence = ConstraintPersistence.get(level);
                            // Only restore if not already attempted
                            persistence.restoreConstraints(level);
                        } else {
                            System.err.println("VStuff: VS not ready for dimension " + level.dimension().location());
                        }
                    } catch (Exception e) {
                        System.err.println("VStuff: Error restoring constraints for " + level.dimension().location() + ": " + e.getMessage());
                        e.printStackTrace();
                    }
                }
                serverInstance = null;
            }
        }

        // Handle cleanup ticks for all dimensions
        if (serverInstance != null) {
            for (ServerLevel level : serverInstance.getAllLevels()) {
                try {
                    ConstraintPersistence persistence = ConstraintPersistence.get(level);
                    persistence.tickCleanup();
                } catch (Exception e) {
                    // Ignore errors during cleanup ticking
                }
            }
        }
    }

    private static void scheduleConstraintRestoration(net.minecraft.server.MinecraftServer server, int attempt) {
        final int maxAttempts = 3;
        final int delayTicks = 100; // 5 seconds at 20 TPS

        server.execute(() -> {
            // Wait before attempting
            server.getTickCount(); // Ensure we're in a valid tick context

            // Schedule the actual restoration after delay
            scheduleDelayedRestoration(server, attempt, maxAttempts);
        });
    }

    private static void scheduleDelayedRestoration(net.minecraft.server.MinecraftServer server, int attempt, int maxAttempts) {
        // Use the server's scheduler instead of Thread.sleep
        server.execute(() -> {
            try {
                // Wait a bit longer for ships to load
                Thread.sleep(1 + (attempt * 1)); // 10s + 2s per attempt

                boolean allDimensionsReady = true;
                int totalShipsFound = 0;

                for (ServerLevel level : server.getAllLevels()) {
                    try {
                        if (VSGameUtilsKt.getShipObjectWorld(level) != null) {
                            int shipCount = VSGameUtilsKt.getShipObjectWorld(level).getAllShips().size();
                            totalShipsFound += shipCount;
                            System.out.println("VStuff: Dimension " + level.dimension().location() + " has " + shipCount + " ships loaded");
                        } else {
                            System.err.println("VStuff: VS not ready for dimension " + level.dimension().location());
                            allDimensionsReady = false;
                        }
                    } catch (Exception e) {
                        System.err.println("VStuff: Error checking dimension " + level.dimension().location() + ": " + e.getMessage());
                        allDimensionsReady = false;
                    }
                }

                System.out.println("VStuff: Attempt " + (attempt + 1) + "/" + maxAttempts +
                        " - Found " + totalShipsFound + " total ships, all dimensions ready: " + allDimensionsReady);

                if (allDimensionsReady) {
                    // Proceed with restoration
                    restoreConstraintsForAllDimensions(server);
                } else if (attempt < maxAttempts - 1) {
                    System.out.println("VStuff: Not all dimensions ready, retrying in a moment...");
                    scheduleConstraintRestoration(server, attempt + 1);
                } else {
                    System.err.println("VStuff: Max attempts reached, proceeding with constraint restoration anyway");
                    restoreConstraintsForAllDimensions(server);
                }

            } catch (InterruptedException e) {
                System.err.println("VStuff: Constraint restoration interrupted");
                Thread.currentThread().interrupt();
            }
        });
    }

    private static void restoreConstraintsForAllDimensions(net.minecraft.server.MinecraftServer server) {
        for (ServerLevel level : server.getAllLevels()) {
            System.out.println("VStuff: Restoring constraints for dimension: " + level.dimension().location());
            try {
                if (VSGameUtilsKt.getShipObjectWorld(level) != null) {
                    ConstraintPersistence persistence = ConstraintPersistence.get(level);
                    persistence.restoreConstraints(level);
                } else {
                    System.err.println("VStuff: VS still not ready for dimension " + level.dimension().location());
                }
            } catch (Exception e) {
                System.err.println("VStuff: Error restoring constraints for " + level.dimension().location() + ": " + e.getMessage());
                e.printStackTrace();
            }
        }
    }
}
