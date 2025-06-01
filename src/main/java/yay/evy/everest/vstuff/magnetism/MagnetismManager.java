package yay.evy.everest.vstuff.magnetism;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.joml.Vector3d;
import org.joml.Vector3dc;
import org.valkyrienskies.core.api.ships.PhysShip;
import org.valkyrienskies.core.api.ships.Ship;
import org.valkyrienskies.core.api.ships.ShipForcesInducer;
import org.valkyrienskies.mod.common.VSGameUtilsKt;
import yay.evy.everest.vstuff.block.ModBlocks;
import yay.evy.everest.vstuff.block.RedstoneMagnetBlock;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;
import java.util.HashSet;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import org.valkyrienskies.core.impl.game.ships.ShipObjectServerWorld;



@Mod.EventBusSubscriber(modid = "vstuff", bus = Mod.EventBusSubscriber.Bus.FORGE)
public class MagnetismManager {
    private static final double MAGNET_RANGE = 10.0; // blocks
    private static final double MAGNETIC_FORCE_MULTIPLIER = 0.1;
    private static final double MIN_MASS_FOR_MOVEMENT = 100.0;
    private static final double STICK_DISTANCE = 2.0;
    private static final double STICK_FORCE = 300000;
    private static final int UPDATE_INTERVAL = 10;
    private static int tickCounter = 0;
    private static final int MAX_MAGNETS_PER_TICK = 10;
    private static final int MAX_PAIRS_PER_TICK = 15;
    private static int lastProcessedMagnetIndex = 0;
    private static boolean hasScannedOnStartup = false;
    private static final Map<String, Boolean> worldStartupScanned = new HashMap<>();
    private static final Gson gson = new Gson();
    private static final Set<String> savedMagnetPositions = new HashSet<>();
    private static final Set<Long> registeredShips = ConcurrentHashMap.newKeySet();


    private static final Set<MagnetInfo> activeMagnets = new HashSet<>();


    private static final Set<MagnetPair> stuckPairs = ConcurrentHashMap.newKeySet();

    private static final Map<Long, MagneticForceInducer> shipForceInducers = new ConcurrentHashMap<>();

    private static class MagnetPair {
        public final MagnetInfo magnet1;
        public final MagnetInfo magnet2;

        public MagnetPair(MagnetInfo m1, MagnetInfo m2) {
            if (m1.hashCode() < m2.hashCode()) {
                this.magnet1 = m1;
                this.magnet2 = m2;
            } else {
                this.magnet1 = m2;
                this.magnet2 = m1;
            }
        }

        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof MagnetPair)) return false;
            MagnetPair other = (MagnetPair) obj;
            return magnet1.equals(other.magnet1) && magnet2.equals(other.magnet2);
        }

        @Override
        public int hashCode() {
            return Objects.hash(magnet1, magnet2);
        }
    }

    private static String positionToString(BlockPos pos, String levelName) {
        return levelName + ":" + pos.getX() + "," + pos.getY() + "," + pos.getZ();
    }

    private static void saveMagnetPositions(ServerLevel level) {
        try {
            Path saveDir = level.getServer().getWorldPath(net.minecraft.world.level.storage.LevelResource.ROOT).resolve("vstuff");
            Files.createDirectories(saveDir);
            Path magnetFile = saveDir.resolve("magnets.json");

            String json = gson.toJson(savedMagnetPositions);
            Files.write(magnetFile, json.getBytes());
            System.out.println("Saved " + savedMagnetPositions.size() + " magnet positions");
        } catch (Exception e) {
            System.err.println("Failed to save magnet positions: " + e.getMessage());
        }
    }

    public static void loadMagnetPositions(ServerLevel level) {
        try {
            Path saveDir = level.getServer().getWorldPath(net.minecraft.world.level.storage.LevelResource.ROOT).resolve("vstuff");
            Path magnetFile = saveDir.resolve("magnets.json");

            if (Files.exists(magnetFile)) {
                String json = Files.readString(magnetFile);
                TypeToken<Set<String>> typeToken = new TypeToken<Set<String>>() {};
                Set<String> loaded = gson.fromJson(json, typeToken.getType());
                if (loaded != null) {
                    savedMagnetPositions.addAll(loaded);
                    System.out.println("Loaded " + savedMagnetPositions.size() + " saved magnet positions");

                    reactivateSavedMagnets(level);
                }
            }
        } catch (Exception e) {
            System.err.println("Failed to load magnet positions: " + e.getMessage());
        }
    }

    // Add a method to properly register force inducers with the physics system
    // Update the registerForceInducerWithPhysics method:
    private static void registerForceInducerWithPhysics(ServerLevel level, Long shipId, MagneticForceInducer inducer) {
        try {
            var shipObjectWorld = VSGameUtilsKt.getShipObjectWorld(level);

            // Get the physics ship
            var physShip = shipObjectWorld.getAllShips().getById(shipId);
            if (physShip != null) {
                // Check if our inducer is already registered
                MagneticForceInducer existingInducer = physShip.getAttachment(MagneticForceInducer.class);

                if (existingInducer == null) {
                    // Register the inducer directly with the physics ship
                    physShip.saveAttachment(MagneticForceInducer.class, inducer);
                    System.out.println("Registered force inducer with physics system for ship " + shipId);
                } else {
                    System.out.println("Force inducer already registered with physics system for ship " + shipId);
                }
            }
        } catch (Exception e) {
            System.err.println("Error registering force inducer with physics: " + e.getMessage());
            e.printStackTrace();
        }
    }


    private static void reactivateSavedMagnets(ServerLevel level) {
        String levelName = level.dimension().location().toString();
        int reactivated = 0;

        System.out.println("Looking for magnets in level: " + levelName);

        for (String posStr : savedMagnetPositions) {
            System.out.println("Checking saved position: " + posStr);

            if (posStr.startsWith(levelName + ":")) {
                try {
                    int lastColon = posStr.lastIndexOf(":");
                    if (lastColon == -1) continue;

                    String coordsPart = posStr.substring(lastColon + 1);
                    String[] coords = coordsPart.split(",");
                    if (coords.length != 3) continue;

                    BlockPos pos = new BlockPos(
                            Integer.parseInt(coords[0].trim()),
                            Integer.parseInt(coords[1].trim()),
                            Integer.parseInt(coords[2].trim())
                    );

                    System.out.println("Parsed position: " + pos);

                    try {
                        level.getChunk(pos);
                        System.out.println("Force loaded chunk for position: " + pos);
                    } catch (Exception e) {
                        System.out.println("Could not force load chunk for: " + pos + " - " + e.getMessage());
                        continue;
                    }

                    if (!level.isLoaded(pos)) {
                        System.out.println("Chunk still not loaded for position: " + pos);
                        continue;
                    }

                    BlockState state = level.getBlockState(pos);
                    System.out.println("Block at " + pos + ": " + state.getBlock().getClass().getSimpleName());

                    if (state.getBlock() instanceof RedstoneMagnetBlock) {
                        boolean isPowered = RedstoneMagnetBlock.isPowered(state);
                        System.out.println("Magnet at " + pos + " is powered: " + isPowered);

                        if (isPowered) {
                            onMagnetActivated(level, pos);
                            reactivated++;
                            System.out.println("Reactivated saved magnet at " + pos);
                        }
                    } else {
                        System.out.println("No magnet block found at " + pos);
                    }
                } catch (Exception e) {
                    System.err.println("Failed to parse magnet position: " + posStr + " - " + e.getMessage());
                }
            }
        }

        System.out.println("Reactivated " + reactivated + " magnets from save file for level " + levelName);
    }




    private static class MagnetInfo {
        public final BlockPos pos;
        public final Long shipId;
        public final Direction attractSide;
        public final Direction repelSide;

        public MagnetInfo(BlockPos pos, Long shipId, Direction attractSide, Direction repelSide) {
            this.pos = pos;
            this.shipId = shipId;
            this.attractSide = attractSide;
            this.repelSide = repelSide;
        }

        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof MagnetInfo)) return false;
            MagnetInfo other = (MagnetInfo) obj;
            return pos.equals(other.pos) && Objects.equals(shipId, other.shipId);
        }

        @Override
        public int hashCode() {
            return Objects.hash(pos, shipId);
        }
    }

    private static void scanForNearbyMagnets(ServerLevel level) {
        Set<MagnetInfo> foundMagnets = new HashSet<>();

        // Always scan around players
        for (var player : level.players()) {
            Vector3d playerPos = new Vector3d(player.getX(), player.getY(), player.getZ());
            var nearbyMagnets = findMagnetsInArea(level, playerPos, 50.0);
            foundMagnets.addAll(nearbyMagnets);
        }

        // Also scan around existing active magnets to find nearby ones
        for (MagnetInfo existingMagnet : new HashSet<>(activeMagnets)) {
            Vector3d magnetPos = getMagnetWorldPosition(level, existingMagnet);
            if (magnetPos != null) {
                var nearbyMagnets = findMagnetsInArea(level, magnetPos, MAGNET_RANGE * 2);
                foundMagnets.addAll(nearbyMagnets);
            }
        }

        int beforeSize = activeMagnets.size();
        activeMagnets.addAll(foundMagnets);
        int afterSize = activeMagnets.size();

        if (beforeSize != afterSize) {
            System.out.println("Scan found " + (afterSize - beforeSize) + " new magnets, total: " + afterSize);
        }
    }



    // Update the onServerTick method in your MagnetismManager class
    public static void onServerTick(ServerLevel level, int dimensionTickCounter) {
        try {
            processDelayedActivations(level);
            String levelKey = level.dimension().location().toString();

            // Ensure force inducers exist early in EACH dimension's lifecycle
            if (dimensionTickCounter == 100) { // After 5 seconds for THIS dimension
                ensureAllForceInducersExist(level);
                System.out.println("Ensured force inducers exist for dimension: " + levelKey);
            }

            // Also re-ensure force inducers periodically in case something goes wrong
            if (dimensionTickCounter % 1200 == 0) { // Every minute
                ensureAllForceInducersExist(level);
                System.out.println("Periodic force inducer check for dimension: " + levelKey);
            }

            // Check if this dimension is in startup period (first 10 seconds)
            boolean isStartupPeriod = dimensionTickCounter <= 200;
            boolean isScanned = worldStartupScanned.getOrDefault(levelKey, false);

            if (isStartupPeriod || !isScanned) {
                // Scan every 20 ticks (1 second) during startup
                if (dimensionTickCounter % 20 == 0) {
                    System.out.println("Startup scan at tick " + dimensionTickCounter + " for dimension " + levelKey);
                    scanForNearbyMagnets(level);
                    loadMagnetPositions(level); // Try to reload saved positions
                }
            } else {
                // After startup, scan every 5 seconds
                if (dimensionTickCounter % 100 == 0) {
                    scanForNearbyMagnets(level);
                }
            }

            // Mark as scanned after startup period
            if (dimensionTickCounter > 200) {
                worldStartupScanned.put(levelKey, true);
            }

            // Always process active magnets if we have any
            if (!activeMagnets.isEmpty()) {
                processActiveMagnetsLimited(level);
            }

            // Regular maintenance (use dimension tick counter for consistency)
            if (dimensionTickCounter % 60 == 0) {
                scanForShipWorldMagnetInteractions(level);
            }

            if (dimensionTickCounter % 1000 == 0) {
                cleanupInactiveMagnets(level);
            }

        } catch (Exception e) {
            System.err.println("Error in magnetism tick for " + level.dimension().location() + ": " + e.getMessage());
            e.printStackTrace();
        }
    }


    private static void processDelayedActivations(ServerLevel level) {
        String levelKey = level.dimension().location().toString();
        Iterator<Map.Entry<String, Integer>> iterator = delayedActivations.entrySet().iterator();

        while (iterator.hasNext()) {
            Map.Entry<String, Integer> entry = iterator.next();
            String key = entry.getKey();
            int remainingTicks = entry.getValue() - 1;

            if (key.endsWith("_" + levelKey)) {
                if (remainingTicks <= 0) {
                    // Time to activate
                    String posStr = key.substring(0, key.lastIndexOf("_"));
                    BlockPos pos = parseBlockPosFromString(posStr);
                    if (pos != null) {
                        BlockState state = level.getBlockState(pos);
                        if (state.getBlock() instanceof RedstoneMagnetBlock && RedstoneMagnetBlock.isPowered(state)) {
                            System.out.println("Executing delayed activation for magnet at " + pos);
                            onMagnetActivated(level, pos);
                        }
                    }
                    iterator.remove();
                } else {
                    entry.setValue(remainingTicks);
                }
            }
        }
    }

    private static void scanForShipWorldMagnetInteractions(ServerLevel level) {
        try {
            // Look for cases where we have ship magnets and world magnets that should interact
            Set<Long> shipIds = activeMagnets.stream()
                    .filter(m -> m.shipId != null)
                    .map(m -> m.shipId)
                    .collect(Collectors.toSet());

            boolean hasWorldMagnets = activeMagnets.stream().anyMatch(m -> m.shipId == null);

            if (!shipIds.isEmpty() && hasWorldMagnets) {
                System.out.println("Detected ship-world magnet scenario, performing extended scan...");
                // Do a more thorough scan when we have both ship and world magnets
                scanForNearbyMagnets(level);
            }
        } catch (Exception e) {
            System.err.println("Error scanning for ship-world interactions: " + e.getMessage());
        }
    }

    private static BlockPos parseBlockPosFromString(String posStr) {
        try {
            // Parse "[x, y, z]" format
            posStr = posStr.replace("[", "").replace("]", "");
            String[] parts = posStr.split(", ");
            return new BlockPos(Integer.parseInt(parts[0]), Integer.parseInt(parts[1]), Integer.parseInt(parts[2]));
        } catch (Exception e) {
            System.err.println("Error parsing BlockPos from string: " + posStr);
            return null;
        }
    }


    private static void processActiveMagnetsLimited(ServerLevel level) {
        if (activeMagnets.isEmpty()) return;

        List<MagnetInfo> activeList = new ArrayList<>(activeMagnets);

        // Only process a few pairs per tick
        int pairsProcessed = 0;
        int maxPairs = 3;

        for (int i = 0; i < activeList.size() && pairsProcessed < maxPairs; i++) {
            MagnetInfo magnet1 = activeList.get(i);
            for (int j = i + 1; j < activeList.size() && pairsProcessed < maxPairs; j++) {
                MagnetInfo magnet2 = activeList.get(j);

                Vector3d pos1 = getMagnetWorldPosition(level, magnet1);
                Vector3d pos2 = getMagnetWorldPosition(level, magnet2);

                if (pos1 != null && pos2 != null) {
                    double distance = pos1.distance(pos2);
                    if (distance <= MAGNET_RANGE) {
                        try {
                            processMagnetPair(level, magnet1, magnet2);
                            pairsProcessed++;
                        } catch (Exception e) {
                            System.err.println("Error processing magnet pair: " + e.getMessage());
                        }
                    }
                }
            }
        }

        if (pairsProcessed > 0) {
            System.out.println("Processed " + pairsProcessed + " magnet pairs this tick");
        }
    }


    private static void cleanupInactiveMagnets(ServerLevel level) {
        int beforeSize = activeMagnets.size();
        activeMagnets.removeIf(magnet -> !isMagnetStillActive(level, magnet));
        int afterSize = activeMagnets.size();

        if (beforeSize != afterSize) {
            System.out.println("Cleanup removed " + (beforeSize - afterSize) + " inactive magnets");
        }
    }



    private static void processActiveMagnets(ServerLevel level) {
        if (activeMagnets.isEmpty()) return;

        System.out.println("Processing " + activeMagnets.size() + " active magnets");



        // Create a stable list for processing
        List<MagnetInfo> activeList = new ArrayList<>(activeMagnets);

        for (int i = 0; i < activeList.size(); i++) {
            MagnetInfo magnet1 = activeList.get(i);

            for (int j = i + 1; j < activeList.size(); j++) {
                MagnetInfo magnet2 = activeList.get(j);

                try {
                    processMagnetPair(level, magnet1, magnet2);
                } catch (Exception e) {
                    System.err.println("Error processing magnet pair " + magnet1.pos + " and " + magnet2.pos + ": " + e.getMessage());
                }
            }
        }
    }



    private static boolean isMagnetStillActive(ServerLevel level, MagnetInfo magnet) {
        try {
            BlockState state;

            if (magnet.shipId != null) {
                var ship = VSGameUtilsKt.getShipManagingPos(level, magnet.pos);
                if (ship == null) {
                    System.out.println("Ship " + magnet.shipId + " no longer exists");
                    return false;
                }

                state = level.getBlockState(magnet.pos);
            } else {
                state = level.getBlockState(magnet.pos);
            }

            if (!(state.getBlock() instanceof RedstoneMagnetBlock)) {
                System.out.println("Block at " + magnet.pos + " is no longer a magnet block, found: " + state.getBlock());
                return false;
            }

            boolean isPowered = RedstoneMagnetBlock.isPowered(state);
            if (!isPowered) {
                System.out.println("Magnet at " + magnet.pos + " is no longer powered");
                return false;
            }

            return true;
        } catch (Exception e) {
            System.err.println("Error checking magnet at " + magnet.pos + ": " + e.getMessage());
            return false;
        }
    }





    private static void updateMagnetism(ServerLevel level) {
        try {
            List<MagnetInfo> allMagnets = findAllActiveMagnets(level);

            int maxMagnetsPerTick = Math.min(20, allMagnets.size()); // Max 20 magnets per tick
            List<MagnetInfo> magnetsToProcess = allMagnets.subList(0, maxMagnetsPerTick);

            processMagnetInteractions(level, magnetsToProcess);
            maintainAllStuckConnections(level);
            cleanupStuckPairs(allMagnets);

        } catch (Exception e) {
            System.err.println("Error updating magnetism: " + e.getMessage());
        }
    }

    private static void maintainAllStuckConnections(ServerLevel level) {
        try {
            Set<MagnetPair> pairsToCheck = new HashSet<>(stuckPairs);

            for (MagnetPair pair : pairsToCheck) {
                Vector3d pos1 = getMagnetWorldPosition(level, pair.magnet1);
                Vector3d pos2 = getMagnetWorldPosition(level, pair.magnet2);

                if (pos1 != null && pos2 != null) {
                    maintainStuckConnection(level, pair.magnet1, pair.magnet2, pos1, pos2);
                }
            }
        } catch (Exception e) {
            System.err.println("Error maintaining stuck connections: " + e.getMessage());
        }
    }


    private static void processMagnetism(ServerLevel level) {
        try {
            List<MagnetInfo> allMagnets = findAllActiveMagnets(level);
            cleanupStuckPairs(allMagnets);

            for (int i = 0; i < allMagnets.size(); i++) {
                for (int j = i + 1; j < allMagnets.size(); j++) {
                    MagnetInfo magnet1 = allMagnets.get(i);
                    MagnetInfo magnet2 = allMagnets.get(j);
                    processMagnetPair(level, magnet1, magnet2);
                }
            }
        } catch (Exception e) {
            System.err.println("Error in magnetism processing: " + e.getMessage());
        }
    }

    private static List<MagnetInfo> findAllActiveMagnets(ServerLevel level) {
        List<MagnetInfo> magnets = new ArrayList<>();
        try {
            for (Ship ship : VSGameUtilsKt.getShipObjectWorld(level).getAllShips()) {
                findMagnetsOnShip(level, ship, magnets);
            }
        } catch (Exception e) {
            System.err.println("Error finding magnets: " + e.getMessage());
        }
        return magnets;
    }

    private static void findMagnetsOnShip(ServerLevel level, Ship ship, List<MagnetInfo> magnets) {
        try {
            Vector3dc shipCenter = ship.getTransform().getPositionInWorld();
            int centerChunkX = (int) Math.floor(shipCenter.x() / 16.0);
            int centerChunkZ = (int) Math.floor(shipCenter.z() / 16.0);

            // Reduced from 10 to 3 chunk radius
            for (int x = centerChunkX - 3; x <= centerChunkX + 3; x++) {
                for (int z = centerChunkZ - 3; z <= centerChunkZ + 3; z++) {
                    scanChunkForMagnets(level, ship.getId(), x, z, magnets);
                }
            }
        } catch (Exception e) {
            System.err.println("Error finding ship magnets: " + e.getMessage());
        }
    }


    private static void processMagnetInteractions(ServerLevel level, List<MagnetInfo> magnets) {
        for (int i = 0; i < magnets.size(); i++) {
            MagnetInfo magnet1 = magnets.get(i);

            for (int j = i + 1; j < Math.min(i + 5, magnets.size()); j++) {
                MagnetInfo magnet2 = magnets.get(j);

                try {
                    processMagnetPair(level, magnet1, magnet2);
                } catch (Exception e) {
                    // Ccgdfgfgsfg
                }
            }
        }
    }


    private static void scanChunkForMagnets(ServerLevel level, long shipId, int chunkX, int chunkZ, List<MagnetInfo> magnets) {
        try {
            int startX = chunkX * 16;
            int startZ = chunkZ * 16;
            for (int x = startX; x < startX + 16; x++) {
                for (int z = startZ; z < startZ + 16; z++) {
                    for (int y = level.getMinBuildHeight(); y < level.getMaxBuildHeight(); y++) {
                        BlockPos pos = new BlockPos(x, y, z);
                        Ship ship = VSGameUtilsKt.getShipManagingPos(level, pos);
                        if (ship != null && ship.getId() == shipId) {
                            BlockState state = level.getBlockState(pos);
                            if (state.is(ModBlocks.REDSTONE_MAGNET.get()) &&
                                    RedstoneMagnetBlock.isPowered(state)) {
                                Direction attractSide = RedstoneMagnetBlock.getAttractSide(state);
                                Direction repelSide = RedstoneMagnetBlock.getRepelSide(state);
                                magnets.add(new MagnetInfo(pos, shipId, attractSide, repelSide));
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            // AAAAAAAAAAAAAAAAAAAA
        }
    }

    private static void processMagnetPair(ServerLevel level, MagnetInfo magnet1, MagnetInfo magnet2) {
        try {
            Vector3d pos1 = getMagnetWorldPosition(level, magnet1);
            Vector3d pos2 = getMagnetWorldPosition(level, magnet2);

            if (pos1 == null || pos2 == null) {
                System.out.println("Skipping pair - null positions");
                return;
            }

            double distance = pos1.distance(pos2);
            System.out.println("Processing magnet pair: " + magnet1.pos + " and " + magnet2.pos +
                    " (distance: " + distance + ")");

            if (distance > MAGNET_RANGE) {
                System.out.println("Distance " + distance + " > range " + MAGNET_RANGE + " - skipping");
                return;
            }

            // Check if magnets should attract or repel
            boolean shouldAttract = shouldMagnetsAttract(magnet1, magnet2);
            System.out.println("Should attract: " + shouldAttract);

            if (shouldAttract && distance <= 1.0 && !areAlreadyStuck(magnet1, magnet2)) {
                stuckPairs.add(new MagnetPair(magnet1, magnet2));
                System.out.println("Magnets stuck together at distance " + distance + "!");
                return;
            }

            if (areAlreadyStuck(magnet1, magnet2)) {
                System.out.println("Magnets are stuck - applying maintenance force");
                Vector3d forceDirection = new Vector3d(pos2).sub(pos1).normalize();
                Vector3d maintainForce = new Vector3d(forceDirection).mul(STICK_FORCE);

                if (magnet1.shipId != null) {
                    applyForceToMagnet(level, magnet1, maintainForce);
                }
                if (magnet2.shipId != null) {
                    applyForceToMagnet(level, magnet2, new Vector3d(maintainForce).negate());
                }
                return;
            }

            Vector3d forceDirection = new Vector3d(pos2).sub(pos1).normalize();
            double forceMagnitude = calculateForceMagnitude(distance, getMagnetMass(level, magnet1), getMagnetMass(level, magnet2));

            if (!shouldAttract) {
                forceMagnitude = -forceMagnitude; // Repel
            }

            Vector3d force1 = new Vector3d(forceDirection).mul(-forceMagnitude);
            Vector3d force2 = new Vector3d(forceDirection).mul(forceMagnitude);

            System.out.println("Applying forces: " + force1 + " and " + force2);
            applyForceToMagnet(level, magnet1, force1);
            applyForceToMagnet(level, magnet2, force2);

        } catch (Exception e) {
            System.err.println("Error processing magnet pair: " + e.getMessage());
            e.printStackTrace();
        }
    }



    private static boolean areAlreadyStuck(MagnetInfo magnet1, MagnetInfo magnet2) {
        for (MagnetPair pair : stuckPairs) {
            if ((pair.magnet1.equals(magnet1) && pair.magnet2.equals(magnet2)) ||
                    (pair.magnet1.equals(magnet2) && pair.magnet2.equals(magnet1))) {
                return true;
            }
        }
        return false;
    }


    private static double calculateForceMagnitude(double distance, double mass1, double mass2) {
        if (distance <= 0) return 0;
        double baseForceMagnitude = 1000000.0;
        double force = baseForceMagnitude / (distance * distance);

        force = Math.max(force, 100000.0);
        return force;
    }



    private static boolean shouldMagnetsAttract(MagnetInfo magnet1, MagnetInfo magnet2) {
        return !magnet1.attractSide.equals(magnet2.attractSide);
    }

    private static Vector3d getMagnetWorldPosition(ServerLevel level, MagnetInfo magnet) {
        try {
            if (magnet.shipId == null) {
                // World magnet - position is already in world coordinates
                return new Vector3d(magnet.pos.getX() + 0.5, magnet.pos.getY() + 0.5, magnet.pos.getZ() + 0.5);
            } else {
                Ship ship = VSGameUtilsKt.getShipObjectWorld(level).getAllShips().getById(magnet.shipId);
                if (ship == null) return null;

                Vector3d shipLocalPos = new Vector3d(magnet.pos.getX() + 0.5, magnet.pos.getY() + 0.5, magnet.pos.getZ() + 0.5);
                Vector3d worldPos = new Vector3d();
                ship.getTransform().getShipToWorld().transformPosition(shipLocalPos, worldPos);
                return worldPos;
            }
        } catch (Exception e) {
            return null;
        }
    }


    private static boolean areAttractingPoles(ServerLevel level, MagnetInfo magnet1, MagnetInfo magnet2,
                                              Vector3d pos1, Vector3d pos2) {
        try {
            Vector3d direction = new Vector3d(pos2).sub(pos1).normalize();

            Vector3d attract1Dir = getDirectionVector(magnet1.attractSide);
            boolean magnet1Attracting = direction.dot(attract1Dir) > 0.5; // 60 degree cone

            Vector3d repel2Dir = getDirectionVector(magnet2.repelSide);
            Vector3d directionReverse = new Vector3d(direction).negate();
            boolean magnet2Repelling = directionReverse.dot(repel2Dir) > 0.5;

            return magnet1Attracting && magnet2Repelling;
        } catch (Exception e) {
            return false;
        }
    }

    private static Vector3d getDirectionVector(Direction direction) {
        return switch (direction) {
            case NORTH -> new Vector3d(0, 0, -1);
            case SOUTH -> new Vector3d(0, 0, 1);
            case EAST -> new Vector3d(1, 0, 0);
            case WEST -> new Vector3d(-1, 0, 0);
            case UP -> new Vector3d(0, 1, 0);
            case DOWN -> new Vector3d(0, -1, 0);
        };
    }

    private static void applyMagneticForce(ServerLevel level, MagnetInfo magnet1, MagnetInfo magnet2,
                                           Vector3d pos1, Vector3d pos2, double distance) {
        try {
            double forceStrength = MAGNETIC_FORCE_MULTIPLIER / (distance * distance);

            // Get masses
            double mass1 = getMagnetMass(level, magnet1);
            double mass2 = getMagnetMass(level, magnet2);

            if (mass1 < MIN_MASS_FOR_MOVEMENT && mass2 < MIN_MASS_FOR_MOVEMENT) return;

            Vector3d forceDirection = new Vector3d(pos2).sub(pos1).normalize();

            if (mass1 < mass2 && mass1 >= MIN_MASS_FOR_MOVEMENT) {
                Vector3d force = new Vector3d(forceDirection).mul(forceStrength / mass1);
                applyForceToMagnet(level, magnet1, force);
            } else if (mass2 < mass1 && mass2 >= MIN_MASS_FOR_MOVEMENT) {
                Vector3d force = new Vector3d(forceDirection).negate().mul(forceStrength / mass2);
                applyForceToMagnet(level, magnet2, force);
            } else if (Math.abs(mass1 - mass2) < MIN_MASS_FOR_MOVEMENT) {
                Vector3d force1 = new Vector3d(forceDirection).mul(forceStrength / (mass1 * 2));
                Vector3d force2 = new Vector3d(forceDirection).negate().mul(forceStrength / (mass2 * 2));
                applyForceToMagnet(level, magnet1, force1);
                applyForceToMagnet(level, magnet2, force2);
            }
        } catch (Exception e) {
            System.err.println("Error applying magnetic force: " + e.getMessage());
        }
    }

    private static double getMagnetMass(ServerLevel level, MagnetInfo magnet) {
        if (magnet.shipId == null) {
            return Double.MAX_VALUE; // World has infinite mass
        }
        try {
            Ship ship = VSGameUtilsKt.getShipObjectWorld(level).getAllShips().getById(magnet.shipId);
            if (ship == null) return MIN_MASS_FOR_MOVEMENT;
            return 1000.0;
        } catch (Exception e) {
            return MIN_MASS_FOR_MOVEMENT;
        }
    }



















    private static void applyForceToMagnet(ServerLevel level, MagnetInfo magnet, Vector3d force) {
        if (magnet.shipId == null) return; // Can't move the world

        try {
            double maxForce = 1000000.0;
            if (force.length() > maxForce) {
                force.normalize().mul(maxForce);
            }

            var shipObjectWorld = VSGameUtilsKt.getShipObjectWorld(level);
            var serverShip = shipObjectWorld.getQueryableShipData().getById(magnet.shipId);

            if (serverShip != null) {
                // Get or create the force inducer
                MagneticForceInducer inducer = MagneticForceInducer.getOrCreate(serverShip);

                // Check if the queue is getting too large
                if (inducer.getQueueSize() > 50) {
                    System.err.println("WARNING: Force queue for ship " + magnet.shipId + " is very large (" +
                            inducer.getQueueSize() + "). This suggests applyForces isn't being called!");

                    inducer.clearQueue();

                    // Try to re-save the attachment
                    serverShip.saveAttachment(MagneticForceInducer.class, inducer);
                    System.out.println("Re-saved attachment for ship " + magnet.shipId);
                    System.out.println("Available methods on shipObjectWorld: " + Arrays.toString(shipObjectWorld.getClass().getMethods()));

                }

                // Add the force
                inducer.addForce(force);

                System.out.println("Applied force " + force + " to ship " + magnet.shipId +
                        " (queue size: " + inducer.getQueueSize() + ")");
            } else {
                System.out.println("Could not find server ship with ID: " + magnet.shipId);
            }
        } catch (Exception e) {
            System.err.println("Error applying force to ship: " + e.getMessage());
            e.printStackTrace();
        }
    }








    private static void maintainStuckConnection(ServerLevel level, MagnetInfo magnet1, MagnetInfo magnet2,
                                                Vector3d pos1, Vector3d pos2) {
        try {
            double distance = pos1.distance(pos2);
            if (distance > STICK_DISTANCE * 1.5) {
                Vector3d forceDirection = new Vector3d(pos2).sub(pos1).normalize();
                Vector3d stickForce = new Vector3d(forceDirection).mul(STICK_FORCE);

                if (magnet1.shipId != null) {
                    applyForceToMagnet(level, magnet1, stickForce);
                }
                if (magnet2.shipId != null) {
                    applyForceToMagnet(level, magnet2, new Vector3d(stickForce).negate());
                }
            }

            if (!isMagnetStillPowered(level, magnet1) || !isMagnetStillPowered(level, magnet2)) {
                stuckPairs.remove(new MagnetPair(magnet1, magnet2));
               // System.out.println("Magnets unstuck - power lost");
            }
        } catch (Exception e) {
            System.err.println("Error maintaining stuck connection: " + e.getMessage());
        }
    }

    private static boolean isMagnetStillPowered(ServerLevel level, MagnetInfo magnet) {
        try {
            BlockState state;
            if (magnet.shipId == null) {
                state = level.getBlockState(magnet.pos);
            } else {
                Ship ship = VSGameUtilsKt.getShipObjectWorld(level).getAllShips().getById(magnet.shipId);
                if (ship == null) return false;
                // Check if the block still exists on the ship
                state = level.getBlockState(magnet.pos);
            }

            return state.is(ModBlocks.REDSTONE_MAGNET.get()) &&
                    RedstoneMagnetBlock.isPowered(state);
        } catch (Exception e) {
            return false;
        }
    }

    private static void cleanupStuckPairs(List<MagnetInfo> activeMagnets) {
        Set<MagnetInfo> activeSet = new HashSet<>(activeMagnets);
        stuckPairs.removeIf(pair ->
                !activeSet.contains(pair.magnet1) || !activeSet.contains(pair.magnet2)
        );
    }

    public static List<MagnetInfo> findMagnetsInArea(ServerLevel level, Vector3d center, double radius) {
        List<MagnetInfo> magnets = new ArrayList<>();

        int searchRadius = Math.min(32, (int) radius);

        int minX = (int) Math.floor(center.x - searchRadius);
        int maxX = (int) Math.ceil(center.x + searchRadius);
        int minY = Math.max(level.getMinBuildHeight(), (int) Math.floor(center.y - searchRadius));
        int maxY = Math.min(level.getMaxBuildHeight(), (int) Math.ceil(center.y + searchRadius));
        int minZ = (int) Math.floor(center.z - searchRadius);
        int maxZ = (int) Math.ceil(center.z + searchRadius);

        // Limit the number of blocks we check per call
        int blocksChecked = 0;
        int maxBlocksPerCall = 1000;

        for (int x = minX; x <= maxX && blocksChecked < maxBlocksPerCall; x++) {
            for (int y = minY; y <= maxY && blocksChecked < maxBlocksPerCall; y++) {
                for (int z = minZ; z <= maxZ && blocksChecked < maxBlocksPerCall; z++) {
                    blocksChecked++;

                    BlockPos pos = new BlockPos(x, y, z);
                    BlockState state = level.getBlockState(pos);

                    if (state.getBlock() instanceof RedstoneMagnetBlock &&
                            RedstoneMagnetBlock.isPowered(state)) {

                        Vector3d blockPos = new Vector3d(x + 0.5, y + 0.5, z + 0.5);
                        if (center.distance(blockPos) <= radius) {
                            var ship = VSGameUtilsKt.getShipManagingPos(level, pos);
                            Long shipId = ship != null ? ship.getId() : null;
                            BlockState magnetState = level.getBlockState(pos);
                            Direction attractSide = RedstoneMagnetBlock.getAttractSide(magnetState);
                            Direction repelSide = RedstoneMagnetBlock.getRepelSide(magnetState);
                            magnets.add(new MagnetInfo(pos, shipId, attractSide, repelSide));
                        }
                    }
                }
            }
        }

        return magnets;
    }


    private static void scanWorldChunkForMagnets(ServerLevel level, int chunkX, int chunkZ,
                                                 Vector3d center, double radius, List<MagnetInfo> magnets) {
        try {
            int startX = chunkX * 16;
            int startZ = chunkZ * 16;

            for (int x = startX; x < startX + 16; x++) {
                for (int z = startZ; z < startZ + 16; z++) {
                    for (int y = level.getMinBuildHeight(); y < level.getMaxBuildHeight(); y++) {
                        BlockPos pos = new BlockPos(x, y, z);

                        if (VSGameUtilsKt.getShipManagingPos(level, pos) != null) continue;

                        BlockState state = level.getBlockState(pos);
                        if (state.is(ModBlocks.REDSTONE_MAGNET.get()) &&
                                RedstoneMagnetBlock.isPowered(state)) {
                            Vector3d blockCenter = new Vector3d(x + 0.5, y + 0.5, z + 0.5);
                            if (blockCenter.distance(center) <= radius) {
                                Direction attractSide = RedstoneMagnetBlock.getAttractSide(state);
                                Direction repelSide = RedstoneMagnetBlock.getRepelSide(state);
                                magnets.add(new MagnetInfo(pos, null, attractSide, repelSide));
                                System.out.println("Found world magnet at " + pos);
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            // C
        }
    }


    private static void findMagnetsOnShipInArea(ServerLevel level, Ship ship, Vector3d center,
                                                double radius, List<MagnetInfo> magnets) {
        try {
            Vector3dc shipCenter = ship.getTransform().getPositionInWorld();
            Vector3d shipCenterMutable = new Vector3d(shipCenter);
            if (shipCenterMutable.distance(center) > radius + 500) return;

            Vector3d shipLocalCenter = new Vector3d();
            ship.getTransform().getWorldToShip().transformPosition(center, shipLocalCenter);

            int centerChunkX = (int) Math.floor(shipLocalCenter.x / 16.0);
            int centerChunkZ = (int) Math.floor(shipLocalCenter.z / 16.0);

            for (int x = centerChunkX - 5; x <= centerChunkX + 5; x++) {
                for (int z = centerChunkZ - 5; z <= centerChunkZ + 5; z++) {
                    scanShipChunkForMagnetsInArea(level, ship.getId(), x, z, center, radius, magnets);
                }
            }
        } catch (Exception e) {
            System.err.println("Error finding ship magnets in area: " + e.getMessage());
        }
    }





    private static void scanShipChunkForMagnetsInArea(ServerLevel level, long shipId, int chunkX, int chunkZ,
                                                      Vector3d center, double radius, List<MagnetInfo> magnets) {
        try {
            Ship ship = VSGameUtilsKt.getShipObjectWorld(level).getAllShips().getById(shipId);
            if (ship == null) return;

            int startX = chunkX * 16;
            int startZ = chunkZ * 16;
            for (int x = startX; x < startX + 16; x++) {
                for (int z = startZ; z < startZ + 16; z++) {
                    for (int y = level.getMinBuildHeight(); y < level.getMaxBuildHeight(); y++) {
                        BlockPos pos = new BlockPos(x, y, z);

                        BlockState state = level.getBlockState(pos);
                        if (state.is(ModBlocks.REDSTONE_MAGNET.get()) &&
                                RedstoneMagnetBlock.isPowered(state)) {

                            Vector3d shipLocalPos = new Vector3d(x + 0.5, y + 0.5, z + 0.5);
                            Vector3d worldPos = new Vector3d();
                            ship.getTransform().getShipToWorld().transformPosition(shipLocalPos, worldPos);

                            if (worldPos.distance(center) <= radius) {
                                Direction attractSide = RedstoneMagnetBlock.getAttractSide(state);
                                Direction repelSide = RedstoneMagnetBlock.getRepelSide(state);
                                magnets.add(new MagnetInfo(pos, shipId, attractSide, repelSide));
                                System.out.println("Found ship magnet at " + pos + " (world pos: " + worldPos + ")");
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {

        }
    }


    public static Set<MagnetPair> getStuckPairs() {
        return new HashSet<>(stuckPairs);
    }

    public static void unstickAll() {
        stuckPairs.clear();
        System.out.println("All magnet pairs unstuck");
    }
    public static void onMagnetDeactivated(ServerLevel level, BlockPos pos) {
        System.out.println("Deactivating magnet at " + pos);
        String posStr = positionToString(pos, level.dimension().location().toString());
        savedMagnetPositions.remove(posStr);
        saveMagnetPositions(level);

        int beforeSize = activeMagnets.size();
        activeMagnets.removeIf(magnet -> magnet.pos.equals(pos));
        stuckPairs.removeIf(pair ->
                pair.magnet1.pos.equals(pos) || pair.magnet2.pos.equals(pos));

        int afterSize = activeMagnets.size();
        System.out.println("Removed " + (beforeSize - afterSize) + " magnets. Active magnets remaining: " + afterSize);
    }





    public static void onMagnetActivated(ServerLevel level, BlockPos pos) {
        try {
            System.out.println("Redstone Magnet at " + pos + " is now ACTIVE - searching for targets...");
            Ship managingShip = VSGameUtilsKt.getShipManagingPos(level, pos);
            Long shipId = managingShip != null ? managingShip.getId() : null;

            // Always ensure force inducer exists for ship magnets immediately
            if (managingShip != null) {
                System.out.println("Magnet is on ship " + shipId + ", ensuring force inducer exists...");
                ensureForceInducerExists(level, shipId);

                if (!isShipFullyLoaded(managingShip)) {
                    System.out.println("Ship not fully loaded, scheduling delayed activation...");
                    scheduleDelayedMagnetActivation(level, pos, 20);
                    return;
                }
            }

            BlockState state = level.getBlockState(pos);
            Direction attractSide = RedstoneMagnetBlock.getAttractSide(state);
            Direction repelSide = RedstoneMagnetBlock.getRepelSide(state);
            MagnetInfo magnetInfo = new MagnetInfo(pos, shipId, attractSide, repelSide);

            String posStr = positionToString(pos, level.dimension().location().toString());
            savedMagnetPositions.add(posStr);
            saveMagnetPositions(level);

            // Remove any existing magnet at this position first
            activeMagnets.removeIf(m -> m.pos.equals(pos) && Objects.equals(m.shipId, shipId));
            activeMagnets.add(magnetInfo);

            System.out.println("Added magnet to active list. Total active: " + activeMagnets.size());

            // Force an immediate scan to find nearby magnets
            performImmediateMagnetScan(level, magnetInfo);

            System.out.println("Magnet activation complete. Total active magnets: " + activeMagnets.size());
        } catch (Exception e) {
            System.err.println("Error processing magnet activation: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Add this new method to ensure force inducers exist
    // Update the ensureForceInducerExists method:
    private static void ensureForceInducerExists(ServerLevel level, Long shipId) {
        try {
            var shipObjectWorld = VSGameUtilsKt.getShipObjectWorld(level);
            var serverShip = shipObjectWorld.getQueryableShipData().getById(shipId);

            if (serverShip != null) {
                MagneticForceInducer inducer = serverShip.getAttachment(MagneticForceInducer.class);
                if (inducer == null) {
                    inducer = new MagneticForceInducer();
                    serverShip.saveAttachment(MagneticForceInducer.class, inducer);
                    System.out.println("Created force inducer for ship " + shipId);
                }
            }
        } catch (Exception e) {
            System.err.println("Error ensuring force inducer exists: " + e.getMessage());
        }
    }

    public static void ensureAllForceInducersExist(ServerLevel level) {
        try {
            System.out.println("Ensuring all force inducers exist for dimension: " + level.dimension().location());

            Set<Long> shipsWithMagnets = new HashSet<>();
            for (MagnetInfo magnet : activeMagnets) {
                if (magnet.shipId != null) {
                    shipsWithMagnets.add(magnet.shipId);
                }
            }

            int ensured = 0;
            for (Long shipId : shipsWithMagnets) {
                try {
                    var shipObjectWorld = VSGameUtilsKt.getShipObjectWorld(level);
                    var serverShip = shipObjectWorld.getQueryableShipData().getById(shipId);
                    if (serverShip != null) {
                        MagneticForceInducer.getOrCreate(serverShip);
                        ensured++;
                    }
                } catch (Exception e) {
                    System.err.println("Error ensuring force inducer for ship " + shipId + ": " + e.getMessage());
                }
            }

            System.out.println("Ensured force inducers exist for " + ensured + " ships in dimension " + level.dimension().location());
        } catch (Exception e) {
            System.err.println("Error ensuring force inducers exist: " + e.getMessage());
        }
    }



    private static void performImmediateMagnetScan(ServerLevel level, MagnetInfo newMagnet) {
        try {
            Vector3d searchCenter = getMagnetWorldPosition(level, newMagnet);
            if (searchCenter == null) {
                System.err.println("Could not get world position for new magnet");
                return;
            }

            System.out.println("Scanning for magnets around " + searchCenter);

            // Force a comprehensive scan around this magnet
            List<MagnetInfo> nearbyMagnets = findMagnetsInArea(level, searchCenter, MAGNET_RANGE * 2);
            System.out.println("Immediate scan found " + nearbyMagnets.size() + " nearby magnets");

            // Add all discovered magnets to active list
            for (MagnetInfo discoveredMagnet : nearbyMagnets) {
                boolean alreadyActive = activeMagnets.stream()
                        .anyMatch(m -> m.pos.equals(discoveredMagnet.pos) && Objects.equals(m.shipId, discoveredMagnet.shipId));
                if (!alreadyActive) {
                    activeMagnets.add(discoveredMagnet);
                    System.out.println("Added discovered magnet at " + discoveredMagnet.pos + " to active list");

                    // Ensure force inducer exists for ship magnets
                    if (discoveredMagnet.shipId != null) {
                        ensureForceInducerExists(level, discoveredMagnet.shipId);
                    }
                }
            }

            // Immediately process interactions with the new magnet
            for (MagnetInfo otherMagnet : nearbyMagnets) {
                if (!otherMagnet.equals(newMagnet)) {
                    System.out.println("Processing immediate interaction between " + newMagnet.pos + " and " + otherMagnet.pos);
                    processMagnetPair(level, newMagnet, otherMagnet);
                }
            }
        } catch (Exception e) {
            System.err.println("Error in immediate magnet scan: " + e.getMessage());
            e.printStackTrace();
        }
    }
    private static boolean isShipFullyLoaded(Ship ship) {
        try {
            return ship.getTransform() != null &&
                    ship.getTransform().getShipToWorld() != null &&
                    ship.getChunkClaim() != null;
        } catch (Exception e) {
            System.err.println("Error checking ship load state: " + e.getMessage());
            return false;
        }
    }


    private static final Map<String, Integer> delayedActivations = new HashMap<>();

    private static void scheduleDelayedMagnetActivation(ServerLevel level, BlockPos pos, int delayTicks) {
        String key = pos.toString() + "_" + level.dimension().location().toString();
        delayedActivations.put(key, delayTicks);
        System.out.println("Scheduled delayed activation for magnet at " + pos + " in " + delayTicks + " ticks");
    }

}
