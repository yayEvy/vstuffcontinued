package yay.evy.everest.vstuff.content.constraint;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.storage.DimensionDataStorage;

import yay.evy.everest.vstuff.VStuff;
import yay.evy.everest.vstuff.content.constraint.ropes.AbstractRope;
import yay.evy.everest.vstuff.content.constraint.ropes.RopeUtils;
import yay.evy.everest.vstuff.util.GetterUtils;

import java.util.HashMap;
import java.util.Map;

public class RopePersistenceYesItWorksNowIPromise3dsmile extends SavedData {

    public static final String NAME = "vstuff_ropes_pleasehelpmeiamgoinginsane";
    private final Map<Integer, AbstractRope> ropes = new HashMap<>();
    private boolean hasAttemptedRestore = false;

    public static RopePersistenceYesItWorksNowIPromise3dsmile get(ServerLevel level) {
        DimensionDataStorage storage = level.getDataStorage();
        return storage.computeIfAbsent(RopePersistenceYesItWorksNowIPromise3dsmile::load, RopePersistenceYesItWorksNowIPromise3dsmile::new, NAME);
    }

    @Override
    public CompoundTag save(CompoundTag tag) {
        CompoundTag ropesList = new CompoundTag();

        for (Map.Entry<Integer, AbstractRope> entry : ropes.entrySet()) {
            Integer constraintId = entry.getKey();

            CompoundTag constraintTag = entry.getValue().toTag();

            ropesList.put(constraintId.toString(), constraintTag);
        }

        tag.put("ropes", ropesList);
        System.out.println("Saved " + ropesList.size() + " constraints to " + NAME + ".dat");
        return tag;
    }

    public static RopePersistenceYesItWorksNowIPromise3dsmile load(CompoundTag tag) {
        RopePersistenceYesItWorksNowIPromise3dsmile data = new RopePersistenceYesItWorksNowIPromise3dsmile();
        CompoundTag ropesList = tag.getCompound("ropes");


        for (String ropeTagKey : ropesList.getAllKeys()) {
            CompoundTag ropeTag = ropesList.getCompound(ropeTagKey);

            AbstractRope rope = RopeUtils.fromTag(tag);

            data.ropes.put(ropeTag.getInt("ID"), rope);
        }
        return data;
    }

    public void saveNow(ServerLevel level) {
        setDirty();
        level.getDataStorage().save();
    }

    public void addRope(AbstractRope rope) {
        ropes.put(rope.ID, rope);
        setDirty();
    }

    public void removeRope(Integer ropeId) {
        ropes.remove(ropeId);
        setDirty();
    }

    public static void restoreConstraints(ServerLevel level) {
        RopePersistenceYesItWorksNowIPromise3dsmile persistence = RopePersistenceYesItWorksNowIPromise3dsmile.get(level);
        persistence.restoreConstraintsInstance(level);
    }

    public void restoreConstraintsInstance(ServerLevel level) {

        if (hasAttemptedRestore) return;
        hasAttemptedRestore = true;

        level.getServer().execute(() -> {
            try {
                Thread.sleep(100);
            } catch (InterruptedException ignored) {}

            Long currentGroundBodyId;
            try {
                currentGroundBodyId = GetterUtils.getGroundBodyId(level);
            } catch (Exception e) {
                VStuff.LOGGER.error("Failed to get ground body id: {}", e.getMessage());
                return;
            }

            int successCount = 0;
            int failCount = 0;

            for (Map.Entry<Integer, AbstractRope> entry : ropes.entrySet()) {
                Integer persistenceId = entry.getKey();
                AbstractRope rope = entry.getValue();


                if (tryRopeRestore(level, rope)) {
                    ropes.put(persistenceId, rope);
                    successCount++;
                } else {
                    failCount++;
                }
            }

            VStuff.LOGGER.info("ConstraintPersistence successfully restored {} constraints, failed to restore {} constraints", successCount, failCount);

            for (ServerPlayer player : level.getServer().getPlayerList().getPlayers()) {
                MasterOfRopes.syncAllConstraintsToPlayer(player);
            }
        });
    }

    private boolean tryRopeRestore(ServerLevel level, AbstractRope rope) {
        try {
            rope.createJoint(level);
        } catch (Exception e) {
            VStuff.LOGGER.error("Error restoring constraint {}: {}", rope.ID, e.getMessage());
        }
        return false;
    }
}
