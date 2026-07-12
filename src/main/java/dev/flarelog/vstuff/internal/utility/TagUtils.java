package dev.flarelog.vstuff.internal.utility;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import org.joml.Vector3d;
import org.valkyrienskies.core.internal.joints.VSJointMaxForceTorque;
import dev.flarelog.vstuff.internal.utility.records.JointValues;
import dev.flarelog.vstuff.internal.utility.records.RopePosData;
import dev.flarelog.vstuff.internal.utility.records.RopeSegment;

import java.util.Locale;

public class TagUtils {

    public static CompoundTag writeVector3d(Vector3d vector3d) {
        CompoundTag compoundTag = new CompoundTag();
        compoundTag.putDouble("X", vector3d.x);
        compoundTag.putDouble("Y", vector3d.y);
        compoundTag.putDouble("Z", vector3d.z);
        return compoundTag;
    }

    public static Vector3d readVector3d(CompoundTag tag) {
        return new Vector3d(tag.getDouble("X"), tag.getDouble("Y"), tag.getDouble("Z"));
    }

    public static CompoundTag writeJointValues(JointValues jointValues) {
        CompoundTag tag = new CompoundTag();

        tag.putFloat("maxForce", jointValues.maxForceTorque().getMaxForce());
        tag.putFloat("maxTorque", jointValues.maxForceTorque().getMaxTorque());
        tag.putFloat("minLength", jointValues.minLength());
        tag.putFloat("maxLength", jointValues.maxLength());
        tag.putDouble("compliance", jointValues.compliance());
        tag.putFloat("tolerance", jointValues.tolerance() == null ? -1 : jointValues.tolerance());
        tag.putFloat("stiffness", jointValues.stiffness() == null ? -1 : jointValues.stiffness());
        tag.putFloat("damping", jointValues.damping() == null ? -1 : jointValues.damping());

        return tag;
    }

    public static JointValues readJointValues(CompoundTag tag) {
        VSJointMaxForceTorque maxForceTorque = new VSJointMaxForceTorque(tag.getFloat("maxForce"), tag.getFloat("maxTorque"));
        Float tolerance = tag.getFloat("tolerance");
        Float stiffness = tag.getFloat("stiffness");
        Float damping = tag.getFloat("damping");

        if (tolerance == -1) tolerance = null;
        if (stiffness == -1) stiffness = null;
        if (damping == -1) damping = null;

        return new JointValues(maxForceTorque, tag.getFloat("minLength"), tag.getFloat("maxLength"), tag.getDouble("compliance"), tolerance, stiffness, damping);
    }

    public static CompoundTag writePosData(RopePosData posData) {
        CompoundTag tag = new CompoundTag();

        tag.putLong("shipId", posData.shipId() == null ? -1 : posData.shipId()); // use -1 to denote ground body
        tag.put("blockPos", NbtUtils.writeBlockPos(posData.blockPos()));
        tag.put("localPos", writeVector3d(posData.localPos()));
        tag.putString("selectType", posData.selectType().name());

        return tag;
    }

    public static RopePosData readPosData(CompoundTag tag) {
        Long shipId = tag.getLong("shipId") == -1 ? null : tag.getLong("shipId");
        BlockPos blockPos = NbtUtils.readBlockPos(tag.getCompound("blockPos"));
        Vector3d localPos = readVector3d(tag.getCompound("localPos"));
        RopeUtils.SelectType selectType = RopeUtils.SelectType.valueOf(tag.getString("selectType"));

        return new RopePosData(shipId, blockPos, localPos, shipId == null, selectType);
    }

    public static CompoundTag writeResourceLocation(ResourceLocation resourceLocation) {
        CompoundTag tag = new CompoundTag();

        tag.putString("namespace", resourceLocation.getNamespace());
        tag.putString("path", resourceLocation.getPath());

        return tag;
    }

    public static ResourceLocation readResourceLocation(CompoundTag tag) {
        return ResourceLocation.fromNamespaceAndPath(tag.getString("namespace"), tag.getString("path"));
    }

    public static <T> CompoundTag writeResourceKey(ResourceKey<T> key) {
        CompoundTag tag = new CompoundTag();

        tag.put("registry", writeResourceLocation(key.registry()));
        tag.put("location", writeResourceLocation(key.location()));

        return tag;
    }

    public static <T> ResourceKey<T> readResourceKey(CompoundTag tag) {
        ResourceLocation registry = readResourceLocation(tag.getCompound("registry"));
        ResourceLocation location = readResourceLocation(tag.getCompound("location"));

        return ResourceKey.create(ResourceKey.createRegistryKey(registry), location);
    }

    public static CompoundTag writeRopeSegment(RopeSegment segment) {
        CompoundTag tag = new CompoundTag();

        tag.putLong("id0", segment.id0() == null ? -1 : segment.id0());
        tag.putLong("id1", segment.id1() == null ? -1 : segment.id1());
        tag.put("pos0", writeVector3d(segment.pos0()));
        tag.put("pos1", writeVector3d(segment.pos1()));

        return tag;
    }

    public static RopeSegment readRopeSegment(CompoundTag tag) {
        Long id0 = tag.getLong("id0") == -1 ? null : tag.getLong("id0");
        Long id1 = tag.getLong("id1") == -1 ? null : tag.getLong("id1");
        Vector3d pos0 = readVector3d(tag.getCompound("pos0"));
        Vector3d pos1 = readVector3d(tag.getCompound("pos1"));

        return new RopeSegment(id0, id1, pos0, pos1);
    }

    public static String sanitizeFileName(String name) {
        return name.toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9_]", "_");
    }
}
