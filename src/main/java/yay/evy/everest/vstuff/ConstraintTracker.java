package yay.evy.everest.vstuff;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.joml.Vector3d;
import org.valkyrienskies.core.api.ships.Ship;
import org.valkyrienskies.mod.common.VSGameUtilsKt;
import yay.evy.everest.vstuff.network.NetworkHandler;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Mod.EventBusSubscriber(modid = "vstuff", bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ConstraintTracker {

    private static final Map<Integer, RopeConstraintData> activeConstraints = new ConcurrentHashMap<>();

    public static class RopeConstraintData {
        public final Long shipA;
        public final Long shipB;
        public final Vector3d localPosA;
        public final Vector3d localPosB;
        public final double maxLength;
        public final double compliance;
        public final double maxForce;

        public RopeConstraintData(Long shipA, Long shipB, Vector3d localPosA, Vector3d localPosB,
                                  double maxLength, double compliance, double maxForce) {
            this.shipA = shipA;
            this.shipB = shipB;
            this.localPosA = new Vector3d(localPosA);
            this.localPosB = new Vector3d(localPosB);
            this.maxLength = maxLength;
            this.compliance = compliance;
            this.maxForce = maxForce;
        }

        public Vector3d getWorldPosA(ServerLevel level, float partialTick) {
            try {
                Long groundBodyId = VSGameUtilsKt.getShipObjectWorld(level)
                        .getDimensionToGroundBodyIdImmutable()
                        .get(VSGameUtilsKt.getDimensionId(level));

                if (shipA.equals(groundBodyId)) {
                    return new Vector3d(localPosA);
                } else {
                    Ship shipObject = VSGameUtilsKt.getShipObjectWorld(level).getAllShips().getById(shipA);
                    if (shipObject != null) {
                        Vector3d worldPos = new Vector3d();
                        shipObject.getTransform().getShipToWorld().transformPosition(localPosA, worldPos);
                        return worldPos;
                    }
                }
                return new Vector3d(localPosA);
            } catch (Exception e) {
                return new Vector3d(localPosA);
            }
        }

        public Vector3d getWorldPosB(ServerLevel level, float partialTick) {
            try {
                Long groundBodyId = VSGameUtilsKt.getShipObjectWorld(level)
                        .getDimensionToGroundBodyIdImmutable()
                        .get(VSGameUtilsKt.getDimensionId(level));

                if (shipB.equals(groundBodyId)) {
                    return new Vector3d(localPosB);
                } else {
                    Ship shipObject = VSGameUtilsKt.getShipObjectWorld(level).getAllShips().getById(shipB);
                    if (shipObject != null) {
                        Vector3d worldPos = new Vector3d();
                        shipObject.getTransform().getShipToWorld().transformPosition(localPosB, worldPos);
                        return worldPos;
                    }
                }
                return new Vector3d(localPosB);
            } catch (Exception e) {
                return new Vector3d(localPosB);
            }
        }
    }

    public static void addConstraintWithPersistence(ServerLevel level, Integer constraintId, Long shipA, Long shipB,
                                                    Vector3d localPosA, Vector3d localPosB, double maxLength,
                                                    double compliance, double maxForce) {
        RopeConstraintData data = new RopeConstraintData(shipA, shipB, localPosA, localPosB, maxLength, compliance, maxForce);
        activeConstraints.put(constraintId, data);

        ConstraintPersistence persistence = ConstraintPersistence.get(level);
        String persistenceId = "constraint_" + constraintId;
        persistence.addConstraint(persistenceId, shipA, shipB, localPosA, localPosB, maxLength, compliance, maxForce, level);

        NetworkHandler.sendConstraintAdd(constraintId, shipA, shipB, localPosA, localPosB, maxLength);
        System.out.println("Added constraint " + constraintId + " to tracker, persistence, and synced to clients");
    }


    public static void removeConstraint(Integer constraintId) {
        if (activeConstraints.remove(constraintId) != null) {
            NetworkHandler.sendConstraintRemove(constraintId);
            System.out.println("Removed constraint " + constraintId + " from tracker and synced to clients");
        }
    }

    public static void removeConstraintWithPersistence(ServerLevel level, Integer constraintId) {
        if (activeConstraints.remove(constraintId) != null) {
            ConstraintPersistence persistence = ConstraintPersistence.get(level);
            String persistenceId = "constraint_" + constraintId;
            persistence.removeConstraint(persistenceId);

            NetworkHandler.sendConstraintRemove(constraintId);
            System.out.println("Removed constraint " + constraintId + " from tracker, persistence, and synced to clients");
        }
    }

    public static Map<Integer, RopeConstraintData> getActiveConstraints() {
        return new HashMap<>(activeConstraints);
    }

    public static void clearAllConstraints() {
        activeConstraints.clear();
        NetworkHandler.sendConstraintClearAll();
        System.out.println("Cleared all constraints and synced to clients");
    }
    public static void addConstraintToTracker(Integer constraintId, Long shipA, Long shipB,
                                              Vector3d localPosA, Vector3d localPosB, double maxLength,
                                              double compliance, double maxForce) {
        RopeConstraintData data = new RopeConstraintData(shipA, shipB, localPosA, localPosB, maxLength, compliance, maxForce);
        activeConstraints.put(constraintId, data);

        NetworkHandler.sendConstraintAdd(constraintId, shipA, shipB, localPosA, localPosB, maxLength);
        System.out.println("Added constraint " + constraintId + " to tracker (restoration) and synced to clients");
    }

    @SubscribeEvent
    public static void onPlayerJoin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            for (Map.Entry<Integer, RopeConstraintData> entry : activeConstraints.entrySet()) {
                Integer constraintId = entry.getKey();
                RopeConstraintData data = entry.getValue();

                NetworkHandler.sendConstraintAddToPlayer(player, constraintId, data.shipA, data.shipB,
                        data.localPosA, data.localPosB, data.maxLength);
            }
            System.out.println("Synced " + activeConstraints.size() + " constraints to player " + player.getName().getString());
        }
    }
}
