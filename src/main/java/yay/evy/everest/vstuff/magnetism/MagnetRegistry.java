package yay.evy.everest.vstuff.magnetism;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.BlockState;
import org.joml.Vector3d;
import org.valkyrienskies.core.api.ships.ServerShip;
import org.valkyrienskies.mod.common.VSGameUtilsKt;
import yay.evy.everest.vstuff.block.RedstoneMagnetBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import yay.evy.everest.vstuff.block.RedstoneMagnetBlock;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.core.BlockPos;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class MagnetRegistry {
    private static final MagnetRegistry INSTANCE = new MagnetRegistry();
    private final Map<ServerLevel, Set<MagnetData>> activeMagnets = new ConcurrentHashMap<>();
    final Map<Long, Set<MagnetPair>> magnetPairs = new ConcurrentHashMap<>();

    private static final double MAX_INTERACTION_DISTANCE = 80.0;
    private static final double MULTI_MAGNET_BONUS_RANGE = 96.0;

    public static MagnetRegistry getInstance() {
        return INSTANCE;
    }

    private static final Executor MAGNET_EXECUTOR = Executors.newSingleThreadExecutor(r -> {
        Thread t = new Thread(r, "Magnet-Loader");
        t.setDaemon(true);
        return t;
    });

    private final Set<ServerLevel> loadingLevels = ConcurrentHashMap.newKeySet();

    public void registerMagnet(ServerLevel level, BlockPos pos, BlockState state) {
        activeMagnets.computeIfAbsent(level, k -> ConcurrentHashMap.newKeySet())
                .add(new MagnetData(pos, state));
        MagnetPersistentData.get(level).addMagnet(level.dimension(), pos);
        System.out.println("[REGISTRY] Registered magnet at " + pos + ". Total magnets in level: " + activeMagnets.get(level).size());
    }

    public void unregisterMagnet(ServerLevel level, BlockPos pos) {
        Set<MagnetData> magnets = activeMagnets.get(level);
        if (magnets != null) {
            boolean removed = magnets.removeIf(magnet -> magnet.pos.equals(pos));
            System.out.println("[REGISTRY] Unregistered magnet at " + pos + ". Removed: " + removed + ". Remaining: " + magnets.size());
            if (magnets.isEmpty()) {
                activeMagnets.remove(level);
            }
        }
        MagnetPersistentData.get(level).removeMagnet(level.dimension(), pos);
        magnetPairs.values().forEach(pairs ->
                pairs.removeIf(pair -> pair.magnet1.pos.equals(pos) || pair.magnet2.pos.equals(pos))
        );
    }

    public void performSpatialCheck(ServerLevel level) {
        Set<MagnetData> magnets = activeMagnets.get(level);
        if (magnets == null || magnets.size() < 2) {
            clearPairsForDimension(level);
            return;
        }

        System.out.println("[REGISTRY] Performing enhanced spatial check with " + magnets.size() + " magnets");

        Set<MagnetPair> newPairs = new HashSet<>();
        Map<MagnetData, ServerShip> magnetToShip = new HashMap<>();
        Map<ServerShip, List<MagnetData>> shipToMagnets = new HashMap<>();
        List<MagnetData> validMagnets = new ArrayList<>();

        for (MagnetData magnet : magnets) {
            if (!isValidMagnet(level, magnet)) {
                System.out.println("[REGISTRY] Invalid magnet at " + magnet.pos + ", skipping");
                continue;
            }

            ServerShip ship = findShipForMagnet(level, magnet);
            magnetToShip.put(magnet, ship);
            validMagnets.add(magnet);

            if (ship != null) {
                shipToMagnets.computeIfAbsent(ship, k -> new ArrayList<>()).add(magnet);
            }

            System.out.println("[REGISTRY] Magnet at " + magnet.pos + " -> Ship: " +
                    (ship != null ? ship.getId() : "world"));
        }

        createEnhancedMagnetPairs(validMagnets, magnetToShip, shipToMagnets, newPairs, level);
    }

    private void createEnhancedMagnetPairs(List<MagnetData> validMagnets,
                                           Map<MagnetData, ServerShip> magnetToShip,
                                           Map<ServerShip, List<MagnetData>> shipToMagnets,
                                           Set<MagnetPair> newPairs, ServerLevel level) {

        for (int i = 0; i < validMagnets.size(); i++) {
            for (int j = i + 1; j < validMagnets.size(); j++) {
                MagnetData magnet1 = validMagnets.get(i);
                MagnetData magnet2 = validMagnets.get(j);
                ServerShip ship1 = magnetToShip.get(magnet1);
                ServerShip ship2 = magnetToShip.get(magnet2);

                if ((ship1 != null || ship2 != null) && !Objects.equals(ship1, ship2)) {
                    Vector3d pos1 = getMagnetWorldPosition(level, magnet1);
                    Vector3d pos2 = getMagnetWorldPosition(level, magnet2);
                    if (pos1 == null || pos2 == null) continue;

                    double distance = pos1.distance(pos2);

                    double maxRange = getEffectiveInteractionRange(ship1, ship2, shipToMagnets);

             //       System.out.println("[REGISTRY] Distance between magnets: " + distance + ", Max range: " + maxRange);

                    if (distance <= maxRange) {
                        MagnetPair pair = new MagnetPair(magnet1, magnet2, distance,
                                ship1 != null ? ship1.getId() : null,
                                ship2 != null ? ship2.getId() : null);
                        newPairs.add(pair);

                        if (ship1 != null) {
                            magnetPairs.computeIfAbsent(ship1.getId(), k -> ConcurrentHashMap.newKeySet()).add(pair);
                      //      System.out.println("[REGISTRY] Added pair to ship " + ship1.getId());
                        }
                        if (ship2 != null) {
                            magnetPairs.computeIfAbsent(ship2.getId(), k -> ConcurrentHashMap.newKeySet()).add(pair);
                    //        System.out.println("[REGISTRY] Added pair to ship " + ship2.getId());
                        }
                    }
                }
            }
        }

        System.out.println("[REGISTRY] Created " + newPairs.size() + " enhanced magnet pairs");
    }

    private double getEffectiveInteractionRange(ServerShip ship1, ServerShip ship2,
                                                Map<ServerShip, List<MagnetData>> shipToMagnets) {
        double baseRange = MAX_INTERACTION_DISTANCE;

        // Calculate multi-magnet bonus range
        int totalMagnets = 0;
        if (ship1 != null) {
            List<MagnetData> ship1Magnets = shipToMagnets.get(ship1);
            totalMagnets += ship1Magnets != null ? ship1Magnets.size() : 0;
        }
        if (ship2 != null) {
            List<MagnetData> ship2Magnets = shipToMagnets.get(ship2);
            totalMagnets += ship2Magnets != null ? ship2Magnets.size() : 0;
        }

        if (totalMagnets > 2) {
            double bonusMultiplier = 1.0 + (totalMagnets - 2) * 0.2; // 20% bonus per extra magnet
            baseRange = Math.min(baseRange * bonusMultiplier, MULTI_MAGNET_BONUS_RANGE);
            //System.out.println("[REGISTRY] Multi-magnet range bonus: " + bonusMultiplier + "x for " + totalMagnets + " total magnets");
        }

        return baseRange;
    }

    private ServerShip findShipForMagnet(ServerLevel level, MagnetData magnet) {
        ServerShip ship = VSGameUtilsKt.getShipManagingPos(level, magnet.pos);
        if (ship != null) {
            return ship;
        }

        ship = VSGameUtilsKt.getShipObjectManagingPos(level, magnet.pos);
        if (ship != null) {
            return ship;
        }

        for (var shipObj : VSGameUtilsKt.getAllShips(level)) {
            if (shipObj instanceof ServerShip) {
                ServerShip serverShip = (ServerShip) shipObj;
                var aabb = serverShip.getShipAABB();
                if (aabb != null &&
                        magnet.pos.getX() >= aabb.minX() && magnet.pos.getX() <= aabb.maxX() &&
                        magnet.pos.getY() >= aabb.minY() && magnet.pos.getY() <= aabb.maxY() &&
                        magnet.pos.getZ() >= aabb.minZ() && magnet.pos.getZ() <= aabb.maxZ()) {
                    System.out.println("[REGISTRY] Found ship " + serverShip.getId() + " via AABB check");
                    return serverShip;
                }
            }
        }
        return null;
    }

    private void clearPairsForDimension(ServerLevel level) {
        Set<Long> shipsToCheck = new HashSet<>(magnetPairs.keySet());
        for (Long shipId : shipsToCheck) {
            Set<MagnetPair> pairs = magnetPairs.get(shipId);
            if (pairs != null) {
                pairs.removeIf(pair -> {
                    boolean invalid = !isValidMagnet(level, pair.magnet1) || !isValidMagnet(level, pair.magnet2);
                    if (invalid) {
                      //  System.out.println("[REGISTRY] Removing invalid pair for ship " + shipId);
                    }
                    return invalid;
                });
                if (pairs.isEmpty()) {
                    magnetPairs.remove(shipId);
                    System.out.println("[REGISTRY] Removed ship " + shipId + " - no valid pairs remaining");
                }
            }
        }
    }

    private Vector3d getMagnetWorldPosition(ServerLevel level, MagnetData magnet) {
        ServerShip ship = VSGameUtilsKt.getShipManagingPos(level, magnet.pos);
        Vector3d magnetPos = new Vector3d(
                magnet.pos.getX() + 0.5,
                magnet.pos.getY() + 0.5,
                magnet.pos.getZ() + 0.5
        );

        if (ship != null) {
            Vector3d worldPos = new Vector3d();
            ship.getTransform().getShipToWorld().transformPosition(magnetPos, worldPos);
          //  System.out.println("[REGISTRY] Transformed ship magnet " + magnet.pos + " to world pos " + worldPos);
            return worldPos;
        } else {
            System.out.println("[REGISTRY] World magnet at " + magnet.pos + " -> " + magnetPos);
            return magnetPos;
        }
    }

    private boolean isValidMagnet(ServerLevel level, MagnetData magnet) {
        BlockState state = level.getBlockState(magnet.pos);
        boolean valid = state.getBlock() instanceof RedstoneMagnetBlock &&
                RedstoneMagnetBlock.isActive(state); // Changed from isPowered to isActive
        if (!valid) {
        //    System.out.println("[REGISTRY] Invalid magnet at " + magnet.pos + ": block=" + state.getBlock() +
         //           ", active=" + (state.getBlock() instanceof RedstoneMagnetBlock ? RedstoneMagnetBlock.isActive(state) : "N/A") +
             //       ", power=" + (state.getBlock() instanceof RedstoneMagnetBlock ? RedstoneMagnetBlock.getPowerLevel(state) : "N/A"));
        }
        return valid;
    }


    public Set<MagnetPair> getPairsForShip(long shipId) {
        Set<MagnetPair> pairs = magnetPairs.getOrDefault(shipId, Collections.emptySet());
     //   System.out.println("[REGISTRY] getPairsForShip(" + shipId + ") returning " + pairs.size() + " pairs");
        return pairs;
    }

    public void clearPairsForShip(long shipId) {
        magnetPairs.remove(shipId);
    }

    public Vector3d getMagnetWorldPos(ServerLevel level, MagnetData magnet, Long knownShipId) {
        ServerShip ship = null;
        if (knownShipId != null) {
            for (var shipObj : VSGameUtilsKt.getAllShips(level)) {
                if (shipObj instanceof ServerShip && ((ServerShip) shipObj).getId() == knownShipId) {
                    ship = (ServerShip) shipObj;
                    break;
                }
            }
        }

        if (ship == null) {
            ship = VSGameUtilsKt.getShipManagingPos(level, magnet.pos);
        }

        if (ship != null) {
            Vector3d localPos = new Vector3d(
                    magnet.pos.getX() + 0.5,
                    magnet.pos.getY() + 0.5,
                    magnet.pos.getZ() + 0.5
            );
            Vector3d worldPos = new Vector3d();
            ship.getTransform().getShipToWorld().transformPosition(localPos, worldPos);
         //   System.out.println("[REGISTRY] Ship " + ship.getId() + " magnet " + magnet.pos + " -> local: " + localPos + " -> world: " + worldPos);
            return worldPos;
        } else {
            Vector3d worldPos = new Vector3d(
                    magnet.pos.getX() + 0.5,
                    magnet.pos.getY() + 0.5,
                    magnet.pos.getZ() + 0.5
            );
            System.out.println("[REGISTRY] World magnet " + magnet.pos + " -> world: " + worldPos);
            return worldPos;
        }
    }

    public List<MagnetData> getMagnetsOnShip(ServerLevel level, long shipId) {
        Set<MagnetData> allMagnets = activeMagnets.get(level);
        if (allMagnets == null) return Collections.emptyList();

        List<MagnetData> shipMagnets = new ArrayList<>();
        for (MagnetData magnet : allMagnets) {
            ServerShip ship = findShipForMagnet(level, magnet);
            if (ship != null && ship.getId() == shipId) {
                shipMagnets.add(magnet);
            }
        }
        return shipMagnets;
    }

    public Map<Long, Integer> getMagnetCountPerShip(ServerLevel level) {
        Map<Long, Integer> counts = new HashMap<>();
        Set<MagnetData> allMagnets = activeMagnets.get(level);
        if (allMagnets == null) return counts;

        for (MagnetData magnet : allMagnets) {
            ServerShip ship = findShipForMagnet(level, magnet);
            if (ship != null) {
                counts.merge(ship.getId(), 1, Integer::sum);
            }
        }
        return counts;
    }

    public static class MagnetData {
        public final BlockPos pos;
        public final BlockState state;

        public MagnetData(BlockPos pos, BlockState state) {
            this.pos = pos;
            this.state = state;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof MagnetData)) return false;
            MagnetData that = (MagnetData) o;
            return Objects.equals(pos, that.pos);
        }

        @Override
        public int hashCode() {
            return Objects.hash(pos);
        }
    }

    public static class MagnetPair {
        public final MagnetData magnet1;
        public final MagnetData magnet2;
        public final double distance;
        public final Long ship1Id;
        public final Long ship2Id;

        public MagnetPair(MagnetData magnet1, MagnetData magnet2, double distance, Long ship1Id, Long ship2Id) {
            this.magnet1 = magnet1;
            this.magnet2 = magnet2;
            this.distance = distance;
            this.ship1Id = ship1Id;
            this.ship2Id = ship2Id;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof MagnetPair)) return false;
            MagnetPair that = (MagnetPair) o;
            return (Objects.equals(magnet1, that.magnet1) && Objects.equals(magnet2, that.magnet2)) ||
                    (Objects.equals(magnet1, that.magnet2) && Objects.equals(magnet2, that.magnet1));
        }

        @Override
        public int hashCode() {
            return Objects.hash(Math.min(magnet1.hashCode(), magnet2.hashCode()),
                    Math.max(magnet1.hashCode(), magnet2.hashCode()));
        }

        @Override
        public String toString() {
            return "MagnetPair{" +
                    "magnet1=" + magnet1.pos +
                    ", magnet2=" + magnet2.pos +
                    ", distance=" + String.format("%.2f", distance) +
                    ", ship1Id=" + ship1Id +
                    ", ship2Id=" + ship2Id +
                    '}';
        }
    }

    public void loadPersistedMagnetsAsync(ServerLevel level) {
        if (loadingLevels.contains(level)) {
            System.out.println("[MAGNET] Already loading magnets for " + level.dimension().location() + ", skipping");
            return;
        }

        loadingLevels.add(level);
        System.out.println("[MAGNET] Starting async load of persisted magnets for dimension " + level.dimension().location());

        CompletableFuture.supplyAsync(() -> {
            try {
                MagnetPersistentData persistentData = MagnetPersistentData.get(level);
                Set<BlockPos> persistedMagnets = new HashSet<>(persistentData.getMagnets(level.dimension()));
                System.out.println("[MAGNET] Found " + persistedMagnets.size() + " persisted magnets to validate");
                return persistedMagnets;
            } catch (Exception e) {
                System.err.println("[MAGNET] Error loading persistent data: " + e.getMessage());
                e.printStackTrace();
                return Collections.<BlockPos>emptySet();
            }
        }, MAGNET_EXECUTOR).thenAcceptAsync(persistedMagnets -> {
            processMagnetsBatched(level, persistedMagnets);
        }, level.getServer());
    }

    private void processMagnetsBatched(ServerLevel level, Set<BlockPos> persistedMagnets) {
        if (persistedMagnets.isEmpty()) {
            loadingLevels.remove(level);
            System.out.println("[MAGNET] No persisted magnets to process for " + level.dimension().location());
            return;
        }

        Set<MagnetData> existingMagnets = activeMagnets.get(level);
        if (existingMagnets != null) {
            existingMagnets.clear();
        }

        List<BlockPos> magnetList = new ArrayList<>(persistedMagnets);
        AtomicInteger processed = new AtomicInteger(0);
        AtomicInteger valid = new AtomicInteger(0);
        Set<BlockPos> invalidMagnets = ConcurrentHashMap.newKeySet();

        int batchSize = 10;
        processMagnetBatch(level, magnetList, 0, batchSize, processed, valid, invalidMagnets);
    }

    private void processMagnetBatch(ServerLevel level, List<BlockPos> magnetList, int startIndex, int batchSize,
                                    AtomicInteger processed, AtomicInteger valid, Set<BlockPos> invalidMagnets) {
        int endIndex = Math.min(startIndex + batchSize, magnetList.size());

        for (int i = startIndex; i < endIndex; i++) {
            BlockPos pos = magnetList.get(i);
            try {
                if (!level.isLoaded(pos)) {
                    continue;
                }

                BlockState state = level.getBlockState(pos);
                if (state.getBlock() instanceof RedstoneMagnetBlock && RedstoneMagnetBlock.isPowered(state)) {
                    activeMagnets.computeIfAbsent(level, k -> ConcurrentHashMap.newKeySet())
                            .add(new MagnetData(pos, state));
                    valid.incrementAndGet();
                } else {
                    invalidMagnets.add(pos);
                }
            } catch (Exception e) {
                System.err.println("[MAGNET] Error validating magnet at " + pos + ": " + e.getMessage());
                invalidMagnets.add(pos);
            }
            processed.incrementAndGet();
        }

        if (endIndex < magnetList.size()) {
            level.getServer().execute(() ->
                    processMagnetBatch(level, magnetList, endIndex, batchSize, processed, valid, invalidMagnets)
            );
        } else {
            finalizeMagnetLoading(level, processed.get(), valid.get(), invalidMagnets);
        }
    }

    private void finalizeMagnetLoading(ServerLevel level, int processed, int valid, Set<BlockPos> invalidMagnets) {
        if (!invalidMagnets.isEmpty()) {
            CompletableFuture.runAsync(() -> {
                try {
                    MagnetPersistentData persistentData = MagnetPersistentData.get(level);
                    for (BlockPos invalidPos : invalidMagnets) {
                        persistentData.removeMagnet(level.dimension(), invalidPos);
                    }
                    System.out.println("[MAGNET] Cleaned up " + invalidMagnets.size() + " invalid magnets from persistent storage");
                } catch (Exception e) {
                    System.err.println("[MAGNET] Error cleaning up invalid magnets: " + e.getMessage());
                }
            }, MAGNET_EXECUTOR);
        }

        loadingLevels.remove(level);

        Map<Long, Integer> magnetCounts = getMagnetCountPerShip(level);
        System.out.println("[MAGNET] Finished loading magnets for " + level.dimension().location() +
                ". Processed: " + processed + ", Valid: " + valid + ", Invalid: " + invalidMagnets.size());
        System.out.println("[MAGNET] Magnets per ship: " + magnetCounts);
    }

    public void validateMagnetsInChunk(ServerLevel level, int chunkX, int chunkZ) {
        if (loadingLevels.contains(level)) {
            return;
        }

        CompletableFuture.supplyAsync(() -> {
            try {
                MagnetPersistentData persistentData = MagnetPersistentData.get(level);
                Set<BlockPos> persistedMagnets = persistentData.getMagnets(level.dimension());
                return persistedMagnets.stream()
                        .filter(pos -> pos.getX() >> 4 == chunkX && pos.getZ() >> 4 == chunkZ)
                        .collect(Collectors.toSet());
            } catch (Exception e) {
                System.err.println("[MAGNET] Error loading chunk magnets: " + e.getMessage());
                return Collections.<BlockPos>emptySet();
            }
        }, MAGNET_EXECUTOR).thenAcceptAsync(chunkMagnets -> {
            if (chunkMagnets.isEmpty()) return;

            for (BlockPos pos : chunkMagnets) {
                Set<MagnetData> levelMagnets = activeMagnets.get(level);
                boolean alreadyRegistered = levelMagnets != null &&
                        levelMagnets.stream().anyMatch(magnet -> magnet.pos.equals(pos));

                if (!alreadyRegistered) {
                    try {
                        BlockState state = level.getBlockState(pos);
                        if (state.getBlock() instanceof RedstoneMagnetBlock && RedstoneMagnetBlock.isPowered(state)) {
                            activeMagnets.computeIfAbsent(level, k -> ConcurrentHashMap.newKeySet())
                                    .add(new MagnetData(pos, state));
                            System.out.println("[MAGNET] Restored magnet at " + pos + " from chunk load");
                        }
                    } catch (Exception e) {
                        System.err.println("[MAGNET] Error validating chunk magnet at " + pos + ": " + e.getMessage());
                    }
                }
            }
        }, level.getServer());
    }
}

