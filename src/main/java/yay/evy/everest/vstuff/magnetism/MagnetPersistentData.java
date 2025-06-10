package yay.evy.everest.vstuff.magnetism;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.storage.DimensionDataStorage;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class MagnetPersistentData extends SavedData {
    private static final String DATA_NAME = "everest_magnets";
    private static final int MAX_MAGNETS_PER_DIMENSION = 1000;

    private final Map<ResourceKey<Level>, Set<BlockPos>> persistentMagnets = new HashMap<>();

    public MagnetPersistentData() {
        super();
    }

    public static MagnetPersistentData load(CompoundTag tag) {
        MagnetPersistentData data = new MagnetPersistentData();

        CompoundTag dimensionsTag = tag.getCompound("dimensions");
        for (String dimensionKey : dimensionsTag.getAllKeys()) {
            try {
                ResourceKey<Level> dimension = Level.OVERWORLD;

                if (dimensionKey.equals("minecraft:overworld")) {
                    dimension = Level.OVERWORLD;
                } else if (dimensionKey.equals("minecraft:the_nether")) {
                    dimension = Level.NETHER;
                } else if (dimensionKey.equals("minecraft:the_end")) {
                    dimension = Level.END;
                }

                ListTag magnetsList = dimensionsTag.getList(dimensionKey, Tag.TAG_COMPOUND);
                Set<BlockPos> magnets = new HashSet<>();

                for (int i = 0; i < magnetsList.size(); i++) {
                    CompoundTag magnetTag = magnetsList.getCompound(i);
                    BlockPos pos = new BlockPos(
                            magnetTag.getInt("x"),
                            magnetTag.getInt("y"),
                            magnetTag.getInt("z")
                    );
                    magnets.add(pos);
                }

                data.persistentMagnets.put(dimension, magnets);
                System.out.println("[PERSISTENT] Loaded " + magnets.size() + " magnets for dimension " + dimensionKey);
            } catch (Exception e) {
                System.err.println("[PERSISTENT] Error loading magnets for dimension " + dimensionKey + ": " + e.getMessage());
            }
        }

        return data;
    }

    @Override
    public @NotNull CompoundTag save(@NotNull CompoundTag tag) {
        CompoundTag dimensionsTag = new CompoundTag();

        for (Map.Entry<ResourceKey<Level>, Set<BlockPos>> entry : persistentMagnets.entrySet()) {
            String dimensionKey = entry.getKey().location().toString();
            ListTag magnetsList = new ListTag();

            for (BlockPos pos : entry.getValue()) {
                CompoundTag magnetTag = new CompoundTag();
                magnetTag.putInt("x", pos.getX());
                magnetTag.putInt("y", pos.getY());
                magnetTag.putInt("z", pos.getZ());
                magnetsList.add(magnetTag);
            }

            dimensionsTag.put(dimensionKey, magnetsList);
        }

        tag.put("dimensions", dimensionsTag);
        System.out.println("[PERSISTENT] Saved magnet data for " + persistentMagnets.size() + " dimensions");
        return tag;
    }

    public static MagnetPersistentData get(ServerLevel level) {
        DimensionDataStorage storage = level.getServer().overworld().getDataStorage();
        return storage.computeIfAbsent(MagnetPersistentData::load, MagnetPersistentData::new, DATA_NAME);
    }

    public void addMagnet(ResourceKey<Level> dimension, BlockPos pos) {
        Set<BlockPos> magnets = persistentMagnets.computeIfAbsent(dimension, k -> new HashSet<>());

        if (magnets.size() >= MAX_MAGNETS_PER_DIMENSION) {
            System.err.println("[PERSISTENT] WARNING: Maximum magnet limit reached for dimension " +
                    dimension.location() + " (" + MAX_MAGNETS_PER_DIMENSION + ")");
            return;
        }

        magnets.add(pos);
        setDirty();
        System.out.println("[PERSISTENT] Added magnet at " + pos + " in dimension " + dimension.location() +
                " (total: " + magnets.size() + ")");
    }
    public void removeMagnet(ResourceKey<Level> dimension, BlockPos pos) {
        Set<BlockPos> magnets = persistentMagnets.get(dimension);
        if (magnets != null) {
            boolean removed = magnets.remove(pos);
            if (removed) {
                setDirty();
                System.out.println("[PERSISTENT] Removed magnet at " + pos + " in dimension " + dimension.location());
            }
            if (magnets.isEmpty()) {
                persistentMagnets.remove(dimension);
            }
        }
    }

    public Set<BlockPos> getMagnets(ResourceKey<Level> dimension) {
        return persistentMagnets.getOrDefault(dimension, Collections.emptySet());
    }

    public void clearMagnets(ResourceKey<Level> dimension) {
        if (persistentMagnets.remove(dimension) != null) {
            setDirty();
            System.out.println("[PERSISTENT] Cleared all magnets for dimension " + dimension.location());
        }
    }
}
