package yay.evy.everest.vstuff.content.roperework;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.storage.DimensionDataStorage;
import org.valkyrienskies.mod.common.util.GameToPhysicsAdapter;
import yay.evy.everest.vstuff.util.GTPAUtils;

import java.util.HashMap;
import java.util.Map;

public class RopePersistence extends SavedData {

    public static final String NAME = "vstuff_ropes";

    public static Map<Integer, NewRope> persistenceRopes = new HashMap<>();

    @Override
    public CompoundTag save(CompoundTag pCompoundTag) {
        return null;
    }

    public static RopePersistence getOrCreate(ServerLevel level) {
        DimensionDataStorage storage = level.getDataStorage();
        return storage.computeIfAbsent(RopePersistence::load, RopePersistence::new, NAME);
    }

    public static RopePersistence load(CompoundTag tag) {
        RopeManager.resetId();
        RopePersistence ropePersistence = new RopePersistence();

        return ropePersistence;
    }

    public void saveNow(ServerLevel level) {
        setDirty();
        level.getDataStorage().save();
    }

    public void addRope(NewRope rope) {

    }

    public void removeRope(NewRope rope) {

    }
}
