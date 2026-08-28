package dev.flarelog.vstuff.content.ropes.util;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.flarelog.vstuff.VStuff;
import dev.flarelog.vstuff.internal.utility.CodecUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3d;
import org.valkyrienskies.core.api.bodies.ServerVsBody;
import org.valkyrienskies.core.api.ships.Ship;
import org.valkyrienskies.core.api.world.ServerShipWorld;
import org.valkyrienskies.core.internal.ships.VsiQueryableShipData;
import org.valkyrienskies.mod.api.ValkyrienSkies;
import org.valkyrienskies.mod.common.VSGameUtilsKt;
import org.valkyrienskies.mod.common.util.VectorConversionsMCKt;

import javax.annotation.Nullable;
import java.util.Optional;

public record LocalPosAndBodyId(@NotNull Vector3d pos, @Nullable Long id) {

    public static final Codec<LocalPosAndBodyId> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    CodecUtil.VECTOR3D.fieldOf("pos").forGetter(LocalPosAndBodyId::pos),
                    Codec.LONG.optionalFieldOf("id")
                            .xmap(opt -> opt.orElse(null), Optional::ofNullable)
                            .forGetter(LocalPosAndBodyId::id)
            ).apply(instance, LocalPosAndBodyId::new)
    );

    public LocalPosAndBodyId(@NotNull Vector3d shipPos, ServerLevel level) {
        this(shipPos, getId(shipPos, level));
    }

    public static LocalPosAndBodyId from(BlockPos pos, ServerLevel level) {
        return new LocalPosAndBodyId(VectorConversionsMCKt.toJOML(pos.getCenter()), level);
    }

    public static Pair<LocalPosAndBodyId, LocalPosAndBodyId> create(ServerLevel level, BlockPos pos0, BlockPos pos1) {
        LocalPosAndBodyId first = new LocalPosAndBodyId(VectorConversionsMCKt.toJOMLD(pos0), level);
        LocalPosAndBodyId second = new LocalPosAndBodyId(VectorConversionsMCKt.toJOMLD(pos1), level);

        if ((!first.isWorld() && second.isWorld())) {
            return new Pair<>(second, first);
        }

        return new Pair<>(first, second);
    }

    private static Long getId(Vector3d pos, ServerLevel level) {
        Ship ship = VSGameUtilsKt.getShipManagingPos(level, pos);
        return ship == null ? null : ship.getBodyId();
    }

    public boolean isWorld() {
        return this.id() == null;
    }

    public Vector3d getWorldPos(ServerLevel level) {
        if (this.id() == null) return pos;
        ServerShipWorld shipWorld = ValkyrienSkies.api().getServerShipWorld(level.getServer());
        if (shipWorld == null) return pos;
        ServerVsBody body = shipWorld.getAllBodies().getById(this.id());
        if (body == null) return pos;
        Vector3d vector3d = body.getKinematics().getTransform().getToWorld().transformPosition(pos, new Vector3d());
        VStuff.LOGGER.warn(String.valueOf(vector3d));
        return vector3d;
    }

    public @Nullable BlockPos blockPos(ServerLevel level) {
        if (this.isShip(level)) {
            return BlockPos.containing(this.pos().x(), this.pos().y(), this.pos().z());
        }
        return null;
    }

    public boolean isShip(Level level) {
        VsiQueryableShipData<Ship> allShips = VSGameUtilsKt.getAllShips(level);
        return allShips.contains(this.id());
    }
}
